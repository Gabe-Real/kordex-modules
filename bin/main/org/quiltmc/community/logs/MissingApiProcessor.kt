/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.logs

import org.quiltmc.community.cozy.modules.logs.data.Log
import org.quiltmc.community.cozy.modules.logs.data.Order
import org.quiltmc.community.cozy.modules.logs.types.LogProcessor

private val MISSING_API_REGEXES = arrayOf(
	// Matches both numbered or named mods with version requirement
	"""(?:(?:\d+ mods)|(?:[\w\s'']+)) requires version \[[^]]+] of (\S+), which is missing!""" +
		"""\s*((?:\n?- <mods>/.*(?:\n|$))*)""",
	// Handles 'any version' requirements
	"""(?:(?:\d+ mods)|(?:[\w\s'']+)) requires any version of (\S+), which is missing!""" +
		"""\s*((?:\n?- <mods>/.*(?:\n|$))*)"""
).map { it.toRegex(RegexOption.IGNORE_CASE) }

public class MissingApiProcessor : LogProcessor() {
	override val identifier: String = "missing-api"
	override val order: Order = Order.Earlier

	override suspend fun process(log: Log) {
		val matches = MISSING_API_REGEXES
			.map { it.findAll(log.content).toList() }
			.flatten()

		if (matches.isEmpty()) return

		val problems = matches.map { match ->
			val api = match.groupValues[1]
			val mods = match.groupValues[2]
				.lines()
				.mapNotNull { line ->
					line.trim().takeIf { it.startsWith("- <mods>/") }?.removePrefix("- <mods>/")?.trim()
				}
				.joinToString("\n") { "- `$it`" }

			"**» Missing API:** `$api`\nAffected mods:\n$mods"
		}

		log.addMessage("**Some mods are missing required APIs or mods:**\n" + problems.joinToString("\n\n"))
		log.hasProblems = true
	}
}
