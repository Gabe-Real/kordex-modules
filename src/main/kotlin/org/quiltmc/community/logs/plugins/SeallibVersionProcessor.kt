/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.logs.plugins

import org.quiltmc.community.cozy.modules.logs.data.Log
import org.quiltmc.community.cozy.modules.logs.data.Order
import org.quiltmc.community.cozy.modules.logs.types.LogProcessor

private val SEALLIB_VERSION_REGEX =
	"""\[SealLib] Enabling SealLib v([\d.\w\-]+)""".toRegex()

private val POWERGEMS_REQUIRED_REGEX =
	"""\[PowerGems] The plugin SealLib is using the wrong version! Please install version ([\d.\w\-]+)""".toRegex()

public class SeallibVersionProcessor : LogProcessor() {
	override val identifier: String = "plugin_version_mismatch"
	override val order: Order = Order.Earlier

	override suspend fun process(log: Log) {
		val actualSealLibVersion = SEALLIB_VERSION_REGEX.find(log.content)?.groupValues?.get(1)
		val requiredSealLibVersion = POWERGEMS_REQUIRED_REGEX.find(log.content)?.groupValues?.get(1)

		if (
			actualSealLibVersion != null &&
			requiredSealLibVersion != null &&
			actualSealLibVersion != requiredSealLibVersion
		) {
			log.addMessage(
				"**SealLib version mismatch detected:** " +
					"Installed version is `$actualSealLibVersion`, but PowerGems requires `$requiredSealLibVersion`."
			)
			log.hasProblems = true
		}
	}
}
