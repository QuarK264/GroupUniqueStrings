plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
dependencies {

}

tasks {
    withType<Jar> {
        manifest {
            attributes["Main-Class"] = "com.example.groupuniquestrings.Main"
        }
        from(sourceSets["main"].allSource)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}