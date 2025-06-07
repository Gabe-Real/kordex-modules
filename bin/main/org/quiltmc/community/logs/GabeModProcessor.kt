/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.logs

import org.quiltmc.community.cozy.modules.logs.data.Log
import org.quiltmc.community.cozy.modules.logs.data.Order
import org.quiltmc.community.cozy.modules.logs.types.LogProcessor

private val GABE_MODS = mapOf(
	"content" to "Plushie Pals",
)

class GabeModProcessor : LogProcessor() {
	override val identifier: String = "gabe_mod"
	override val order: Order = Order.Early

	override suspend fun process(log: Log) {
		val mods = log.getMods().filter { it.key in GABE_MODS }

		if (mods.isEmpty()) {
			return
		}

		log.addMessage(
			buildString {
				append("Yey, you are using one of Gabe_Real's mods: ")

				appendLine(
					mods
						.map { GABE_MODS[it.key] }
						.toSet()
						.sortedBy { it }
						.joinToString { "**$it**" }
				)

				appendLine()

				append(
					"Thank you for supporting me even more than just using this bot, " +
						"it really does mean a lot 💖"
				)
			}
		)
	}
}
