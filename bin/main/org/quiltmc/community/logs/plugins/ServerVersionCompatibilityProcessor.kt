/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.logs.plugins

import dev.kord.core.event.Event
import org.quiltmc.community.cozy.modules.logs.data.LoaderType
import org.quiltmc.community.cozy.modules.logs.data.Log
import org.quiltmc.community.cozy.modules.logs.data.Order
import org.quiltmc.community.cozy.modules.logs.types.LogProcessor

// Patterns that indicate server version compatibility issues
private val VERSION_COMPATIBILITY_PATTERNS = arrayOf(
	"""This plugin was compiled for (.+) but you're running (.+)""".toRegex(RegexOption.IGNORE_CASE),
	"""Plugin (.+) requires Minecraft (.+) but server is running (.+)""".toRegex(RegexOption.IGNORE_CASE),
	"""Unsupported server version: (.+)""".toRegex(RegexOption.IGNORE_CASE),
	"""Server version (.+) is not supported""".toRegex(RegexOption.IGNORE_CASE),
	"""This plugin requires a (.+) server""".toRegex(RegexOption.IGNORE_CASE),
	"""Plugin (.+) is not compatible with this server version""".toRegex(RegexOption.IGNORE_CASE)
)

public class ServerVersionCompatibilityProcessor : LogProcessor() {
	override val identifier: String = "server_version_compatibility"
	override val order: Order = Order.Default

	override suspend fun predicate(log: Log, event: Event): Boolean =
		// Only run on plugin server platforms
		log.getLoaderVersion(LoaderType.Paper) != null ||
		log.getLoaderVersion(LoaderType.Spigot) != null ||
		log.getLoaderVersion(LoaderType.Bukkit) != null ||
		log.getLoaderVersion(LoaderType.Velocity) != null ||
		log.getLoaderVersion(LoaderType.Bungeecord) != null ||
		log.getLoaderVersion(LoaderType.Waterfall) != null
	override suspend fun process(log: Log) {
		val compatibilityIssues = mutableListOf<String>()

		VERSION_COMPATIBILITY_PATTERNS.forEach { pattern ->
			pattern.findAll(log.content).forEach { match ->
				compatibilityIssues.add(match.value)
			}
		}

		if (compatibilityIssues.isNotEmpty()) {
			log.addMessage(
				"**Server version compatibility issues detected:**\n" +
				compatibilityIssues.joinToString("\n") { "• $it" } + "\n\n" +
				"**Recommendations:**\n" +
				"• Update your plugins to versions compatible with your server\n" +
				"• Update your server if using outdated plugins that require newer versions\n" +
				"• Check plugin documentation for supported server versions\n" +
				"• Consider using plugin alternatives that support your server version"
			)
			log.hasProblems = true
		}
	}
}
