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

tasks.register("test") {
    dependsOn(subprojects.map { "${it.path}:test" })
    doLast {
        subprojects.forEach { project ->
            println("${project.name}: file://$projectDir/${project.name}/build/reports/tests/test/index.html")
        }
    }
}