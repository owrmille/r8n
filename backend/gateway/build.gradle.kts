plugins {
	kotlin("jvm")
	kotlin("plugin.spring")
	id("org.springframework.boot")
	id("io.spring.dependency-management")
}

group = "com.r8n.backend"
version = "0.0.1-SNAPSHOT"
description = "API gateway"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
	withSourcesJar()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
	
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	jvmToolchain(21)
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
