/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.logs

import org.quiltmc.community.cozy.modules.logs.data.Log
import org.quiltmc.community.cozy.modules.logs.data.Order
import org.quiltmc.community.cozy.modules.logs.types.LogProcessor

private val SERVER_WATCHDOG_REGEXES = arrayOf(
	"""Description:\s+Watching Server""",
	"""java\.lang\.Error:\s+Watchdog"""
).map { it.toRegex(RegexOption.IGNORE_CASE) }

public class ServerWatchdogProcessor : LogProcessor() {
	override val identifier: String = "server-watchdog"
	override val order: Order = Order.Earlier // so we can exit fast
	override suspend fun process(log: Log) {
		if (SERVER_WATCHDOG_REGEXES.any { it.containsMatchIn(log.content) }) {
			log.addMessage(
				"""
				**This appears to be a server crash caused by the Watchdog.**

				Cozy doesn't currently support server crash logs fully, so some issues may not be detected correctly.

				You can still ask for help, or try checking for common causes like infinite loops or tick stalls in mods.
				""".trimIndent()
			)
			log.hasProblems = true
		}
	}
}
