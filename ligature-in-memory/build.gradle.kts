/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("dev.ligature.slonky.kotlin-library-conventions")
}

dependencies {
    api(project(":ligature"))
    testImplementation("io.kotest:kotest-runner-junit5:4.6.0")
    testImplementation(project(":ligature-test-suite"))
}
