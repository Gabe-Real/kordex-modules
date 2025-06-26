/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("MagicNumber", "UnderscoresInNumericLiterals")

package dev.gabereal.minecraft

import dev.kord.common.entity.Snowflake
import dev.kordex.core.DISCORD_BLURPLE
import dev.kordex.core.DISCORD_GREEN
import dev.kordex.core.DISCORD_RED
import dev.kordex.core.utils.env
import dev.kordex.core.utils.envOrNull

internal val MINECRAFT_UPDATE_CHANNEL_ID = envOrNull("UPDATE_CHANNEL_ID")
	?.let { Snowflake(it) }
	?: Snowflake(1384066726613876789)

internal val MINECRAFT_UPDATE_PING_ROLE = envOrNull("MINECRAFT_UPDATE_PING_ROLE")
	?.let { Snowflake(it) }
	?: Snowflake(1333526015107928197)




