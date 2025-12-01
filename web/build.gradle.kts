plugins {
    kotlin("js") version "1.9.25"
}

kotlin {
    js(IR) {
        browser {
            binaries.executable()
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-js"))
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-browser:1.0.0-pre.688")
}
