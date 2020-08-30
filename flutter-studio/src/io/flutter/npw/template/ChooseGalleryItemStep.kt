/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.template

import com.android.tools.adtui.util.FormScalingUtil
import com.android.tools.adtui.validation.Validator
import com.android.tools.adtui.validation.Validator.Result.Companion.fromNullableMessage
import com.android.tools.adtui.validation.ValidatorPanel
import com.android.tools.idea.model.AndroidModuleInfo
import com.android.tools.idea.npw.template.BlankModel
import com.android.tools.idea.npw.template.ChooseGalleryItemStep.TemplateRenderer
import com.android.tools.idea.npw.template.WizardGalleryItemsStepMessageKeys
import com.android.tools.idea.npw.ui.TemplateIcon
import com.android.tools.idea.npw.ui.WizardGallery
import com.android.tools.idea.observable.ListenerManager
import com.android.tools.idea.observable.core.ObservableBool
import com.android.tools.idea.observable.core.StringValueProperty
import com.android.tools.idea.ui.wizard.WizardUtils
import com.android.tools.idea.wizard.model.ModelWizard
import com.android.tools.idea.wizard.model.ModelWizardStep
import com.android.tools.idea.wizard.model.SkippableWizardStep
import com.android.tools.idea.wizard.model.WizardModel
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable
import com.intellij.openapi.util.io.FileUtil
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import icons.AndroidIcons
import icons.FlutterIcons
import io.flutter.FlutterBundle
import io.flutter.FlutterConstants
import io.flutter.FlutterUtils
import io.flutter.npw.model.RenderTemplateModel
import io.flutter.sdk.FlutterSdkUtil
import io.flutter.wizard.template.FlutterWizardTemplateProvider
import org.jetbrains.android.facet.AndroidFacet
import org.jetbrains.android.util.AndroidBundle
import java.awt.event.ActionEvent
import java.io.File
import javax.swing.AbstractAction
import javax.swing.Icon
import javax.swing.JComponent

class ChooseGalleryItemStep(
  private val renderModel: RenderTemplateModel,
  //private val messageKeys: WizardGalleryItemsStepMessageKeys,
  private val emptyItemLabel: String
) : SkippableWizardStep<WizardModel>(BlankModel(), FlutterBundle.message("studio.project.gallery.select"), FlutterIcons.Flutter_64) {
  private val templateRenderers: List<TemplateRenderer> = sequence {
    if (isNewModule) {
      yield(NewTemplateRenderer(Template))
    }
    yieldAll(FlutterWizardTemplateProvider().getTemplates()
               .map(ChooseGalleryItemStep::NewTemplateRenderer))
  }.toList()
  private val itemGallery = WizardGallery(title, { t: NewTemplateRenderer? -> t!!.icon }, { t: NewTemplateRenderer? -> t!!.label })
  private val validatorPanel = ValidatorPanel(this, JBScrollPane(itemGallery)).also {
    FormScalingUtil.scaleComponentTree(this.javaClass, it)
  }

  private val invalidParameterMessage = StringValueProperty()
  private val listeners = ListenerManager()

  private val isNewModule: Boolean
    get() = renderModel.module == null

  override fun getComponent(): JComponent = validatorPanel

  override fun getPreferredFocusComponent(): JComponent? = itemGallery

  public override fun createDependentSteps(): Collection<ModelWizardStep<*>> =
    listOf()

  override fun dispose() = listeners.releaseAll()

  override fun onWizardStarting(wizard: ModelWizard.Facade) {
    validatorPanel.registerMessageSource(invalidParameterMessage)

    itemGallery.setDefaultAction(object : AbstractAction() {
      override fun actionPerformed(actionEvent: ActionEvent) {
        wizard.goForward()
      }
    })

    itemGallery.addListSelectionListener {
      itemGallery.selectedElement?.run {
        renderModel.newTemplate = this.template
        wizard.updateNavigationProperties()
      }
      validateTemplate()
    }

    itemGallery.run {
      model = JBList.createDefaultListModel(templateRenderers)
      selectedIndex = getDefaultSelectedTemplateIndex(templateRenderers, emptyItemLabel)
    }
  }

  override fun canGoForward(): ObservableBool = validatorPanel.hasErrors().not()

  override fun onEntering() = validateTemplate()

  private fun validateTemplate() {
    invalidParameterMessage.set(validateFlutterModuleName(renderModel.projectName.get()))
    if (!invalidParameterMessage.isEmpty.get()) return
    // SDK path, download error from SDK downloader
    invalidParameterMessage.set(validateFlutterSdk(renderModel.sdkPath.get()))
    if (!invalidParameterMessage.isEmpty.get()) return

    invalidParameterMessage.set(
      renderModel.newTemplate.validate(isNewModule, renderModel.androidXSupport.get(), if (renderModel.useKotlin.get()) Language.Kotlin else Language.Java,
      if (renderModel.useSwift.get()) Language.Swift else Language.ObjC, WizardGalleryItemsStepMessageKeys("","","","","", "", ""))
    )
  }

  private fun getContainerName() = "project"
  /**
   * @see [https://dart.dev/tools/pub/pubspec.name](dart.dev/tools/pub/pubspec.name)
   */
  private fun validateFlutterModuleName(moduleName: String): String {
    if (moduleName.isEmpty()) {
      return "Please enter a name for the " + getContainerName() + "."
    }
    val loc = File(FileUtil.toSystemDependentName(renderModel.projectLocation.get()), renderModel.projectName.get())
    if (loc.exists()) {
      return "Project location already exists: ${loc.path}"
    }
    if (!FlutterUtils.isValidPackageName(moduleName)) {
      return "Invalid " + getContainerName() + " name: '" + moduleName + "' - must be a valid Dart package name (lower_case_with_underscores)."
    }
    if (FlutterUtils.isDartKeyword(moduleName)) {
      return "Invalid module name: '$moduleName' - must not be a Dart keyword."
    }
    // Package name is more restrictive than identifier, so no need to check for identifier validity.
    if (FlutterConstants.FLUTTER_PACKAGE_DEPENDENCIES.contains(moduleName)) {
      return "Invalid " + getContainerName() + " name: '" + moduleName + "' - this will conflict with Flutter package dependencies."
    }
    if (moduleName.length > FlutterConstants.MAX_MODULE_NAME_LENGTH) {
      return "Invalid " + getContainerName() + " name - must be less than " + FlutterConstants.MAX_MODULE_NAME_LENGTH + " characters."
    }
    if (renderModel.project.get().isPresent) {
      val project: Project = renderModel.project.value
      val module: Module?
      val fromConfigurable = ProjectStructureConfigurable.getInstance(
        project)
      module = if (fromConfigurable != null) {
        fromConfigurable.modulesConfig.getModule(moduleName)
      }
      else {
        ModuleManager.getInstance(project).findModuleByName(moduleName)
      }
      if (module != null) {
        return "A module with that name already exists in the project."
      }
    }
    return ""
  }
  private fun validateFlutterSdk(sdkPath: String):String {
    return if (sdkPath.isNotEmpty()) {
      val message = FlutterSdkUtil.getErrorMessageIfWrongSdkRootPath(sdkPath)
      message ?: ""
    }
    else {
      "Flutter SDK path not given."
    }
  }

  open class NewTemplateRenderer(internal val template: Template) : TemplateRenderer {
    override val label: String
      get() = getTemplateTitle(template)

    override val icon: Icon?
      get() = getTemplateIcon(template)

    override val exists: Boolean = template != Template.None

    override fun toString(): String = label
  }
}

