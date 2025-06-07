/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.logs

import org.quiltmc.community.cozy.modules.logs.data.Log
import org.quiltmc.community.cozy.modules.logs.data.Order
import org.quiltmc.community.cozy.modules.logs.types.LogProcessor

private val MISSING_MODS_TOML_REGEX = Regex(
	"""Mod file .+?\.jar is missing mods\.toml file""",
	RegexOption.IGNORE_CASE
)

public class MissingModsTomlProcessor : LogProcessor() {
	override val identifier: String = "missing-mods-toml"
	override val order: Order = Order.Default
	override suspend fun process(log: Log) {
		if (MISSING_MODS_TOML_REGEX.containsMatchIn(log.content)) {
			log.addMessage(
				"""
				**One or more mods are missing a `mods.toml` file:**

				This usually happens when a library or mod is not properly built for Forge or is being loaded incorrectly.

				Make sure all mods are compatible with Forge and downloaded from a trusted source like [Modrinth](https://modrinth.com/) or [CurseForge](https://curseforge.com/).
				""".trimIndent()
			)
			log.hasProblems = true
		}
	}
}
