import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import nl.abelkrijgtalles.licenseannotations.GeneratableLicense
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

configurations {

    // loading licenses
    val licenseDir = file("license_properties/")
    println(
        "Creating projects for all %s licenses (%s projects).".format(
            licenseDir.listFiles().filter { it.extension == "properties" }.size,
            licenseDir.listFiles().filter { it.extension == "properties" }.size * 2
        )
    )

    var licenses = emptySet<GeneratableLicense>()
    licenseDir.listFiles()
        ?.filter { it.extension == "properties" }
        ?.mapNotNull { file ->
            val properties = Properties().apply { load(file.inputStream()) }

            licenses = licenses.plus(
                GeneratableLicense(
                    properties.getProperty("name"),
                    properties.getProperty("package")
                )
            )

        }

    licenses.forEach { license ->

        generateProjectForLicense(license)
        generateProjectForLicenseWithEmbeddedLicense(license)

    }

    println("\nAll projects generated.")

}

fun generateProjectForLicense(license: GeneratableLicense) {

    val source = """
        package nl.abelkrijgtalles.licenseannotations.${license.`package`};

        import nl.abelkrijgtalles.licenseannotations.common.PossiblyEmpty;

        @SuppressWarnings("unused")
        public @interface ${license.name}License {

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
             * @return The location of the original class as a canonical name.
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

        }
        """.trimIndent()

    // create directory and source code

    val projectPath = Paths.get(rootDir.toString(), "licenses", license.getPackage())
    val javaSourceCodePath =
        projectPath.resolve("src/main/java/nl/abelkrijgtalles/licenseannotations/${license.getPackage()}")
    val annotationFile = javaSourceCodePath.resolve("${license.name}License.java")

    Files.createDirectories(javaSourceCodePath)
    Files.write(
        annotationFile,
        source.toByteArray(StandardCharsets.UTF_8)
    )

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

fun generateProjectForLicenseWithEmbeddedLicense(license: GeneratableLicense) {

    val projectPath = Paths.get(rootDir.toString(), "licenses", license.getPackage() + "-included")
    val resourcesPath = Paths.get(projectPath.toString(), "src", "main", "resources")

    Files.createDirectories(resourcesPath)

    // copy license
    Files.copy(
        Paths.get(rootDir.toString(), "raw_licenses", license.name),
        resourcesPath.resolve(license.name),
        StandardCopyOption.REPLACE_EXISTING
    )

    // create build.gradle.kts
    Files.write(
        projectPath.resolve("build.gradle.kts"), """
            plugins {
                java
            }
                
            dependencies {
            
                implementation(project(":licenses:${license.`package`}"))
            
            }
        """.trimIndent().toByteArray(StandardCharsets.UTF_8)
    )

}

allprojects.forEach { p ->
    p.apply(plugin = "java")
    p.apply(plugin = "com.github.johnrengelman.shadow")

    p.tasks.build {
        dependsOn("shadowJar")
    }

    p.tasks {
        named<ShadowJar>("shadowJar") {
            configurations = listOf(project.configurations["compileClasspath"])
        }
    }

}

dependencies {

    // I am to lazy to import them transitive or something like that
    implementation(project(":common"))

    val licenseDir = file("license_properties/")

    licenseDir.listFiles()
        ?.filter { it.extension == "properties" }
        ?.mapNotNull { file ->
            val properties = Properties().apply { load(file.inputStream()) }

            implementation(project(":licenses:${properties.getProperty("package")}"))
            implementation(project(":licenses:${properties.getProperty("package")}-included"))

        }

}