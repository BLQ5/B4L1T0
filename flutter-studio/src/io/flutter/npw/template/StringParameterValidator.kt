/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.flutter.npw.template

import com.android.SdkConstants
import com.android.builder.model.SourceProvider
import com.android.resources.ResourceFolderType
import com.android.resources.ResourceType
import com.android.tools.idea.npw.assetstudio.resourceExists
import com.android.tools.idea.res.IdeResourceNameValidator
import com.android.tools.idea.util.androidFacet
import com.google.common.annotations.VisibleForTesting
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope
import io.flutter.sdk.FlutterSdkUtil
import org.jetbrains.android.util.AndroidUtils
import java.io.File

/**
 * Validate the given value for this parameter and list any reasons why the given value is invalid.
 * @return An error message detailing why the given value is invalid.
 */
fun StringParameter.validate(
  project: Project?, module: Module?, provider: SourceProvider?, packageName: String?, value: Any?, relatedValues: Set<Any>
): String? {
  val v = value?.toString().orEmpty()
  val violations = validateStringType(project, module, provider, packageName, v, relatedValues)
  return violations.mapNotNull { getErrorMessageForViolatedConstraint(it, v) }.firstOrNull()
}

private fun StringParameter.getErrorMessageForViolatedConstraint(c: Constraint, value: String): String? = when (c) {
  Constraint.NONEMPTY -> "Please specify $name"
  Constraint.CLASS -> "$name is not set to a valid class name"
  Constraint.PACKAGE -> "$name is not set to a valid package name"
  Constraint.MODULE -> "$name is not set to a valid module name"
  Constraint.STRING -> {
    val rft = c.toResourceFolderType()
    val resourceNameError = IdeResourceNameValidator.forFilename(rft).getErrorText(value)
    if (resourceNameError == null)
      "Unknown resource name error (name: $name). Constraint $c is violated"
    else
      "$name is not set to a valid resource name: $resourceNameError"
  }
  Constraint.UNIQUE -> "$name must be unique"
  Constraint.EXISTS -> "$name must already exist"
  Constraint.SDK -> "$name must be a path to a Flutter SDK"
  Constraint.PROJECT -> "$name is not set to a valid Dart project name"
}

/**
 * Validate the given value for this parameter and list the constraints that the given value violates.
 * @return All constraints of this parameter that are violated by the proposed value.
 */
@VisibleForTesting
fun StringParameter.validateStringType(
  project: Project?, module: Module?, provider: SourceProvider?, packageName: String?, value: String?, relatedValues: Set<Any> = setOf()
): Collection<Constraint> {
  if (value == null || value.isEmpty()) {
    return if (Constraint.NONEMPTY in constraints) listOf(Constraint.NONEMPTY)
    else listOf()
  }
  val searchScope = if (module != null) GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module) else GlobalSearchScope.EMPTY_SCOPE
  val qualifier = if (packageName != null && !value.contains('.')) "$packageName." else ""
  val fqName = qualifier + value

  fun validateConstraint(c: Constraint): Boolean = when (c) {
    Constraint.NONEMPTY -> value.isEmpty()
    Constraint.CLASS, Constraint.PACKAGE -> !isValidFullyQualifiedJavaIdentifier(fqName)
    Constraint.STRING -> {
      val rft = c.toResourceFolderType()
      IdeResourceNameValidator.forFilename(rft).getErrorText(value) != null
    }
    Constraint.SDK -> !isValidSdkPath(value)
    Constraint.MODULE -> false // may only violate uniqueness
    Constraint.UNIQUE, Constraint.EXISTS -> false // not applicable
    Constraint.PROJECT -> !isValidFlutterProjectName(value)
  }

  fun checkExistence(c: Constraint): Boolean {
    return when (c) {
      Constraint.CLASS -> project != null && existsClassFile(project, searchScope, provider, fqName)
      Constraint.PACKAGE -> project != null && existsPackage(project, provider, value)
      Constraint.MODULE -> project != null && ModuleManager.getInstance(project).findModuleByName(value) != null
      Constraint.SDK -> isValidSdkPath(value)
      Constraint.NONEMPTY, Constraint.STRING -> false
      Constraint.UNIQUE, Constraint.EXISTS -> false // not applicable
      Constraint.PROJECT -> false // TODO(messick) Determine project name existence from class name
    }
  }

  val exists = constraints.any { checkExistence(it) } || value in relatedValues
  val violations = constraints.filter { validateConstraint(it) }
  if (Constraint.UNIQUE in constraints && exists) {
    return violations + listOf(Constraint.UNIQUE)
  }
  if (Constraint.EXISTS in constraints && !exists) {
    return violations + listOf(Constraint.EXISTS)
  }
  return violations
}

/**
 * Returns true if the given stringType is non-unique when it should be.
 */
fun StringParameter.uniquenessSatisfied(
  project: Project?, module: Module?, provider: SourceProvider?, packageName: String?, value: String?, relatedValues: Set<Any>
): Boolean = !validateStringType(project, module, provider, packageName, value, relatedValues).contains(Constraint.UNIQUE)

fun existsResourceFile(module: Module?, resourceType: ResourceType, name: String?): Boolean {
  if (name == null || name.isEmpty() || module == null) {
    return false
  }
  val facet = module.androidFacet ?: return false
  return resourceExists(facet, resourceType, name)
}

fun existsClassFile(
  project: Project?, searchScope: GlobalSearchScope, sourceProvider: SourceProvider?, fullyQualifiedClassName: String
): Boolean {
  if (project == null) {
    return false
  }
  if (sourceProvider == null) {
    return searchScope != GlobalSearchScope.EMPTY_SCOPE && JavaPsiFacade.getInstance(project).findClass(fullyQualifiedClassName,
                                                                                                        searchScope) != null
  }
  val base = fullyQualifiedClassName.replace('.', File.separatorChar)
  return sourceProvider.javaDirectories.any { javaDir ->
    val javaFile = File(javaDir, base + SdkConstants.DOT_JAVA)
    val ktFile = File(javaDir, base + SdkConstants.DOT_KT)
    javaFile.exists() || ktFile.exists()
  }
}

fun Constraint.toResourceFolderType(): ResourceFolderType = when (this) {
  Constraint.STRING -> ResourceFolderType.VALUES
  else -> throw IllegalArgumentException("There is no matching ResourceFolderType for $this constraint")
}

private fun isValidFullyQualifiedJavaIdentifier(value: String) = AndroidUtils.isValidJavaPackageName(value) && value.contains('.')

private fun isValidFlutterProjectName(value: String) = true // TODO(messick) Implement FlutterProjectStep.validateFlutterModuleName()

private fun isValidSdkPath(value: String) = !FlutterSdkUtil.isFlutterSdkHome(value)

private fun existsPackage(project: Project?, sourceProvider: SourceProvider?, packageName: String): Boolean {
  if (project == null) {
    return false
  }
  if (sourceProvider == null) {
    return JavaPsiFacade.getInstance(project).findPackage(packageName) != null
  }
  return sourceProvider.javaDirectories.any {
    val classFile = File(it, packageName.replace('.', File.separatorChar))
    classFile.exists() && classFile.isDirectory
  }
}
