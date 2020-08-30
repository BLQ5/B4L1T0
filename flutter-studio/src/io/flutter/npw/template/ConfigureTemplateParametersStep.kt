/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.template

import com.android.tools.adtui.TabularLayout
import com.android.tools.adtui.validation.ValidatorPanel
import com.android.tools.idea.npw.bindExpression
import com.android.tools.idea.npw.invokeLater
import com.android.tools.idea.npw.model.NewProjectModel.Companion.getSuggestedProjectPackage
import com.android.tools.idea.npw.model.NewProjectModel.Companion.nameToJavaPackage
import com.android.tools.idea.npw.template.components.ComponentProvider
import com.android.tools.idea.observable.AbstractProperty
import com.android.tools.idea.observable.BindingsManager
import com.android.tools.idea.observable.ListenerManager
import com.android.tools.idea.observable.Receiver
import com.android.tools.idea.observable.core.BoolProperty
import com.android.tools.idea.observable.core.BoolValueProperty
import com.android.tools.idea.observable.core.ObservableBool
import com.android.tools.idea.observable.core.StringProperty
import com.android.tools.idea.observable.core.StringValueProperty
import com.android.tools.idea.observable.expressions.Expression
import com.android.tools.idea.observable.ui.IconProperty
import com.android.tools.idea.observable.ui.TextProperty
import com.android.tools.idea.observable.ui.VisibleProperty
import com.android.tools.idea.ui.wizard.WizardUtils
import com.android.tools.idea.ui.wizard.WizardUtils.wrapWithVScroll
import com.android.tools.idea.wizard.model.ModelWizardStep
import com.android.tools.idea.wizard.template.WizardUiContext
import com.google.common.base.Joiner
import com.google.common.cache.CacheBuilder
import com.google.common.io.Files
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.ui.RecentsManager
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import io.flutter.FlutterBundle
import io.flutter.npw.model.RenderTemplateModel
import io.flutter.npw.template.components.CheckboxProvider
import io.flutter.npw.template.components.LabelFieldProvider
import io.flutter.npw.template.components.LabelWithEditButtonProvider
import io.flutter.npw.template.components.PackageComboProvider
import io.flutter.npw.template.components.SeparatorProvider
import io.flutter.npw.template.components.TextFieldProvider
import org.jetbrains.kotlin.utils.addToStdlib.firstNotNullResult
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.util.EnumSet
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

val TYPE_CONSTRAINTS: EnumSet<Constraint> = EnumSet.of(
  Constraint.CLASS,
  Constraint.PACKAGE,
  Constraint.MODULE,
  Constraint.STRING
)

fun Parameter<*>.isRelated(p: Parameter<*>): Boolean =
  p is StringParameter && this is StringParameter && p !== this &&
  TYPE_CONSTRAINTS.intersect(constraints).intersect(p.constraints).isNotEmpty()

