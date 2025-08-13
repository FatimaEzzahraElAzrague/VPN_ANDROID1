plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

val ktorVersion = "2.3.12"
val exposedVersion = "0.46.0"
val jedisVersion = "5.1.5"
val hikariVersion = "5.1.0"
val postgresVersion = "42.7.3"
val dotenvVersion = "6.4.1"
val bcryptVersion = "0.10.2"
val jakartaMailVersion = "2.0.1"
val googleApiClientVersion = "2.6.0"

dependencies {
    // Ktor server
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion")

    // Exposed ORM + Hikari + Postgres
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")

    // Redis (Jedis)
    implementation("redis.clients:jedis:$jedisVersion")

    // Env loader
    implementation("io.github.cdimascio:dotenv-kotlin:$dotenvVersion")

    // Password hashing
    implementation("at.favre.lib:bcrypt:$bcryptVersion")
    implementation("de.mkammerer:argon2-jvm:2.11")

    // Email (Jakarta Mail)
    implementation("com.sun.mail:jakarta.mail:$jakartaMailVersion")

    // Google ID token verification
    implementation("com.google.api-client:google-api-client:$googleApiClientVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("io.github.microutils:kotlin-logging:3.0.5")
}

application {
    mainClass.set("com.myapp.backend.ApplicationKt")
}

// Force JVM target/source to 17 so Kotlin 1.9.0 doesn't infer 21
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}

configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin" && requested.name.startsWith("kotlin-stdlib")) {
            useVersion("1.9.24")
            because("Align kotlin-stdlib with Kotlin plugin 1.9.24 to avoid metadata 2.1.0")
        }
    }
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

