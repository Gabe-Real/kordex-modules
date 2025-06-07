/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.extensions

import dev.kord.rest.builder.message.embed
import dev.kordex.core.DISCORD_BLURPLE
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.commands.application.slash.ephemeralSubCommand
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.i18n.toKey
import org.quiltmc.community.APP_ICON

class InformationExtension : Extension() {
	override val name = "Information"	// Descriptions
	val logparser = "The log parsing system is made of many different files and extensions, " +
		"mainly focusing on parsing and processing the uploaded file." +
		"\n\nFor reference, here is an image showing how the system actually works: " +
		"[Image](https://cdn.discordapp.com/attachments/1377978022526849115/" +
		"1380642800953200791/log-parser-process.png?ex=68449f4e&is=68434dce&" +
		"hm=19326c67aad17378bc137731d1b73ad7b182899ecb9b1a210adfeb484de2898a&)" +
		"\nOtherwise, here is the link to the github repository where everything else " +
		"takes place: [Github](https://github.com/Gabe-Real/Rtuuy-discord/tree/master/module-log-parser)"

	val unsupported = "As we are a plural, inclusive and legitimate community, there are " +
		"some loaders, launchers, mods ect that we may not support. Here are the main ones:" +
		"\n\n**Specific unsupported environments:**" +
		"\n- TLauncher - A cracked minecraft launcher." +
		"\n- PolyMC - A launcher taken over by homophobes, antisemites and infamous bigots." +
		"\n- Forge - There is nothing bad about forge it's just we currently do not provide " +
		"fully blown support on it." +		"\n\n**General supported environments:**" +
		"\n- Alternative Minecraft authentication providers." +
		"\n- Offline minecraft mod usage." +
		"\n- Outdated versions of Prism." +
		"\n\n-# **Please note:** this list does not contain all unsupported environments, " +
		"you may come across other exceptions whilst using this bot."

	override suspend fun setup() {
		ephemeralSlashCommand {
			name = "information".toKey()
			description = "Subcommands displaying information about the bot.".toKey()

			allowInDms = false

			check { anyGuild() }

			ephemeralSubCommand {
				name = "log-parser".toKey()
				description = "Information about the minecraft log parsing system.".toKey()

				action {
					respond {
							embed {
								color = DISCORD_BLURPLE

								title = "Information: Log-parser"
								description = logparser

								thumbnail {
									url = APP_ICON
								}
							}
						}
					}
				}

			ephemeralSubCommand {
				name = "un-supported".toKey()
				description = "Get a list of un-supported loaders, launchers and mods".toKey()

				action {
					respond {
						embed {
							color = DISCORD_BLURPLE

							title = "Information: Un-supported"
							description = unsupported
						}
					}
				}
			}
			}
		}
	}
