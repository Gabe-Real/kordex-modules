/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.cozy.modules.logs.parsers

import org.quiltmc.community.cozy.modules.logs.Version
import org.quiltmc.community.cozy.modules.logs.data.LoaderType
import org.quiltmc.community.cozy.modules.logs.data.Log
import org.quiltmc.community.cozy.modules.logs.data.Order
import org.quiltmc.community.cozy.modules.logs.types.LogParser

private val PATTERNS = mapOf(
	"\\| Quilt Loader\\s+\\| quilt_loader\\s+\\| (\\S+).+"
		.toRegex(RegexOption.IGNORE_CASE) to LoaderType.Quilt,  // Quilt mods table

	": Loading .+ with Quilt Loader (\\S+)".toRegex(RegexOption.IGNORE_CASE) to LoaderType.Quilt,
	": Loading .+ with Fabric Loader (\\S+)".toRegex(RegexOption.IGNORE_CASE) to LoaderType.Fabric,

	"--fml.forgeVersion, ([^\\s,]+)".toRegex(RegexOption.IGNORE_CASE) to LoaderType.Forge,
	// Won't show up in a valid log but here anyways
	"""^\s*at\s+net\.minecraftforge\..*""".toRegex(RegexOption.IGNORE_CASE) to LoaderType.Forge,	// Older versions
	"MinecraftForge v([^\\s,]+) Initialized".toRegex(RegexOption.IGNORE_CASE) to LoaderType.Forge,

	// Plugin platforms
	"Starting minecraft server version ([\\d\\.]+)".toRegex(RegexOption.IGNORE_CASE) to LoaderType.Bukkit,
	"This server is running CraftBukkit version ([^\\s]+)".toRegex(RegexOption.IGNORE_CASE) to LoaderType.Bukkit,
	"This server is running Bukkit version ([^\\s]+)".toRegex(RegexOption.IGNORE_CASE) to LoaderType.Bukkit,
	"This server is running Spigot version ([^\\s]+)".toRegex(RegexOption.IGNORE_CASE) to LoaderType.Spigot,
	"This server is running Paper version ([^\\s]+)".toRegex(RegexOption.IGNORE_CASE) to LoaderType.Paper,
	"\\[Server thread/INFO\\]: Starting Minecraft server on .*".toRegex(RegexOption.IGNORE_CASE) to LoaderType.Paper,
	"You are running paper version ([^\\s]+)".toRegex(RegexOption.IGNORE_CASE) to LoaderType.Paper,
	"Starting Velocity ([\\d\\.\\w\\-]+)".toRegex(RegexOption.IGNORE_CASE) to LoaderType.Velocity,
	"Velocity ([\\d\\.\\w\\-]+) is starting up".toRegex(RegexOption.IGNORE_CASE) to LoaderType.Velocity,
	"Starting BungeeCord version ([^\\s]+)".toRegex(RegexOption.IGNORE_CASE) to LoaderType.Bungeecord,
	"BungeeCord version ([^\\s]+) by SpigotMC".toRegex(RegexOption.IGNORE_CASE) to LoaderType.Bungeecord,
	"Starting Waterfall version ([^\\s]+)".toRegex(RegexOption.IGNORE_CASE) to LoaderType.Waterfall,
	"Waterfall version ([^\\s]+) by PaperMC".toRegex(RegexOption.IGNORE_CASE) to LoaderType.Waterfall,
)

public class LoaderParser : LogParser() {
	override val identifier: String = "loader"
	override val order: Order = Order.Earlier

	override suspend fun process(log: Log) {
		for ((pattern, loader) in PATTERNS) {
			val match = pattern.find(log.content)
				?: continue

			log.setLoaderVersion(loader, Version(match.groups[1]!!.value))

			if (loader == LoaderType.Forge) {
					log.addMessage(
						"**It looks like you're using the Forge Loader.** " +
							"Please note that we do not fully support Forge yet but are currently in the works to do so, " +
							"while we can provide semi-accurate logs not everything will work as intended. " +
							"Thank you for understanding."

					)
			}

			return
		}
	}
}
