/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.cozy.modules.logs.parsers

import org.quiltmc.community.cozy.modules.logs.data.Log
import org.quiltmc.community.cozy.modules.logs.data.Order
import org.quiltmc.community.cozy.modules.logs.types.LogParser

public class PluginInformationParser : LogParser() {
	override val identifier: String = "plugin-information-parser"
	override val order: Order = Order.Earlier
	override suspend fun process(log: Log) {
		TODO("Not yet implemented")
	}
}
