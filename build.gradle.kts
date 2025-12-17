/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//import com.github.vlsi.gradle.dsl.configureEach
//import com.github.vlsi.gradle.properties.dsl.lastEditYear
//import com.github.vlsi.gradle.properties.dsl.props
//import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    java
    maven
    publishing
    `maven-publish`
    // Verification
    checkstyle
    calcite.buildext
    // IDE configuration
    id("org.jetbrains.gradle.plugin.idea-ext")
    id("com.github.vlsi.ide")
}

repositories {
    // At least for RAT
    mavenCentral()
}

fun reportsForHumans() = !(System.getenv()["CI"]?.toBoolean() ?: false)

//val lastEditYear by extra(lastEditYear())

// Do not enable spotbugs by default. Execute it only when -Pspotbugs is present
//val skipCheckstyle by props()
//val skipJavadoc by props()
//val enableMavenLocal by props()
//val enableGradleMetadata by props()

//ide {
//    copyrightToAsf()
//    ideaInstructionsUri =
//        uri("https://calcite.apache.org/docs/howto.html#setting-up-intellij-idea")
//    doNotDetectFrameworks("android", "jruby")
//}

val String.v: String get() = rootProject.extra["$this.version"] as String

val buildVersion = "calcite".v

println("Building Apache Calcite $buildVersion")

// 删除非必要的聚合 Javadoc 任务

// 删除非必要的 SqlLine classpath 辅助任务与相关配置

val javaccGeneratedPatterns = arrayOf(
    "org/apache/calcite/jdbc/CalciteDriverVersion.java",
    "**/parser/**/*ParserImpl.*",
    "**/parser/**/*ParserImplConstants.*",
    "**/parser/**/*ParserImplTokenManager.*",
    "**/parser/**/PigletParser.*",
    "**/parser/**/PigletParserConstants.*",
    "**/parser/**/ParseException.*",
    "**/parser/**/SimpleCharStream.*",
    "**/parser/**/Token.*",
    "**/parser/**/TokenMgrError.*",
    "**/org/apache/calcite/runtime/Resources.java",
    "**/parser/**/*ParserTokenManager.*"
)

fun PatternFilterable.excludeJavaCcGenerated() {
    exclude(*javaccGeneratedPatterns)
}

allprojects {
    group = "org.apache.calcite"
    version = buildVersion

//    apply(plugin = "com.github.vlsi.gradle-extensions")

    repositories {
        // RAT and Autostyle dependencies
        mavenCentral()
    }

    val javaUsed = file("src/main/java").isDirectory
    if (javaUsed) {
        apply(plugin = "java-library")
    }

    plugins.withId("java-library") {
        dependencies {
            "implementation"(platform(project(":bom")))
        }
    }



    tasks {
    }
    plugins.withType<JavaPlugin> {
        configure<JavaPluginConvention> {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        repositories {
            mavenLocal()
            mavenCentral()
        }
        val sourceSets: SourceSetContainer by project

        apply(plugin = "maven-publish")


        tasks {
            // Cannot be moved above otherwise configure each will override
            // also the specific configurations below.
            register<Test>("testSlow") {
                group = LifecycleBasePlugin.VERIFICATION_GROUP
                description = "Runs the slow unit tests."
                useJUnitPlatform() {
                    includeTags("slow")
                }
                jvmArgs("-Xmx6g")
            }
        }

        // Note: jars below do not normalize line endings.
        // Those jars, however are not included to source/binary distributions
        // so the normailzation is not that important

        val testJar by tasks.registering(Jar::class) {
            from(sourceSets["test"].output)
            archiveClassifier.set("tests")
        }

        val testSourcesJar by tasks.registering(Jar::class) {
            from(sourceSets["test"].allJava)
            archiveClassifier.set("test-sources")
        }

        val sourcesJar by tasks.registering(Jar::class) {
            from(sourceSets["main"].allJava)
            archiveClassifier.set("sources")
        }


        val archives by configurations.getting

        // Parenthesis needed to use Project#getArtifacts
        (artifacts) {
            archives(sourcesJar)
        }

        val archivesBaseName = "calcite-$name"
        setProperty("archivesBaseName", archivesBaseName)

        configure<PublishingExtension> {
            if (project.path == ":") {
                // Do not publish "root" project. Java plugin is applied here for DSL purposes only
                return@configure
            }

            // 只发布core-rule模块
            if (project.name != "core-rule") {
                return@configure
            }

            // 配置发布仓库（私服）
            repositories {
                maven {
                    // 私服URL地址
                    url = uri("http://nexus.baocloud.cn/content/repositories/releases/")
                }
            }

            publications {
                create<MavenPublication>(project.name) {
                    artifactId = archivesBaseName
                    version = rootProject.version.toString()
                    description = project.description
                    from(components["java"])

                        // Eager task creation is required due to
                        // https://github.com/gradle/gradle/issues/6246
                    artifact(sourcesJar.get())

                    // Use the resolved versions in pom.xml
                    // Gradle might have different resolution rules, so we set the versions
                    // that were used in Gradle build/test.
                    versionMapping {
                        usage(Usage.JAVA_RUNTIME) {
                            fromResolutionResult()
                        }
                        usage(Usage.JAVA_API) {
                            fromResolutionOf("runtimeClasspath")
                        }
                    }
                    pom {
                        withXml {
                            val sb = asString()
                            var s = sb.toString()
                            // <scope>compile</scope> is Maven default, so delete it
                            s = s.replace("<scope>compile</scope>", "")
                            // Cut <dependencyManagement> because all dependencies have the resolved versions
                            s = s.replace(
                                Regex(
                                    "<dependencyManagement>.*?</dependencyManagement>",
                                    RegexOption.DOT_MATCHES_ALL
                                ),
                                ""
                            )
                            sb.setLength(0)
                            sb.append(s)
                            // Re-format the XML
                            asNode()
                        }
                        name.set(
                            (project.findProperty("artifact.name") as? String) ?: "Calcite ${project.name.capitalize()}"
                        )
                    }
                }
            }
        }
    }




    tasks {
        withType<Jar> {
            manifest {
                attributes["Bundle-License"] = "Apache-2.0"
                attributes["Implementation-Title"] = "Apache Calcite"
                attributes["Implementation-Version"] = project.version
                attributes["Specification-Vendor"] = "The Apache Software Foundation"
                attributes["Specification-Version"] = project.version
                attributes["Specification-Title"] = "Apache Calcite"
                attributes["Implementation-Vendor"] = "Apache Software Foundation"
                attributes["Implementation-Vendor-Id"] = "org.apache.calcite"
            }
        }

        withType<JavaCompile> {
            options.encoding = "UTF-8"
            options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Werror"))
        }



        afterEvaluate {
        }
    }


}
