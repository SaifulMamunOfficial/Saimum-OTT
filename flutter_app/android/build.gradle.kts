allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

// Fix for isar_flutter_libs: namespace required by newer AGP
subprojects {
    afterEvaluate {
        if (project.hasProperty("android")) {
            val androidExt = project.extensions.findByName("android")
            if (androidExt is com.android.build.gradle.LibraryExtension) {
                if (androidExt.namespace == null) {
                    androidExt.namespace = project.group.toString().ifEmpty { "com.example.${project.name}" }
                }
                androidExt.compileSdk = 34
            }
        }
        project.configurations.all {
            resolutionStrategy {
                force("androidx.core:core:1.12.0")
                force("androidx.core:core-ktx:1.12.0")
            }
        }
    }
}

val newBuildDir: Directory =
    rootProject.layout.buildDirectory
        .dir("../../build")
        .get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir: Directory = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
}
subprojects {
    project.evaluationDependsOn(":app")
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
