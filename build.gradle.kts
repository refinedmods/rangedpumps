plugins {
    id("com.refinedmods.refinedarchitect.root")
    id("com.refinedmods.refinedarchitect.neoforge")
}

repositories {
    maven {
        name = "Refined Storage"
        url = uri("https://maven.creeperhost.net")
        content {
            includeGroup("com.refinedmods.refinedstorage")
        }
    }
}

refinedarchitect {
    modId = "rangedpumps"
    neoForge()
    publishing {
        maven = true
        curseForge = "247496"
        modrinth = "ceOkTRlU"
    }
}

group = "com.refinedmods"

base {
    archivesName.set("rangedpumps")
}
