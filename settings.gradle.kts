/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

pluginManagement {
	repositories {
		google()
		gradlePluginPortal()
	}
}
plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "cozy-modules"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")


include(":module-log-parser")
include("module-mappings")
include("module-minecraft")
include("module-role-sync")
include("module-moderation")
include("module-ama")
include("module-forums")
include("module-tags")
