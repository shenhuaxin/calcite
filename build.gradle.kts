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

    val hasTests = file("src/test/java").isDirectory || file("src/test/kotlin").isDirectory
    if (hasTests) {
        // Add default tests dependencies
        dependencies {
            val testImplementation by configurations
            val testRuntimeOnly by configurations
            testImplementation("org.junit.jupiter:junit-jupiter-api")
            testImplementation("org.junit.jupiter:junit-jupiter-params")
            testImplementation("org.hamcrest:hamcrest")
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
            if (project.findProperty("junit4")?.toString()?.toBoolean() ?: false) {
                // Allow projects to opt-out of junit dependency, so they can be JUnit5-only
                testImplementation("junit:junit")
                testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
            }
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
//            if (enableMavenLocal) {
                mavenLocal()
//            }
            mavenCentral()
        }
        val sourceSets: SourceSetContainer by project

        apply(plugin = "maven-publish")

//        if (!enableGradleMetadata) {
//            tasks.withType<GenerateModuleMetadata> {
//                enabled = false
//            }
//        }


//                java {
//                    filter.exclude(*javaccGeneratedPatterns + "**/test/java/*.java")
//                    license()
//                    if (!project.props.bool("junit4", default = false)) {
//                        replace("junit5: Test", "org.junit.Test", "org.junit.jupiter.api.Test")
//                        replaceRegex("junit5: Before", "org.junit.Before\\b", "org.junit.jupiter.api.BeforeEach")
//                        replace("junit5: BeforeClass", "org.junit.BeforeClass", "org.junit.jupiter.api.BeforeAll")
//                        replaceRegex("junit5: After", "org.junit.After\\b", "org.junit.jupiter.api.AfterEach")
//                        replace("junit5: AfterClass", "org.junit.AfterClass", "org.junit.jupiter.api.AfterAll")
//                        replace("junit5: Ignore", "org.junit.Ignore", "org.junit.jupiter.api.Disabled")
//                        replaceRegex("junit5: @Before", "@Before\\b", "@BeforeEach")
//                        replace("junit5: @BeforeClass", "@BeforeClass", "@BeforeAll")
//                        replaceRegex("junit5: @After", "@After\\b", "@AfterEach")
//                        replace("junit5: @AfterClass", "@AfterClass", "@AfterAll")
//                        replace("junit5: @Ignore", "@Ignore", "@Disabled")
//                        replace("junit5: Assert.assertThat", "org.junit.Assert.assertThat", "org.hamcrest.MatcherAssert.assertThat")
//                        replace("junit5: Assert.fail", "org.junit.Assert.fail", "org.junit.jupiter.api.Assertions.fail")
//                    }
//                    replaceRegex("side by side comments", "(\n\\s*+[*]*+/\n)(/[/*])", "\$1\n\$2")
//                    importOrder(
//                        "org.apache.calcite.",
//                        "org.apache.",
//                        "au.com.",
//                        "com.",
//                        "io.",
//                        "mondrian.",
//                        "net.",
//                        "org.",
//                        "scala.",
//                        "java",
//                        "",
//                        "static com.",
//                        "static org.apache.calcite.",
//                        "static org.apache.",
//                        "static org.",
//                        "static java",
//                        "static "
//                    )
//                    removeUnusedImports()
//                    indentWithSpaces(2)
//                    replaceRegex("@Override should not be on its own line", "(@Override)\\s{2,}", "\$1 ")
//                    replaceRegex("@Test should not be on its own line", "(@Test)\\s{2,}", "\$1 ")
//                    replaceRegex("Newline in string should be at end of line", """\\n" *\+""", "\\n\"\n  +")
//                    // (?-m) disables multiline, so $ matches the very end of the file rather than end of line
//                    replaceRegex("Remove '// End file.java' trailer", "(?-m)\n// End [^\n]+\\.\\w+\\s*$", "")
//                    replaceRegex("<p> should not be placed a the end of the line", "(?-m)\\s*+<p> *+\n \\* ", "\n *\n * <p>")
//                    // Assume developer copy-pasted the link, and updated text only, so the url is old, and we replace it with the proper one
//                    replaceRegex(">[CALCITE-...] link styles: 1", "<a(?:(?!CALCITE-)[^>])++CALCITE-\\d+[^>]++>\\s*+\\[?(CALCITE-\\d+)\\]?", "<a href=\"https://issues.apache.org/jira/browse/\$1\">[\$1]")
//                    // If the link was crafted manually, ensure it has [CALCITE-...] in the link text
//                    replaceRegex(">[CALCITE-...] link styles: 2", "<a(?:(?!CALCITE-)[^>])++(CALCITE-\\d+)[^>]++>\\s*+\\[?CALCITE-\\d+\\]?", "<a href=\"https://issues.apache.org/jira/browse/\$1\">[\$1]")
//                    custom("((() preventer", 1) { contents: String ->
//                        ParenthesisBalancer.apply(contents)
//                    }
//                }
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
//            withType<Test> {
//                outputs.cacheIf("test results depend on the database configuration, so we souldn't cache it") {
//                    false
//                }
//                useJUnitPlatform {
//                    excludeTags("slow")
//                }
//                testLogging {
//                    exceptionFormat = TestExceptionFormat.FULL
//                    showStandardStreams = true
//                }
//                exclude("**/*Suite*")
//                jvmArgs("-Xmx1536m")
//                jvmArgs("-Djdk.net.URLClassPath.disableClassPathURLCheck=true")
//                // Pass the property to tests
//                fun passProperty(name: String, default: String? = null) {
//                    val value = System.getProperty(name) ?: default
//                    value?.let { systemProperty(name, it) }
//                }
//                passProperty("java.awt.headless")
//                passProperty("junit.jupiter.execution.parallel.enabled", "true")
//                passProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
//                passProperty("junit.jupiter.execution.timeout.default", "5 m")
//                passProperty("user.language", "TR")
//                passProperty("user.country", "tr")
//                val props = System.getProperties()
//                for (e in props.propertyNames() as `java.util`.Enumeration<String>) {
//                    if (e.startsWith("calcite.") || e.startsWith("avatica.")) {
//                        passProperty(e)
//                    }
//                }
//            }
            // Cannot be moved above otherwise configure each will override
            // also the specific configurations below.
            // 删除非必要的慢测试任务


            afterEvaluate {
                // Add default license/notice when missing

            }
        }

        // Note: jars below do not normalize line endings.
        // Those jars, however are not included to source/binary distributions
        // so the normailzation is not that important

