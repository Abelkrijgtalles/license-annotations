import java.util.*

rootProject.name = "license-annotations"
include("licenses", "common")

val licenseDir = file("license_properties/")

licenseDir.listFiles()
    ?.filter { it.extension == "properties" }
    ?.mapNotNull { file ->
        val properties = Properties().apply { load(file.inputStream()) }

        include("licenses:${properties.getProperty("package")}")
        include("licenses:${properties.getProperty("package")}-included")

    }
