/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.cozy.modules.logs.parsers.plugins

import dev.kord.core.event.Event
import org.quiltmc.community.cozy.modules.logs.Version
import org.quiltmc.community.cozy.modules.logs.data.LoaderType
import org.quiltmc.community.cozy.modules.logs.data.Log
import org.quiltmc.community.cozy.modules.logs.data.Mod
import org.quiltmc.community.cozy.modules.logs.data.Order
import org.quiltmc.community.cozy.modules.logs.types.LogParser

// BungeeCord/Waterfall plugin patterns
private val BUNGEE_PLUGIN_PATTERNS = arrayOf(
	// Plugin loading messages	"""Enabled plugin ([^\s]+) version ([\d\w\.\-_]+)""".toRegex(RegexOption.IGNORE_CASE),
	"""Loading plugin ([^\s]+) v([\d\w\.\-_]+)""".toRegex(RegexOption.IGNORE_CASE),
	"""Successfully loaded plugin ([^\s]+) ([\d\w\.\-_]+)""".toRegex(RegexOption.IGNORE_CASE),

	// Plugin startup messages
	"""Plugin ([^\s]+) has been (enabled|started)""".toRegex(RegexOption.IGNORE_CASE),

	// Plugin discovery messages
	"""Found plugin: ([^\s]+) version ([\d\w\.\-_]+)""".toRegex(RegexOption.IGNORE_CASE)
)

public class BungeeCordPluginsParser : LogParser() {
	override val identifier: String = "plugins-bungeecord"
	override val order: Order = Order.Default

	override suspend fun predicate(log: Log, event: Event): Boolean =
		log.getLoaderVersion(LoaderType.Bungeecord) != null ||
		log.getLoaderVersion(LoaderType.Waterfall) != null
	override suspend fun process(log: Log) {
		val plugins = mutableMapOf<String, Mod>()
		BUNGEE_PLUGIN_PATTERNS.forEach { pattern ->
			pattern.findAll(log.content).forEach { match ->
				val pluginName = match.groupValues[1].trim()
				val pluginVersion = if (match.groupValues.size > 2 &&
					!match.groupValues[2].matches("enabled|started".toRegex(RegexOption.IGNORE_CASE))
				) {
					match.groupValues[2].trim()
				} else {
					"unknown"
				}

				plugins[pluginName.lowercase()] = Mod(
					id = pluginName.lowercase().replace(" ", "_"),
					version = Version(pluginVersion),
					path = null,
					hash = null,
					type = "bungeecord-plugin"
				)
			}
		}

		// Add all found plugins to the log
		plugins.values.forEach { plugin ->
			log.addMod(plugin)
		}
	}
}