//        val testJar by tasks.registering(Jar::class) {
//            from(sourceSets["test"].output)
//            archiveClassifier.set("tests")
//        }
//
//        val testSourcesJar by tasks.registering(Jar::class) {
//            from(sourceSets["test"].allJava)
//            archiveClassifier.set("test-sources")
//        }

//        val sourcesJar by tasks.registering(Jar::class) {
//            from(sourceSets["main"].allJava)
//            archiveClassifier.set("sources")
//        }

//        val javadocJar by tasks.registering(Jar::class) {
//            from(tasks.named(JavaPlugin.JAVADOC_TASK_NAME))
//            archiveClassifier.set("javadoc")
//        }

//        val testClasses by configurations.creating {
//            extendsFrom(configurations["testRuntime"])
//        }

//        val archives by configurations.getting

        // Parenthesis needed to use Project#getArtifacts
//        (artifacts) {
//            testClasses(testJar)
//            archives(sourcesJar)
//            archives(testJar)
//            archives(testSourcesJar)
//        }

        val archivesBaseName = "calcite-$name"
//        setProperty("archivesBaseName", archivesBaseName)

//        configure<PublishingExtension> {
//            if (project.path == ":") {
//                // Do not publish "root" project. Java plugin is applied here for DSL purposes only
//                return@configure
//            }
//            if (!(project.findProperty("nexus.publish")?.toString()?.toBoolean() ?: true)) {
//                // Some of the artifacts do not need to be published
//                return@configure
//            }
//            publications {
//                create<MavenPublication>(project.name) {
//                    artifactId = archivesBaseName
//                    version = rootProject.version.toString()
//                    description = project.description
//                    from(components["java"])
//
//                    if (!skipJavadoc) {
//                        // Eager task creation is required due to
//                        // https://github.com/gradle/gradle/issues/6246
//                        artifact(sourcesJar.get())
//                        artifact(javadocJar.get())
//                    }
//
//                    // Use the resolved versions in pom.xml
//                    // Gradle might have different resolution rules, so we set the versions
//                    // that were used in Gradle build/test.
//                    versionMapping {
//                        usage(Usage.JAVA_RUNTIME) {
//                            fromResolutionResult()
//                        }
//                        usage(Usage.JAVA_API) {
//                            fromResolutionOf("runtimeClasspath")
//                        }
//                    }
//                    pom {
//                        withXml {
//                            val sb = asString()
//                            var s = sb.toString()
//                            // <scope>compile</scope> is Maven default, so delete it
//                            s = s.replace("<scope>compile</scope>", "")
//                            // Cut <dependencyManagement> because all dependencies have the resolved versions
//                            s = s.replace(
//                                Regex(
//                                    "<dependencyManagement>.*?</dependencyManagement>",
//                                    RegexOption.DOT_MATCHES_ALL
//                                ),
//                                ""
//                            )
//                            sb.setLength(0)
//                            sb.append(s)
//                            // Re-format the XML
//                            asNode()
//                        }
//                        name.set(
//                            (project.findProperty("artifact.name") as? String) ?: "Calcite ${project.name.capitalize()}"
//                        )
//                        description.set(project.description ?: "Calcite ${project.name.capitalize()}")
//                        inceptionYear.set("2012")
//                        url.set("https://calcite.apache.org")
//                        licenses {
//                            license {
//                                name.set("The Apache License, Version 2.0")
//                                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
//                                comments.set("A business-friendly OSS license")
//                                distribution.set("repo")
//                            }
//                        }
//                        issueManagement {
//                            system.set("Jira")
//                            url.set("https://issues.apache.org/jira/browse/CALCITE")
//                        }
//                        mailingLists {
//                            mailingList {
//                                name.set("Apache Calcite developers list")
//                                subscribe.set("dev-subscribe@calcite.apache.org")
//                                unsubscribe.set("dev-unsubscribe@calcite.apache.org")
//                                post.set("dev@calcite.apache.org")
//                                archive.set("https://lists.apache.org/list.html?dev@calcite.apache.org")
//                            }
//                        }
//                        scm {
//                            connection.set("scm:git:https://gitbox.apache.org/repos/asf/calcite.git")
//                            developerConnection.set("scm:git:https://gitbox.apache.org/repos/asf/calcite.git")
//                            url.set("https://github.com/apache/calcite")
//                            tag.set("HEAD")
//                        }
//                    }
//                }
//            }
//        }
}
