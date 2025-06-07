/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.logs

import org.quiltmc.community.cozy.modules.logs.data.Log
import org.quiltmc.community.cozy.modules.logs.data.Order
import org.quiltmc.community.cozy.modules.logs.types.LogProcessor

private val DUPLICATE_MOD = arrayOf(
	"""Duplicate mandatory mod ids \[([^\]]+)]\s*((?:- .+\n?)+)"""
).map { it.toRegex(RegexOption.IGNORE_CASE) }

public class DuplicateModProcessor : LogProcessor() {
	override val identifier: String = "duplicate"
	override val order: Order = Order.Earlier

	override suspend fun process(log: Log) {
		val matches = DUPLICATE_MOD
			.map { it.findAll(log.content).toList() }
			.flatten()

		if (matches.isEmpty()) {
			return
		}

		val mods = matches
			.map { it.groupValues[1] }
			.toSet()
			.joinToString(", ") { "`$it`" }

		log.addMessage("**Found ${matches.size} duplicate mod instances with the following mods:** $mods")
		log.hasProblems = true
	}
}
