// Top-level build file where you can add configuration options common to all sub-projects/modules.
import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.spotless) apply false
}

allprojects {
    afterEvaluate {
        plugins.apply(libs.plugins.spotless.get().pluginId)
        extensions.configure<SpotlessExtension> {
            kotlin {
                target("src/**/*.kt")
                ktlint(libs.ktlint.get().version)
            }
            kotlinGradle {
                ktlint(libs.ktlint.get().version)
            }
        }
    }

    // Configure Java to use our chosen language level. Kotlin will automatically pick this up.
    // See https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
    plugins.withType<JavaBasePlugin>().configureEach {
        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion = JavaLanguageVersion.of(21)
        }
    }

    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions {
            freeCompilerArgs.add("-Xannotation-default-target=param-property")
        }
    }
}

buildscript {
    dependencies {
        // For KGP
        classpath(libs.gradlePlugin.kotlin)
    }
}