fun getDefaultSelectedTemplateIndex(
  templateRenderers: List<com.android.tools.idea.npw.template.ChooseGalleryItemStep.TemplateRenderer>,
  emptyItemLabel: String = "Empty Activity"
): Int = templateRenderers.indices.run {
  val defaultTemplateIndex = firstOrNull { templateRenderers[it].label == emptyItemLabel }
  val firstValidTemplateIndex = firstOrNull { templateRenderers[it].exists }

  defaultTemplateIndex ?: firstValidTemplateIndex ?: throw IllegalArgumentException("No valid Template found")
}

fun getTemplateTitle(template: Template): String =
  template.name

private val logger = Logger.getInstance(TemplateIcon::class.java)

fun getTemplateIcon(template: Template): TemplateIcon? {
  if (template == Template.None) {
    return TemplateIcon(AndroidIcons.Wizards.NoActivity)
  }

  return try {
    val icon = com.intellij.openapi.util.IconLoader.findIcon(template.thumb().path()) ?: return null
    TemplateIcon(icon)
  }
  catch (e: Exception) {
    logger.warn(e)
    // Return the icon for No Activity to prevent other templates from not being rendered even if an exception is thrown.
    // For example if a template has a wrong path name for its thumbnail, template.thumb() throws IllegalArgumentException
    TemplateIcon(AndroidIcons.Wizards.NoActivity)
  }
}

fun Template.validate(isNewModule: Boolean,
                      isAndroidxProject: Boolean,
                      androidLanguage: Language,
                      iosLanguage: Language,
                      messageKeys: WizardGalleryItemsStepMessageKeys
): String = when {
  this == Template.None -> if (isNewModule) "" else AndroidBundle.message(messageKeys.itemNotFound)
  constraints.contains(TemplateConstraint.AndroidX) && !isAndroidxProject -> AndroidBundle.message(messageKeys.invalidAndroidX)
  constraints.contains(TemplateConstraint.Kotlin) && androidLanguage != Language.Kotlin && isNewModule -> AndroidBundle.message(
    messageKeys.invalidNeedsKotlin)
  constraints.contains(TemplateConstraint.Swift) && iosLanguage != Language.Swift && isNewModule -> FlutterBundle.message("invalidNeedsSwift")
  else -> ""
}
