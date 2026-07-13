dependencyResolutionManagement {
    repositories {
        maven {
            name = "Refined Architect"
            url = uri("https://maven.creeperhost.net")
            content {
                includeGroupAndSubgroups("com.refinedmods.refinedarchitect")
            }
        }
    }
}

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            name = "Refined Architect"
            url = uri("https://maven.creeperhost.net")
            content {
                includeGroupAndSubgroups("com.refinedmods.refinedarchitect")
            }
        }
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
    }
    val refinedarchitectVersion: String by settings
    plugins {
        id("com.refinedmods.refinedarchitect.root").version(refinedarchitectVersion)
        id("com.refinedmods.refinedarchitect.neoforge").version(refinedarchitectVersion)
    }
}