// See com.android.tools.idea.npw.template.ConfigureTemplateParametersStep
class ConfigureTemplateParametersStep(model: RenderTemplateModel, title: String, private val templates: List<NamedModuleTemplate>)
  : ModelWizardStep<RenderTemplateModel>(model, title) {
  private val bindings = BindingsManager()
  private val listeners = ListenerManager()
  private val thumbnailsCache = CacheBuilder.newBuilder().build(IconLoader())!!
  private val parameterRows = hashMapOf<Parameter<in Any>, RowEntry<*>>()
  private val userValues = hashMapOf<Parameter<*>, Any>()
  private val thumbPath = StringValueProperty()

  /**
   * Validity check of all parameters is performed when any parameter changes, and the first error found is set here.
   * This is then registered as its own validator with [validatorPanel].
   * This vastly simplifies validation, as we no longer have to worry about implicit relationships between parameters
   * (when changing one makes another valid/invalid).
   */
  private val invalidParameterMessage = StringValueProperty()

  private val templateDescriptionLabel = JLabel().apply {
    font = Font("Default", Font.PLAIN, 11)
  }
  private val templateThumbLabel = JLabel().apply {
    horizontalTextPosition = SwingConstants.CENTER
    verticalAlignment = SwingConstants.TOP
    verticalTextPosition = SwingConstants.BOTTOM
    font = Font("Default", Font.PLAIN, 16)
  }
  private var parametersPanel = JPanel(TabularLayout("Fit-,*").setVGap(10))

  private val rootPanel = JBPanel<JBPanel<*>>(GridLayoutManager(2, 2)).apply {
    val anySize = Dimension(-1, -1)
    val defaultSizePolicy = GridConstraints.SIZEPOLICY_CAN_GROW or GridConstraints.SIZEPOLICY_CAN_SHRINK
    add(templateThumbLabel,
        GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                        0, 0, anySize, anySize, anySize))
    add(templateDescriptionLabel,
        GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, defaultSizePolicy, 0, anySize, anySize,
                        anySize))
    add(parametersPanel,
        GridConstraints(0, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                        defaultSizePolicy,// or GridConstraints.SIZEPOLICY_WANT_GROW,
                        defaultSizePolicy or GridConstraints.SIZEPOLICY_WANT_GROW,
                        anySize, anySize, anySize))
  }

  private val validatorPanel: ValidatorPanel = ValidatorPanel(this, wrapWithVScroll(rootPanel))
  private var evaluationState = EvaluationState.NOT_EVALUATING
  private val parameters: Collection<Parameter<*>> get() = model.newTemplate.parameters

  /**
   * Get the current thumbnail path.
   */
  private val thumbnailPath: String
    get() = model.newTemplate.thumb().path().path

  private val project: Project? get() = if (model.isNewProject) null else model.project.value

  /**
   * Given a parameter, return a String key we can use to interact with IntelliJ's [RecentsManager] system.
   */
  private fun getRecentsKeyForParameter(parameter: Parameter<*>) = "flutter.template.${parameter.hashCode()}"

  @Suppress("UNCHECKED_CAST")
  override fun onEntering() {
    // The Model TemplateHandle may have changed, rebuild the panel
    resetPanel()

    val newTemplate = model.newTemplate

    invokeLater {
      // We want to set the label's text AFTER the wizard has been packed. Otherwise, its
      // width calculation gets involved and can really stretch out some wizards if the label is
      // particularly long (see Master/Detail Activity for example).
      templateDescriptionLabel.text = WizardUtils.toHtmlString(newTemplate.description)
    }

    icon = newTemplate.icon

    val thumb = IconProperty(templateThumbLabel)
    val thumbVisibility = VisibleProperty(templateThumbLabel)
    bindings.apply {
      bindExpression(thumb, thumbPath) { thumbnailsCache.getUnchecked(newTemplate.thumb().path()) }
      bindExpression(thumbVisibility, thumb) { thumb.get().isPresent }
    }
    thumbPath.set(thumbnailPath)
    templateThumbLabel.text = newTemplate.name

    for (widget in model.newTemplate.widgets) {
      val row = createRowForWidget(model.module, widget).apply { addToPanel(parametersPanel) }

      if (widget !is ParameterWidget<*>) {
        continue
      }

      val property = row.property
      val parameter = widget.parameter as Parameter<in Any>
      property?.addListener {
        // If not evaluating, change comes from the user (or user pressed "Back" and updates are "external". eg Template changed)
        if (evaluationState != EvaluationState.EVALUATING && rootPanel.isShowing) {
          userValues[parameter] = property.get()
          parameter.setFromProperty(property)
          // Evaluate later to prevent modifying Swing values that are locked during read
          enqueueEvaluateParameters()
        }
      }
      parameterRows[parameter] = row
      when (widget) {
        // We cannot know a good default value for package in template, but it's being preset in [createRowForWidget]
        is PackageNameWidget -> row.setValue(parameter.value) //parameter.value = property!!.get()
        else -> row.setValue(parameter.value)
      }
    }

    validatorPanel.registerMessageSource(invalidParameterMessage)

    // TODO do not deduplicate package name etc.
    val parameterValues = parameters.filterIsInstance<StringParameter>()
      .associateWith { userValues[it] ?: deduplicate(it) }

    parameters.forEach {
      val resolvedValue = parameterValues[it]
      if (resolvedValue != null) {
        parameterRows[it]!!.setValue(resolvedValue)
      }
    }

    val packageNameEntry = findPackageNameEntry()
    if (packageNameEntry != null) {
      val projectNameEntry = findProjectNameEntry()!!
      val projectName = projectNameEntry.property as TextProperty
      val basePackage = getSuggestedProjectPackage()

      val computedPackageName: Expression<String> = projectName // was: model.projectName
        .transform { appName: String? -> String.format("%s.%s", basePackage, nameToJavaPackage(appName!!)) }
      val packageNameText = packageNameEntry.property as TextProperty
      packageNameText.set(computedPackageName.get())
      model.packageName.set(packageNameText.get())
      val isPackageNameSynced: BoolProperty = BoolValueProperty(true)
      bindings.bind(model.packageName, packageNameText)

      bindings.bind(packageNameText, computedPackageName, isPackageNameSynced)
      listeners.listen(packageNameText,
                       Receiver { value: String -> isPackageNameSynced.set(value == computedPackageName.get()) })
    }

    evaluateParameters()
  }

  private fun findPackageNameEntry(): RowEntry<*>? = findEntry("studio.npw.package.name")
  private fun findProjectNameEntry(): RowEntry<*>? = findEntry("studio.npw.project.name")

  private fun findEntry(bundleName: String): RowEntry<*>? {
    if (model.newTemplate.uiContexts.contains(WizardUiContext.NewModule)) return null
    val key = FlutterBundle.message(bundleName)
    val parameter = parameterRows.keys.find { p -> p.name == key }
    return parameterRows[parameter] as RowEntry<*>
  }

  /**
   * Every template parameter, based on its type, can generate a row of* components. For example, a text parameter becomes a
   * "Label: Textfield" set, while a list of choices becomes "Label: pulldown".
   *
   * This method takes an input [Parameter] and returns a generated [RowEntry] for  it, which neatly encapsulates its UI.
   * The caller should use [RowEntry.addToPanel] after receiving it.
   */
  private fun createRowForWidget(module: Module?, widget: Widget<*>): RowEntry<*> = when (widget) {
    is TextFieldWidget -> RowEntry(widget.p.name, TextFieldProvider(widget.parameter))
    is LabelWidget -> RowEntry(LabelFieldProvider(widget.text))
    is PackageNameWidget -> {
      val rowEntry = if (module != null)
        RowEntry(
          widget.p.name, PackageComboProvider(module.project, widget.p, model.packageName.get(), getRecentsKeyForParameter(widget.p))
        )
      else
        RowEntry(widget.p.name, LabelWithEditButtonProvider(widget.p))

      // All ATTR_PACKAGE_NAME providers should be string types and provide StringProperties
      val packageName = rowEntry.property as StringProperty
      if (!packageName.isEmpty.get() && model.packageName.isEmpty.get()) model.packageName.set(packageName.get())
      bindings.bindTwoWay(packageName, model.packageName)
      // Model.packageName is used for parameter evaluation, but updated asynchronously. Do new evaluation when value changes.
      listeners.listen(model.packageName) { enqueueEvaluateParameters() }
      rowEntry
    }
    is CheckBoxWidget -> RowEntry(CheckboxProvider(widget.p))
    is Separator -> RowEntry(SeparatorProvider())
    is SdkSelectorWidget -> RowEntry(widget.p.name, TextFieldProvider(widget.parameter))
    // TODO(messick) Create SdkSelectorWidget
    else -> error("Only string and bool parameters are supported for now")
  }

  /**
   * Instead of evaluating all parameters immediately, invoke the request to run later. This option allows us to avoid the situation where
   * a value has just changed, is forcefully re-evaluated immediately, and causes Swing to throw an exception between we're editing a
   * value while it's in a locked read-only state.
   */
  private fun enqueueEvaluateParameters() {
    if (evaluationState == EvaluationState.REQUEST_ENQUEUED) {
      return
    }
    evaluationState = EvaluationState.REQUEST_ENQUEUED

    invokeLater { evaluateParameters() }
  }

  /**
   * Run through all parameters for our current template and update their values,
   * including visibility, enabled state, and actual values.
   */
  private fun evaluateParameters() {
    evaluationState = EvaluationState.EVALUATING

    parameters.forEach { p ->
      parameterRows[p]!!.apply {
        setEnabled(p.enabled)
        setVisible(p.isVisibleAndEnabled)
      }
    }

    val parameterValues = parameters.filterIsInstance<StringParameter>()
      .associateWith { userValues[it] ?: deduplicate(it) }

    parameters.forEach {
      val resolvedValue = parameterValues[it]
      if (resolvedValue != null) {
        parameterRows[it]!!.setValue(resolvedValue)
      }
    }

    // Aggressively update the icon path just in case it changed
    thumbPath.set(thumbnailPath)

    evaluationState = EvaluationState.NOT_EVALUATING

    invalidParameterMessage.set(validateAllParameters() ?: "")
  }

  private fun validateAllParameters(): String? {
    val sourceProvider = null //model.template.get().getSourceProvider()

    return parameters
      .filterIsInstance<StringParameter>()
      .filter { it.isVisibleAndEnabled }
      .firstNotNullResult { parameter ->
        val property = parameterRows[parameter as Parameter<in Any>]?.property ?: return@firstNotNullResult null
        parameter.validate(project, model.module, sourceProvider, model.packageName.get(), property.get(), getRelatedValues(parameter))
      }
  }

  override fun getComponent(): JComponent = validatorPanel

  override fun getPreferredFocusComponent(): JComponent? = parametersPanel.components.firstOrNull {
    val child = it as JComponent
    child.componentCount == 0 && child.isFocusable && child.isVisible
  } as? JComponent

  override fun canGoForward(): ObservableBool = validatorPanel.hasErrors().not()

  private fun resetPanel() {
    parametersPanel.removeAll()
    parameterRows.clear()
    userValues.clear()
    dispose()
  }

  override fun dispose() {
    bindings.releaseAll()
    listeners.releaseAll()
    thumbnailsCache.invalidateAll()
  }

  /**
   * When finished with this step, calculate and install a bunch of values.
   */
  override fun onProceeding() {
    // Some parameter values should be saved for later runs through this wizard, so do that first.
    parameterRows.values.forEach(RowEntry<*>::accept)

    parameterRows.forEach { (p, row) ->
      p.setFromProperty(row.property!!)
    }
  }

  private fun <T> Parameter<T>.setFromProperty(property: AbstractProperty<*>) {
    @Suppress("UNCHECKED_CAST")
    this.value = property.get() as T // TODO(qumeric): row may have no property? (e.g. separator)
  }

  /**
   * Fetches the values of all parameters that are related to the target parameter. This is useful when validating a parameter's value.
   */
  private fun getRelatedValues(parameter: Parameter<*>): Set<Any> =
    parameters.filter { parameter.isRelated(it) }.mapNotNull { parameterRows[it]?.property?.get() }.toSet()

  /**
   * Because the FreeMarker templating engine is mostly opaque to us, any time any parameter changes, we need to re-evaluate all parameters.
   * Parameter evaluation can be started immediately via [evaluateParameters] or with a delay using [enqueueEvaluateParameters].
   */
  private enum class EvaluationState {
    NOT_EVALUATING,
    REQUEST_ENQUEUED,
    EVALUATING
  }

  /**
   * A template is broken down into separate fields, each which is given a row with optional header.
   * This class wraps all UI elements in the row, providing methods for managing them.
   */
  private class RowEntry<T : JComponent> {
    val component: T
    val property: AbstractProperty<*>?

    private val header: JPanel?
    private val componentProvider: ComponentProvider<T>
    private val container: JPanel = JBPanel<JBPanel<*>>().apply {
      layout = BoxLayout(this, BoxLayout.Y_AXIS)
    }

    constructor(headerText: String, componentProvider: ComponentProvider<T>) {
      val headerLabel = JBLabel(headerText)
      header = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
        add(headerLabel)
        add(Box.createHorizontalStrut(20))
      }
      this.componentProvider = componentProvider
      component = componentProvider.createComponent()
      property = componentProvider.createProperty(component)

      headerLabel.labelFor = component
      container.apply {
        add(header)
        add(component)
      }
    }

    constructor(componentProvider: ComponentProvider<T>) {
      header = null
      this.componentProvider = componentProvider
      component = componentProvider.createComponent()
      property = componentProvider.createProperty(component)
      container.add(component)
    }

    fun addToPanel(panel: JPanel) {
      require(panel.layout is TabularLayout)
      val row = panel.componentCount
      panel.add(container, TabularLayout.Constraint(row, 1, 1))
    }

    fun setEnabled(enabled: Boolean) {
      header?.isEnabled = enabled
      component.isEnabled = enabled
    }

    fun setVisible(visible: Boolean) {
      header?.isVisible = visible
      component.isVisible = visible
    }

    @Suppress("UNCHECKED_CAST")
    fun <V> setValue(value: V) {
      checkNotNull(property)
      (property as AbstractProperty<V>).set(value)
    }

    fun accept() {
      componentProvider.accept(component)
    }
  }

  private fun deduplicate(parameter: StringParameter): String {
    val value = parameter.suggest() ?: parameter.value
    if (value.isEmpty() || !parameter.constraints.contains(Constraint.UNIQUE)) {
      return value
    }

    var suggested = value
    val extPart = Files.getFileExtension(value)

    // First remove file extension. Then remove all trailing digits, because we probably were the ones that put them there.
    // For example, if two parameters affect each other, say "Name" and "Layout", you get this:
    // Step 1) Resolve "Name" -> "Name2", causes related "Layout" to become "Layout2"
    // Step 2) Resolve "Layout2" -> "Layout22"
    // Although we may possibly strip real digits from a name, it's much more likely we're not,
    // and a user can always modify the related value manually in that rare case.
    val namePart = value.replace(".$extPart", "").replace("\\d*$".toRegex(), "")
    val filenameJoiner = Joiner.on('.').skipNulls()

    var suffix = 2
    val relatedValues = getRelatedValues(parameter)
    val sourceProvider = null //model.template.get().getSourceProvider()
    while (!parameter.uniquenessSatisfied(project, model.module, sourceProvider, model.packageName.get(), suggested, relatedValues)) {
      suggested = filenameJoiner.join(namePart + suffix, extPart.ifEmpty { null })
      suffix++
    }
    return suggested
  }
}