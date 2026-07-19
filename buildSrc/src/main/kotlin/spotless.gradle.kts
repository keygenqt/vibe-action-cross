plugins {
    id("com.diffplug.spotless")
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin {
        target("src/**/*.kt")
        targetExclude("src/**/build/**/*.kt")
        licenseHeaderFile("$rootDir/copyright")
        ktlint("1.6.0").editorConfigOverride(
            mapOf(
                "ktlint_standard_no-wildcard-imports" to "disabled",
                "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
            )
        )
    }
    format("misc") {
        target("**/*.gradle.kts", "**/*.md", "**/.gitignore")
        trimTrailingWhitespace()
        endWithNewline()
    }
}
