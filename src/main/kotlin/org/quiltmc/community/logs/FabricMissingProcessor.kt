/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.logs

import org.quiltmc.community.cozy.modules.logs.data.Log
import org.quiltmc.community.cozy.modules.logs.data.Order
import org.quiltmc.community.cozy.modules.logs.types.LogProcessor

private val FABRIC_MISSING_MODS = arrayOf(
	"""Can't load .*?: requires fabric-.*?""",
).map { it.toRegex(RegexOption.IGNORE_CASE) }

public class FabricMissingProcessor : LogProcessor() {
	override val identifier: String = "fabric-missing"
	override val order: Order = Order.Earlier

	override suspend fun process(log: Log) {
		val matches = FABRIC_MISSING_MODS
			.map { it.findAll(log.content).toList() }
			.flatten()

		if (matches.isEmpty()) {
			return
		}
		val mods = matches
			.flatMap { it.groupValues[1].lines() }
			.mapNotNull { line ->
				line.trim().takeIf { it.startsWith("- <mods>/") }?.removePrefix("- <mods>/")?.trim()
			}
			.toSet()
			.joinToString("\n") { "- `$it`" }

		log.addMessage(
			"**The following mods require Fabric but it is missing!**\n $mods\n\n " +
				"**Download the Fabric Installer:**\n**»** " +
				"[Installation instructions](https://fabricmc.net/use/installer/)\n".trimIndent()
		)
		log.hasProblems = true
	}
}
