/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.logs

import dev.kord.core.event.Event
import org.quiltmc.community.cozy.modules.logs.data.LoaderType
import org.quiltmc.community.cozy.modules.logs.data.Log
import org.quiltmc.community.cozy.modules.logs.data.Order
import org.quiltmc.community.cozy.modules.logs.types.LogProcessor

class NonQuiltLoaderProcessor : LogProcessor() {
	override val identifier: String = "non-quilt-loader"
	override val order: Order = Order.Early

	private val pluginPlatforms = setOf(
		LoaderType.Paper,
		LoaderType.Spigot,
		LoaderType.Bukkit,
		LoaderType.Velocity,
		LoaderType.Bungeecord,
		LoaderType.Waterfall
	)

	override suspend fun predicate(log: Log, event: Event): Boolean {
		// Only show this message if:
		// 1. No Quilt loader is detected
		// 2. There are loaders present
		// 3. None of the loaders are plugin platforms (since those are valid non-Quilt platforms)
		val hasQuilt = log.getLoaderVersion(LoaderType.Quilt) != null
		val hasLoaders = log.getLoaders().isNotEmpty()
		val hasPluginPlatform = pluginPlatforms.any { log.getLoaderVersion(it) != null }
		
		return !hasQuilt && hasLoaders && !hasPluginPlatform
	}

	override suspend fun process(log: Log) {
		log.hasProblems = true

		log.addMessage(
			"**You don't appear to be using Quilt:** please double-check that you have Quilt installed!"
		)
	}
}
