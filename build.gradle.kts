import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    id("org.springframework.boot") version "3.5.3" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

group = "ru.mdemidkin"
version = "0.0.1-SNAPSHOT"

allprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "io.spring.dependency-management")

    extensions.configure<DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.0.0")
        }
    }
}

tasks.register("test") {
    dependsOn(subprojects.map { "${it.path}:test" })
    doLast {
        subprojects.forEach { project ->
            println("${project.name}: file://$projectDir/${project.name}/build/reports/tests/test/index.html")
        }
    }
}