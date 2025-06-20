/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.logs.plugins.powergems.processors

import org.quiltmc.community.cozy.modules.logs.data.Log
import org.quiltmc.community.cozy.modules.logs.data.Order
import org.quiltmc.community.cozy.modules.logs.types.LogProcessor

private val SEALLIB_MISSING_REGEX =
	"""\[PowerGems] The plugin SealLib \(version ([\d.\w\-]+)\) is required for this plugin to work. Please install it.""".toRegex() // mainly paper (hopefully everything)

public class MissingSeallibProcessor : LogProcessor() {
	override val identifier: String = "missing_seallib_processor"
	override val order: Order = Order.Earlier

	override suspend fun process(log: Log) {
		val seallibMissingVersion = SEALLIB_MISSING_REGEX.find(log.content)?.groupValues?.get(1)

		if (
			seallibMissingVersion != null
		) {
			log.addMessage(
				"**Powergems requires Seallib version `$seallibMissingVersion`** " +
					"\nYou can install the exact version needed [here](https://modrinth.com/plugin/seallib/version/$seallibMissingVersion)."
			)
			log.hasProblems = true
		}
	}
}
