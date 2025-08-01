plugins {
	`maven-publish`
	kotlin("jvm") version "2.1.0"
	kotlin("plugin.spring") version "2.1.0"
	id("org.springframework.boot") version "3.4.0"
	id("io.spring.dependency-management") version "1.1.6"
	id("net.researchgate.release") version "3.0.2"
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenLocal()
	mavenCentral()
	maven {
		name = "GitHubPackages"
		url = uri("https://maven.pkg.github.com/bible-game/config")
		credentials {
			username = System.getenv("GITHUB_ACTOR")
			password = System.getenv("GITHUB_TOKEN")
		}
	}
}

dependencies {
	implementation(libs.bundles.config)
	implementation(libs.bundles.core)
	implementation(libs.bundles.data)
	implementation(libs.bundles.integration)
	implementation(libs.bundles.jwt)
	implementation(libs.bundles.kotlin)
	implementation(libs.bundles.spring)
	implementation(libs.bundles.web)
	implementation(libs.bundles.security)
	implementation(libs.bundles.test)
}

tasks.getByName<Jar>("jar") {
	enabled = true
}

release {
	buildTasks.add("publish")
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			from(components["java"])
		}
	}

	repositories {
		maven {
			name = "githubPackages"
			url = uri("https://maven.pkg.github.com/bible-game/common")
			credentials {
				username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
				password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
			}
		}
	}
}

tasks.register("printTagVersion") {
	doLast {
		println(project.version.toString().split("-")[0])
	}
}