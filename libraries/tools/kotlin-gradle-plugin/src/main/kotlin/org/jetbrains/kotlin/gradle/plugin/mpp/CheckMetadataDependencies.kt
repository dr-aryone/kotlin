/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.mpp

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.dsl.multiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.sources.KotlinDependencyScope
import org.jetbrains.kotlin.gradle.plugin.sources.sourceSetMetadataConfigurationByScope
import org.jetbrains.kotlin.gradle.plugin.usesPlatformOf
import org.jetbrains.kotlin.gradle.targets.metadata.isKotlinGranularMetadataEnabled
import org.jetbrains.kotlin.gradle.utils.isGradleVersionAtLeast

open class CheckMetadataDependencies : DefaultTask() {
    @TaskAction
    fun detectAndReportDirectMetadataDependencies() {
        val incorrectDependencies = if (project.multiplatformExtension.isGradleMetadataAvailable) {
            detectIncorrectDependenciesWithModuleMetadata()
        } else {
            detectIncorrectDependenciesWithoutModuleMetadata()
        }

        if (incorrectDependencies.isNotEmpty()) {
            reportIncorrectMetadataDependencies(incorrectDependencies)
        }
    }

    private fun detectIncorrectDependenciesWithoutModuleMetadata(): Set<ResolvedDependency> {
        val configuration = project.configurations.maybeCreate("checkApiMetadataDependencies").apply {
            if (project.isKotlinGranularMetadataEnabled)
                usesPlatformOf(project.multiplatformExtension.metadata())
            isCanBeConsumed = false
        }

        (project.multiplatformExtension.metadata().kotlinComponents.single() as KotlinVariant).usages.forEach { usageContext ->
            usageContext.dependencies.forEach { dependency ->
                if (dependency !is ProjectDependency) {
                    val group = dependency.group
                    val name = dependency.name
                    val version = dependency.version
                    configuration.dependencies.add(
                        project.dependencies.create("$group:$name:$version@module") // request the *.module file
                    )
                }
            }
        }

        val lenientConfiguration = configuration.resolvedConfiguration.lenientConfiguration
        val lenientArtifacts = lenientConfiguration.artifacts

        return lenientConfiguration.firstLevelModuleDependencies
            .filterTo(mutableSetOf()) { resolvedDependency ->
                val artifactOrNull = resolvedDependency.moduleArtifacts.singleOrNull()
                    .takeIf { it in lenientArtifacts } // without this check, `.file` below would fail if there's no *.module file

                val moduleFile = artifactOrNull?.file
                    ?: return@filterTo false

                val moduleJson =
                    moduleFile.bufferedReader().use { bufferedReader -> Gson().fromJson(bufferedReader, JsonObject::class.java) }
                        ?: return@filterTo false

                val variants = moduleJson.get("variants").asJsonArrayOrNull
                    ?: return@filterTo false

                val platformTypesInModule = variants.mapNotNull {
                    it.asJsonObjectOrNull?.get("attributes")?.asJsonObjectOrNull?.get(KotlinPlatformType.attribute.name)?.asStringOrNull
                }.toSet()

                platformTypesInModule.size == 1 && platformTypesInModule.single() == KotlinPlatformType.common.name
            }
    }

    private val JsonElement.asJsonObjectOrNull: JsonObject? get() = if (isJsonObject) asJsonObject else null
    private val JsonElement.asJsonArrayOrNull: JsonArray? get() = if (isJsonArray) asJsonArray else null
    private val JsonElement.asStringOrNull: String? get() = if (isJsonPrimitive) asString else null

    private fun detectIncorrectDependenciesWithModuleMetadata(): Set<ResolvedDependency> = with(project) {
        val publishedSourceSets = if (isKotlinGranularMetadataEnabled) {
            multiplatformExtension.metadata().compilations.map { it.defaultSourceSet }
        } else {
            listOf(kotlinExtension.sourceSets.getByName(KotlinSourceSet.COMMON_MAIN_SOURCE_SET_NAME))
        }

        val apiConfigurations = publishedSourceSets.map { sourceSetMetadataConfigurationByScope(it, KotlinDependencyScope.API_SCOPE) }

        apiConfigurations.flatMapTo(mutableSetOf()) { inspectConfigurationWithModuleMetadata(it) }
    }

    private fun inspectConfigurationWithModuleMetadata(configuration: Configuration): Iterable<ResolvedDependency> {
        val firstLevelResultsByModuleId =
            configuration.incoming.resolutionResult.root.dependencies.filterIsInstance<ResolvedDependencyResult>().associateBy {
                val moduleVersion = it.selected.moduleVersion
                Pair(moduleVersion?.group, moduleVersion?.name)
            }

        return configuration.resolvedConfiguration.lenientConfiguration.firstLevelModuleDependencies.filter { resolvedDependency ->
            val resolutionResult = firstLevelResultsByModuleId[Pair(resolvedDependency.moduleGroup, resolvedDependency.moduleName)]
                ?: return@filter false

            val attributes = resolutionResult.selected.variant.attributes
            val kotlinPlatformTypeAttribute = attributes.keySet().find { it.name == KotlinPlatformType.attribute.name }
                ?: return@filter false

            @Suppress("UnstableApiUsage")
            attributes.getAttribute(kotlinPlatformTypeAttribute) == KotlinPlatformType.common.name &&
                    /**
                     * Is a dependency on the metadata module, as opposed to a dependency on the MPP's root module that is further indirectly
                     * resolved to the metadata module, in which case the module artifacts are empty and there's a single transitive dependency.
                     */
                    resolvedDependency.moduleArtifacts.isNotEmpty() && resolutionResult.selected.id !is ProjectComponentIdentifier
        }
    }

    private fun reportIncorrectMetadataDependencies(resolvedDependencies: Set<ResolvedDependency>) {
        throw InvalidUserDataException(
            "$ERROR_MESSAGE_TITLE\n\n" +

                    resolvedDependencies.joinToString("\n") {
                        "  * ${it.moduleGroup}:${it.moduleName}:${it.moduleVersion} "
                    } +

                    "\n\nThis would interfere with dependency resolution in this project or its consumers " +
                    "and is thus not allowed. \nTo fix this, please " +

                    if (!isGradleVersionAtLeast(5, 3) && !project.multiplatformExtension.isGradleMetadataAvailable) {
                        "update Gradle version to 5.3+ or enable the GRADLE_METADATA feature preview, then "
                    } else {
                        ""
                    } +

                    "replace the dependencies listed above with dependencies on the main modules of the libraries."
        )
    }

    companion object {
        internal const val ERROR_MESSAGE_TITLE =
            "The following Kotlin metadata modules of multiplatform libraries were referenced by direct dependencies:"
    }
}