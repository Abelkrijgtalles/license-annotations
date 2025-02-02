import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import nl.abelkrijgtalles.licenseannotations.GeneratableLicense
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
    java
    `maven-publish`
}


configurations {

    // get pairs of ids and names
    val mapper = ObjectMapper().findAndRegisterModules()
    val autoGeneratedLicenses = Paths.get("raw_licenses", "licenses", "autogenerated").toFile()
    val manualLicenses = Paths.get("raw_licenses", "licenses", "manual").toFile()
    val allJsonFiles =
        (autoGeneratedLicenses.listFiles() ?: emptyArray()) + (manualLicenses.listFiles() ?: emptyArray())

    println(allJsonFiles.size)
    val licenses: List<GeneratableLicense> = allJsonFiles
        .filter { it.extension == "json" }
        .flatMap { mapper.readValue<List<Map<String, Any>>>(it) }
        .mapNotNull { obj ->

            var other_names = ArrayList<String>()
            if (obj["other_names"] != null) {
                (obj["other_names"] as List<Map<String, Any?>>).forEach { name ->
                    other_names = other_names.plus(convertToName(name["name"].toString())) as ArrayList<String>
                }
            }

            GeneratableLicense(
                convertToName(obj["name"].toString()),
                convertToPackage(obj["id"].toString()),
                obj["id"].toString(),
                other_names
            )
        }

    println(
        "Creating projects for all %s licenses (%s projects).".format(
            licenses.size,
            licenses.size * 2
        )
    )

    licenses.forEach { license ->

        generateProjectForLicense(license)
        generateProjectForLicenseWithEmbeddedLicense(license)

    }

    println("\nAll projects generated.")

}

fun convertToName(string: String): String {

    var cleaned = string.lowercase()
    cleaned = cleaned.replace("\\(.*?\\)".toRegex(), "")

    val parts = cleaned.replace("[^a-z0-9]".toRegex(), " ").trim().split("\\s+".toRegex())
    val name = parts.joinToString("") { it.replaceFirstChar { c -> c.uppercaseChar() } }

    return if (name.firstOrNull()?.isLetter() == true) name else "_$name"
}

fun convertToPackage(string: String): String {

    val lower = string.lowercase()
    val cleaned = lower.replace("[^a-z0-9]".toRegex(), "_")
    val packagee = if (cleaned.firstOrNull()?.isLetter() == true) cleaned else "pkg_$cleaned"

    return packagee

}

fun generateProjectForLicense(license: GeneratableLicense) {


    // create directory and source code
    val projectPath = Paths.get(rootDir.toString(), "licenses", license.getPackage())
    val javaSourceCodePath =
        projectPath.resolve("src/main/java/nl/abelkrijgtalles/licenseannotations/${license.getPackage()}")
    val annotationFile = javaSourceCodePath.resolve("${license.name}.java")

    Files.createDirectories(javaSourceCodePath)
    Files.write(
        annotationFile,
        generateSourceCodeFile(license.name, license.`package`).toByteArray(StandardCharsets.UTF_8)
    )

    for (alternativeName in license.alternativeNames) {
        Files.write(
            javaSourceCodePath.resolve("${alternativeName}.java"),
            generateSourceCodeFile(alternativeName, license.`package`).toByteArray(StandardCharsets.UTF_8)
        )
    }

    // create build.gradle.kts
    Files.write(
        projectPath.resolve("build.gradle.kts"), """
            plugins {
                java
            }
                
            dependencies {
            
                implementation(project(":common"))
            
            }
        """.trimIndent().toByteArray(StandardCharsets.UTF_8)
    )

}

fun generateSourceCodeFile(name: String, packagee: String): String {
    return """
        package nl.abelkrijgtalles.licenseannotations.${packagee};

        import nl.abelkrijgtalles.licenseannotations.common.PossiblyEmpty;

        @SuppressWarnings("unused")
        public @interface $name {

            /**
             * @return The name of the original project.
             */
            @PossiblyEmpty
            String project() default "";

            /**
             * @return The source code repository link of the original project.
             */
            @PossiblyEmpty
            String projectSourceCode() default "";

            /**
             * @return The location of the original class.
             */
            @PossiblyEmpty
            String originalLocation() default "";

            /**
             * @return The date when the class was originally copied.
             */
            @PossiblyEmpty
            String firstCopied() default "";

            /**
             * @return When the code was last updated. This could be the original or the edited code, depending on your choice.
             */
            String lastUpdate() default "";

            /**
             * @return Whether only small changes are being made to the file.
             */
            boolean smallChanges() default false;
            
            /**
             * @return Additional information.
             */
            String otherInformation() default "";

        }
        """.trimIndent()
}

fun generateProjectForLicenseWithEmbeddedLicense(license: GeneratableLicense) {

    val projectPath = Paths.get(rootDir.toString(), "licenses", license.getPackage() + "-included")
    val resourcesPath = Paths.get(projectPath.toString(), "src", "main", "resources")

    Files.createDirectories(resourcesPath)

    // copy license
    Files.copy(
        Paths.get(rootDir.toString(), "raw_licenses", "texts", "plain", license.rawId),
        resourcesPath.resolve(license.rawId),
        StandardCopyOption.REPLACE_EXISTING
    )

    // create build.gradle.kts
    Files.write(
        projectPath.resolve("build.gradle.kts"), """
            import java.time.LocalDate
            import java.time.format.DateTimeFormatter
            
            plugins {
                java
            }
                
            dependencies {
                implementation(project(":common"))
            }
        """.trimIndent().toByteArray(StandardCharsets.UTF_8)
    )

}

allprojects.forEach { p ->
    p.apply(plugin = "java")
    p.apply(plugin = "maven-publish")

    p.dependencies {
        if (p != project(":common").dependencyProject) {
            implementation(project(":common"))
        }
    }


    p.publishing {
        publications {
            create<MavenPublication>(project.name) {
                groupId = "nl.abelkrijgtalles"
                artifactId = "license-annotations"
                version = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                description = "A simple annotation library for Java to specify a license."

                pom {
                    name = "License Annotations"
                    description = "A simple annotation library for Java to specify a license."
                    url = "https://github.com/Abelkrijgtalles/LicenseAnnotations"
                    licenses {
                        license {
                            name = "GNU GPLv3"
                            url = "https://github.com/Abelkrijgtalles/LicenseAnnotations/blob/main/LICENSE"
                        }
                    }
                }

                from(components["java"])
            }
        }
    }

}

dependencies {

    // I am to lazy to import them transitive or something like that
    implementation(project(":common"))

    file("licenses").listFiles().forEach { file ->
        if (file.name != "build") {
            implementation(project(":licenses:${file.name}"))
        }
    }

}