


plugins {
    kotlin("jvm") version "2.2.0"
}



repositories {
    mavenCentral()
}


dependencies {
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("org.apache.logging.log4j:log4j-core:2.25.0")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.25.0")
    implementation("org.apache.commons:commons-lang3:3.12.0")
}

