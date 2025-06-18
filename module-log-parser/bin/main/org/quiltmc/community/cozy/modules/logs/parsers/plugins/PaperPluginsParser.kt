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

// Paper/Spigot/Bukkit plugin patterns
private val PAPER_PLUGIN_PATTERNS = arrayOf(
	// Standard plugin loading messages	"""Loading ([^\s]+) v([\d\w\.\-_]+)""".toRegex(RegexOption.IGNORE_CASE),
	"""Enabling ([^\s]+) v([\d\w\.\-_]+)""".toRegex(RegexOption.IGNORE_CASE),
	"""Plugin ([^\s]+) version ([\d\w\.\-_]+) is enabled""".toRegex(RegexOption.IGNORE_CASE),

	// Plugin list format
	"""Plugins \(\d+\): (.+)""".toRegex(RegexOption.IGNORE_CASE),

	// Individual plugin status
	"""([^\s]+): (Enabled|Disabled)""".toRegex(RegexOption.IGNORE_CASE)
)

public class PaperPluginsParser : LogParser() {
	override val identifier: String = "plugins-paper"
	override val order: Order = Order.Default

	override suspend fun predicate(log: Log, event: Event): Boolean =
		log.getLoaderVersion(LoaderType.Paper) != null ||
		log.getLoaderVersion(LoaderType.Spigot) != null ||
		log.getLoaderVersion(LoaderType.Bukkit) != null
	override suspend fun process(log: Log) {
		val plugins = mutableMapOf<String, Mod>()

		// Parse individual plugin loading messages
		PAPER_PLUGIN_PATTERNS.forEach { pattern ->
			pattern.findAll(log.content).forEach { match ->
				when (pattern.pattern.lowercase()) {
					// Handle plugin list format: "Plugins (5): PluginA, PluginB v1.0, etc."
					"plugins \\(\\d+\\): (.+)" -> {
						val pluginList = match.groupValues[1]
						parsePluginList(pluginList, plugins)
					}
					// Handle individual plugin status
					"([^\\s]+): (enabled|disabled)" -> {
						val pluginName = match.groupValues[1].trim()
						if (!plugins.containsKey(pluginName.lowercase())) {
							    plugins[pluginName.lowercase()] = Mod(
								id = pluginName.lowercase().replace(" ", "_"),
								version = Version("unknown"),
								path = null,
								hash = null,
								type = "plugin"
							)
                        }
					}
					// Handle standard loading messages with version
					else -> {
						val pluginName = match.groupValues[1].trim()
						val pluginVersion = if (match.groupValues.size > 2) {
							match.groupValues[2].trim()
						} else {
							"unknown"
						}
						plugins[pluginName.lowercase()] = Mod(
							id = pluginName.lowercase().replace(" ", "_"),
							version = Version(pluginVersion),
							path = null,
							hash = null,
							type = "plugin"
						)
					}
				}
			}
		}

		// Add all found plugins to the log
		plugins.values.forEach { plugin ->
			log.addMod(plugin)
		}
	}

	private fun parsePluginList(pluginList: String, plugins: MutableMap<String, Mod>) {
		// Parse comma-separated plugin list like "PluginA, PluginB v1.0, PluginC v2.1"
		pluginList.split(",").forEach { pluginEntry ->
			val entry = pluginEntry.trim()
			val versionMatch = """(.+?)\s+v([\d\w\.\-_]+)""".toRegex().find(entry)

			if (versionMatch != null) {
				val pluginName = versionMatch.groupValues[1].trim()
				val pluginVersion = versionMatch.groupValues[2].trim()

				plugins[pluginName.lowercase()] = Mod(
					id = pluginName.lowercase().replace(" ", "_"),
					version = Version(pluginVersion),
					path = null,
					hash = null,
					type = "plugin"
				)
			} else if (entry.isNotBlank()) {
				plugins[entry.lowercase()] = Mod(
					id = entry.lowercase().replace(" ", "_"),
					version = Version("unknown"),
					path = null,
					hash = null,
					type = "plugin"
				)
			}
		}
	}
}
