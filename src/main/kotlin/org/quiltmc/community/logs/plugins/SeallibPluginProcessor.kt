/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.logs.plugins

import org.quiltmc.community.cozy.modules.logs.data.Log
import org.quiltmc.community.cozy.modules.logs.data.Order
import org.quiltmc.community.cozy.modules.logs.types.LogProcessor


private val MISSING_PLUGIN_PATTERNS = arrayOf(
	"""Missing plugin: ([\w\-.]+)""",
	"""Plugin ([\w\-.]+) is (?:required|missing)""",
	"""Could not load plugin ([\w\-.]+): (?:not found|missing dependency)""",
	"""Required plugin ([\w\-.]+) was not found"""
).map { it.toRegex(RegexOption.IGNORE_CASE) }

public class MissingPluginProcessor : LogProcessor() {
	override val identifier: String = "missing_plugins"
	override val order: Order = Order.Earlier

	override suspend fun process(log: Log) {
		val matches = MISSING_PLUGIN_PATTERNS
			.flatMap { it.findAll(log.content).toList() }

		if (matches.isEmpty()) {
			return
		}

		val missingPlugins = matches
			.map { it.groupValues[1].trim() }
			.toSet()
			.joinToString(", ") { "`$it`" }

		log.addMessage("**Missing required plugins detected:** $missingPlugins")
		log.hasProblems = true
	}
}
