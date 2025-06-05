/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:OptIn(PrivilegedIntent::class)

package org.quiltmc.community

import dev.kord.gateway.ALL
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.checks.guildFor
import dev.kordex.core.utils.envOrNull
import dev.kordex.modules.func.phishing.DetectionAction
import dev.kordex.modules.func.phishing.extPhishing
import dev.kordex.modules.pluralkit.extPluralKit
import org.quiltmc.community.cozy.modules.logs.extLogParser
import org.quiltmc.community.cozy.modules.logs.processors.PiracyProcessor
import org.quiltmc.community.cozy.modules.logs.processors.ProblematicLauncherProcessor

val MODE = envOrNull("MODE")?.lowercase() ?: "quilt"
val ENVIRONMENT = envOrNull("ENVIRONMENT")?.lowercase() ?: "production"

suspend fun setupCollab() = ExtensibleBot(DISCORD_TOKEN) {
	common()
	database()

	about {
		addGeneral(
			"Cozy: Collab",

			"Quilt's Discord bot, Community Collab edition."
		)
	}

	extensions {
		sentry {
			distribution = "collab"
		}
	}
}

suspend fun setupDev() = ExtensibleBot(DISCORD_TOKEN) {
	common()
	database()

	about {
		addGeneral(
			"Cozy: Dev Tools",

			"Quilt's Discord bot, Dev Tools edition.\n\n" +
				"Once provided mappings commands, but you should use the Allium Discord bot or " +
				"[Linkie Web](https://linkie.shedaniel.dev/) going forward."
		)
	}

	extensions {
		sentry {
			distribution = "dev"
		}
	}
}

suspend fun setupQuilt() = ExtensibleBot(DISCORD_TOKEN) {
	common()
	database(true)
	settings()

	about {
		addGeneral(
			"Cozy: Community",

			"Quilt's Discord bot, Community edition.\n\n" +
				"Provides a ton of commands and other utilities, to help staff with moderation and provide users " +
				"with day-to-day features on the main Discord server."
		)
	}

	chatCommands {
		defaultPrefix = "%"
		enabled = true
	}

	intents {
		+Intents.ALL
	}

	members {
		all()

		fillPresences = true
	}

	extensions {
		extPluralKit()

		extLogParser {
			// Bundled non-default processors
			processor(PiracyProcessor())
			processor(ProblematicLauncherProcessor())

			globalPredicate { event ->
				val guild = guildFor(event)

				guild?.id != COLLAB_GUILD
			}
		}

		help {
			enableBundledExtension = true
		}

		extPhishing {
			detectionAction = DetectionAction.Kick
			logChannelName = "cozy-logs"
			requiredCommandPermission = null
		}

		sentry {
			distribution = "community"
		}
	}
}

suspend fun setupShowcase() = ExtensibleBot(DISCORD_TOKEN) {
	common()
	database()
	settings()

	about {
		addGeneral(
			"Cozy: Showcase",

			"Quilt's Discord bot, Showcase edition.\n\n" +
				"This bot is currently in development, but someday we hope it'll let you post in the showcase " +
				"channels from your project servers."
		)
	}

	extensions {
		sentry {
			distribution = "showcase"
		}
	}
}

suspend fun main() {
	val bot = when (MODE) {
		"collab" -> setupCollab()
		"dev" -> setupDev()
		"quilt" -> setupQuilt()
		"showcase" -> setupShowcase()

		else -> error("Invalid mode: $MODE")
	}

	bot.start()
}
