///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to you under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//import com.github.vlsi.gradle.crlf.CrLfSpec
//import com.github.vlsi.gradle.crlf.LineEndings
//import com.github.vlsi.gradle.git.FindGitAttributes
//import com.github.vlsi.gradle.git.dsl.gitignore
//import com.github.vlsi.gradle.license.GatherLicenseTask
//import com.github.vlsi.gradle.license.api.SpdxLicense
//import com.github.vlsi.gradle.release.Apache2LicenseRenderer
//import com.github.vlsi.gradle.release.ArtifactType
//import com.github.vlsi.gradle.release.ReleaseExtension
//import com.github.vlsi.gradle.release.ReleaseParams
//import com.github.vlsi.gradle.release.dsl.dependencyLicenses
//import com.github.vlsi.gradle.release.dsl.licensesCopySpec

plugins {
//    id("com.github.vlsi.stage-vote-release")
}



val distributionGroup = "distribution"
val baseFolder = "apache-calcite-${rootProject.version}"



fun CopySpec.excludeLicenseFromSourceRelease() {
    // Source release has "/licenses" folder with licenses for third-party dependencies
    // It is populated by "dependencyLicenses" above,
    // so we ignore the folder when building source releases
    exclude("licenses/**")
    exclude("LICENSE")
}

fun CopySpec.excludeCategoryBLicensedWorksFromSourceRelease() {
    // The source distribution contains "font-awesome:fonts" which is licensed as
    // http://fontawesome.io/license (Font: SIL OFL 1.1, CSS: MIT License).
    //
    // OFL 1.1 is "category B" (see LEGAL-112).
    //
    // According to
    // https://www.apache.org/legal/resolved.html#binary-only-inclusion-condition,
    // the source code can not include Category B licensed works.

    // We need to remove "web and desktop font files".
    exclude("site/fonts/**")
}
