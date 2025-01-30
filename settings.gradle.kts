import groovy.json.JsonSlurper
import java.nio.file.Paths

rootProject.name = "license-annotations"
include("licenses", "common")

// submodule check
val rawLicenseDir = file("raw_licenses")
if (rawLicenseDir.listFiles().isEmpty()) {
    error("The submodule hasn't been downloaded yet. Please use `git submodule update --init --recursive` to download it.")
}

val slurper = JsonSlurper()

val jsonFolders =
    listOf(
        Paths.get("raw_licenses", "licenses", "autogenerated").toFile(),
        Paths.get("raw_licenses", "licenses", "manual").toFile()
    )

jsonFolders.forEach { folder ->

    folder.listFiles { file -> file.extension == "json" }?.forEach { json ->

        // some weird chatgpt magic while it's hallucinating. it's just fascinating. my head is rotating. riming is such a sensation.
        val licenses =
            slurper.parseText(json.readText(Charsets.UTF_8))

        if (licenses is List<*>) {
            licenses.forEach { license ->
                if (license is Map<*, *>) {
                    include("licenses:${convertToPackage(license["id"].toString())}")
                    include("licenses:${convertToPackage(license["id"].toString())}-included")
                }
            }
        }

    }

}

fun convertToPackage(string: String): String {

    val lower = string.lowercase()
    val cleaned = lower.replace("[^a-z0-9]".toRegex(), "_")
    val packagee = if (cleaned.firstOrNull()?.isLetter() == true) cleaned else "pkg_$cleaned"

    return packagee

}