/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.cozy.modules.logs.parsers

import dev.kord.core.event.Event
import org.quiltmc.community.cozy.modules.logs.Version
import org.quiltmc.community.cozy.modules.logs.data.LoaderType
import org.quiltmc.community.cozy.modules.logs.data.Log
import org.quiltmc.community.cozy.modules.logs.data.Mod
import org.quiltmc.community.cozy.modules.logs.data.Order
import org.quiltmc.community.cozy.modules.logs.types.LogParser

// Common plugin patterns for different server types
private val PLUGIN_PATTERNS = arrayOf(
	// Paper/Spigot/Bukkit plugin loading
	"""Loading ([^\s]+) v([\d\w\.\-_]+)""".toRegex(RegexOption.IGNORE_CASE),
	"""Enabling ([^\s]+) v([\d\w\.\-_]+)""".toRegex(RegexOption.IGNORE_CASE),
	"""Plugin ([^\s]+) version ([\d\w\.\-_]+) is enabled""".toRegex(RegexOption.IGNORE_CASE),

	// Velocity plugin loading
	"""Loaded plugin ([^\s]+) ([\d\w\.\-_]+)""".toRegex(RegexOption.IGNORE_CASE),
	"""Enabling plugin ([^\s]+) ([\d\w\.\-_]+)""".toRegex(RegexOption.IGNORE_CASE),

	// BungeeCord/Waterfall plugin loading
	"""Enabled plugin ([^\s]+) version ([\d\w\.\-_]+)""".toRegex(RegexOption.IGNORE_CASE),
)

public class PluginInformationParser : LogParser() {
	override val identifier: String = "plugin-information-parser"
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
		val pluginMatches = PLUGIN_PATTERNS
			.flatMap { pattern ->
				pattern.findAll(log.content).map { match ->
					val pluginName = match.groupValues[1].trim()
					val pluginVersion = match.groupValues[2].trim()

					Mod(
						id = pluginName.lowercase().replace(" ", "_"),
						version = Version(pluginVersion),
						path = null, // Plugin path info usually not available in logs
						hash = null, // Plugin hash info usually not available in logs
						type = "plugin"
					)
				}
			}
			.distinctBy { it.id } // Remove duplicates based on plugin ID
			.toList()

		// Add all found plugins to the log
		pluginMatches.forEach { plugin ->
			log.addMod(plugin)
		}
	}
}
