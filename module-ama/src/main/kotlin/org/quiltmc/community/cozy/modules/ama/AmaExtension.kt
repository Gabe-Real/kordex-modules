/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.cozy.modules.ama

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createEphemeralFollowup
import dev.kord.core.entity.Message
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.channel.TopGuildChannel
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.rest.builder.message.embed
import dev.kordex.core.checks.anyGuild
import dev.kordex.core.checks.guildFor
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.ephemeralSubCommand
import dev.kordex.core.commands.converters.impl.channel
import dev.kordex.core.commands.converters.impl.optionalChannel
import dev.kordex.core.components.ComponentRegistry
import dev.kordex.core.components.components
import dev.kordex.core.components.ephemeralButton
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.components.forms.widgets.LineTextWidget
import dev.kordex.core.components.forms.widgets.ParagraphTextWidget
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.extensions.event
import dev.kordex.core.i18n.toKey
import dev.kordex.core.i18n.types.Key
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.commands.slash.InitialSlashCommandResponse
import dev.kordex.modules.dev.unsafe.extensions.unsafeSlashCommand
import org.koin.core.component.inject
import org.quiltmc.community.cozy.modules.ama.data.AmaConfig
import org.quiltmc.community.cozy.modules.ama.data.AmaData
import org.quiltmc.community.cozy.modules.ama.data.AmaEmbedConfig
import org.quiltmc.community.cozy.modules.ama.enums.QuestionStatusFlag

public class AmaExtension : Extension() {
	override val name: String = "ama"

	public val amaData: AmaData by inject()

	@OptIn(UnsafeAPI::class)
	override suspend fun setup() {
		ephemeralSlashCommand {
			name = "ama".toKey()
			description = "The command for using AMA".toKey()

			ephemeralSubCommand(::AmaConfigArgs, ::AmaConfigModal) {
				name = "config".toKey()
				description = "Configure your AMA settings".toKey()
				requirePermission(Permission.SendMessages)

				check {
					anyGuild()
					with(amaData) {
						managementChecks()
					}
				}

				var buttonMessage: Message?
				action { modal ->
					val embedConfig = AmaEmbedConfig(modal?.header?.value!!, modal.body.value, modal.image.value)
					buttonMessage = arguments.buttonChannel.asChannelOf<GuildMessageChannel>().createMessage {
						embed {
							title = embedConfig.title
							description = embedConfig.description
							image = embedConfig.imageUrl
						}
					}

					val buttonId = "ama-button.${buttonMessage!!.id}"

					buttonMessage!!.edit {
						val components = components {
							ephemeralButton {
								label = "Ask a question".toKey()
								style = ButtonStyle.Secondary

								id = buttonId
								disabled = true
								action { }
							}
						}
						components.removeAll()
					}

					amaData.setConfig(
						AmaConfig(
							guild!!.id,
							arguments.answerQueueChannel.id,
							arguments.liveChatChannel.id,
							arguments.buttonChannel.id,
							arguments.approvalQueue?.id,
							arguments.flaggedQuestionChannel?.id,
							embedConfig,
							buttonMessage!!.id,
							buttonId,
							false
						)
					)

					respond { content = "Set AMA config" }
				}
			}

			ephemeralSubCommand {
				name = "start".toKey()
				description = "Start the AMA for this server".toKey()
				requirePermission(Permission.SendMessages)

				check {
					anyGuild()
					with(amaData) {
						managementChecks()
					}
				}

				action {
					val config = amaData.getConfig(guild!!.id)

					if (config == null) {
						respond {
							content = "There is no AMA config for this guild! Please run `/ama config` first."
						}
						return@action
					}

					if (config.enabled) {
						respond {
							content = "AMA is already started for this guild"
						}
						return@action
					}

					guild!!.getChannelOf<GuildMessageChannel>(config.buttonChannel).getMessage(config.buttonMessage)
						.edit {
							components?.removeFirst()
							val newComponents = components {
								ephemeralButton {
									label = "Ask a question".toKey()
									style = ButtonStyle.Primary

									id = config.buttonId
									action { }
								}
							}
							newComponents.removeAll()
						}

					amaData.modifyButton(guild!!.id, true)

					respond { content = "AMA Started!" }
				}
			}

			ephemeralSubCommand {
				name = "stop".toKey()
				description = "Stop the AMA for this server".toKey()
				requirePermission(Permission.SendMessages)

				check {
					anyGuild()
					with(amaData) {
						managementChecks()
					}
				}

				action {
					val config = amaData.getConfig(guild!!.id)

					if (config == null) {
						respond {
							content = "There is no AMA config for this guild!"
						}
						return@action
					}

					if (!config.enabled) {
						respond {
							content = "AMA is already stopped for this guild"
						}
						return@action
					}

					guild!!.getChannelOf<GuildMessageChannel>(config.buttonChannel).getMessage(config.buttonMessage)
						.edit {
							components?.removeFirst()
							val newComponents = components {
								ephemeralButton {
									label = "Ask a question".toKey()
									style = ButtonStyle.Secondary

									id = config.buttonId
									disabled = true
									action { }
								}
							}
							newComponents.removeAll()
						}

					amaData.modifyButton(guild!!.id, false)

					respond { content = "AMA Stopped!" }
				}
			}
		}

		event<GuildButtonInteractionCreateEvent> {
			check {
				anyGuild()
				failIfNot { event.interaction.componentId.contains("ama-button.") }
				with(amaData) {
					userChecks()
				}
			}

			action {
				// Start modal creation
				val modalObj = AskModal()
				val componentRegistry: ComponentRegistry by inject()
				var interactionResponse: EphemeralMessageInteractionResponseBehavior? = null

				componentRegistry.register(modalObj)

				event.interaction.modal(modalObj.title.toString(), modalObj.id) {
					modalObj.applyToBuilder(this, getLocale()) // null but too many arguments in public final fun
				}

				modalObj.awaitCompletion { modalSubmitInteraction ->
					interactionResponse = modalSubmitInteraction?.deferEphemeralMessageUpdate()
				}
				// End modal creation

				val config = amaData.getConfig(guildFor(event)!!.id) ?: return@action

				val channelId = config.approvalQueueChannel ?: config.answerQueueChannel
				val channel = guildFor(event)?.getChannelOf<GuildMessageChannel>(channelId)

				val originalInteractionUser = event.interaction.user
				val pkMember = checkPK(originalInteractionUser, modalObj.pkId.value, interactionResponse)
				if (pkMember !is PKResult.Success) {
					return@action
				}

				val embedMessage = channel?.createEmbed {
					questionEmbed(originalInteractionUser, modalObj.question.value, QuestionStatusFlag.NO_FLAG, pkMember.result)
				}

				val answerQueueChannel = guildFor(event)?.getChannelOf<GuildMessageChannel>(config.answerQueueChannel)
				val liveChatChannel = guildFor(event)?.getChannelOf<GuildMessageChannel>(config.liveChatChannel)
				val flaggedQueueChannel = if (config.flaggedQuestionChannel != null) {
					guildFor(event)?.getChannelOf<GuildMessageChannel>(config.flaggedQuestionChannel)
				} else {
					null
				}

				embedMessage?.edit {
					components {
						questionComponents(
							embedMessage,
							originalInteractionUser,
							pkMember.result,
							modalObj.question.value,
							answerQueueChannel,
							liveChatChannel,
							flaggedQueueChannel,
							config.flaggedQuestionChannel
						)
					}
				}

				interactionResponse?.createEphemeralFollowup { content = "Question sent!" }
			}
		}

		unsafeSlashCommand {
			name = "ask".toKey()
			description = "Ask a question for the current AMA".toKey()

			initialResponse = InitialSlashCommandResponse.None

			check {
				anyGuild()
				with(amaData) {
					userChecks()
				}
			}

			action {
				if (amaData.isButtonEnabled(guild!!.id) == false) {
					ackEphemeral()
					respondEphemeral { content = "The AMA is not running!" }
					return@action
				}
				// Start modal creation
				val modalObj = AskModal()

				this@unsafeSlashCommand.componentRegistry.register(modalObj)

				event.interaction.modal(modalObj.title.toString(), modalObj.id) {
					modalObj.applyToBuilder(this, getLocale()) // null but too many arguments in public final fun
				}

				modalObj.awaitCompletion { modalSubmitInteraction ->
					interactionResponse = modalSubmitInteraction?.deferEphemeralMessageUpdate()
				}

				// End modal creation
				val config = amaData.getConfig(guild!!.id) ?: return@action

				val channelId = config.approvalQueueChannel ?: config.answerQueueChannel
				val channel = guild?.getChannelOf<GuildMessageChannel>(channelId)

				val originalInteractionUser = event.interaction.user

				val pkMember = checkPK(originalInteractionUser, modalObj.pkId.value, interactionResponse)
				if (pkMember !is PKResult.Success) {
					return@action
				}

				val embedMessage = channel?.createEmbed {
					questionEmbed(
						originalInteractionUser,
						modalObj.question.value,
						QuestionStatusFlag.NO_FLAG,
						pkMember.result
					)
				}

				val answerQueueChannel = guild?.getChannelOf<GuildMessageChannel>(config.answerQueueChannel)
				val liveChatChannel = guild?.getChannelOf<GuildMessageChannel>(config.liveChatChannel)
				val flaggedQueueChannel = if (config.flaggedQuestionChannel != null) {
					guildFor(event)?.getChannelOf<GuildMessageChannel>(config.flaggedQuestionChannel)
				} else {
					null
				}

				embedMessage?.edit {
					components {
						questionComponents(
							embedMessage,
							originalInteractionUser,
							pkMember.result,
							modalObj.question.value,
							answerQueueChannel,
							liveChatChannel,
							flaggedQueueChannel,
							config.flaggedQuestionChannel
						)
					}
				}

				interactionResponse?.createEphemeralFollowup { content = "Question Sent" }
			}
		}
	}

	public suspend inline fun Channel?.checkPermission(permissions: Permissions): Boolean? {
		this ?: return null
		val topGuildChannel = this.asChannelOfOrNull<TopGuildChannel>() ?: return null
		return topGuildChannel.getEffectivePermissions(kord.selfId).contains(permissions)
	}

	private suspend fun checkPK(
		user: User,
		pkId: String?,
		interactionResponse: MessageInteractionResponseBehavior?
	): PKResult<PKMember?> {
		if (pkId == null) {
			return PKResult.Success(null)
		}

		val result = user.getPluralKitMember(pkId)
		when (result) {
			PKResult.SystemNotFound -> {
				if (pkId.isBlank()) {
					return PKResult.Success(null)
				}

				interactionResponse?.createEphemeralFollowup {
					content = "**Error**: No PK system is associated with your Discord account. Please try " +
						"again, leaving the field blank."
				}
			}
			PKResult.SystemNotAccessible -> {
				if (pkId.isBlank()) {
					return PKResult.Success(null)
				}

				interactionResponse?.createEphemeralFollowup {
					content = "**Error**: The PK system associated with your Discord account is not " +
						"accessible. Please try again, leaving the field blank, or contact a staff member."
				}
			}
			PKResult.MemberNotFound -> interactionResponse?.createEphemeralFollowup {
				content = "**Error**: No PK member was found with the provided information. If you used " +
					"a member's name, please try again with their ID or UUID."
			}
			PKResult.MemberNotFromUser -> interactionResponse?.createEphemeralFollowup {
				content = "**Error**: The PK member you provided is not from your system. Please try " +
					"again with a member from your system, or leave the field blank."
			}
			PKResult.NotPermitted -> interactionResponse?.createEphemeralFollowup {
				content = "**Error**: The PK system or member not accessible. Please try again, leaving the field " +
					"blank, or contact a staff member."
			}
			PKResult.NoFronter -> return PKResult.Success(null)
			is PKResult.Success -> return result
		}

		return result
	}

	public inner class AmaConfigArgs : Arguments() {
		public val answerQueueChannel: Channel by channel {
			name = "answer-queue-channel".toKey()
			description = "The channel for asked questions to queue up in before response".toKey()
			requiredChannelTypes = mutableSetOf(ChannelType.GuildText)

			validate {
				val checkResult = value.checkPermission(Permissions(Permission.ViewChannel, Permission.SendMessages))
				failIf(checkResult == false, "The bot cannot see this channel")
				failIf(checkResult == null, "Cannot find this channel!")
			}
		}

		public val liveChatChannel: Channel by channel {
			name = "live-chat-channel".toKey()
			description = "The channel questions will be sent to when answered by staff".toKey()
			requiredChannelTypes = mutableSetOf(ChannelType.GuildText)

			validate {
				val checkResult = value.checkPermission(Permissions(Permission.ViewChannel, Permission.SendMessages))
				failIf(checkResult == false, "The bot cannot see this channel")
				failIf(checkResult == null, "Cannot find this channel!")
			}
		}

		public val buttonChannel: Channel by channel {
			name = "button-channel".toKey()
			description = "The channel the button for asking questions with is in".toKey()
			requiredChannelTypes = mutableSetOf(ChannelType.GuildText)
			validate {
				val checkResult = value.checkPermission(Permissions(Permission.ViewChannel, Permission.SendMessages))
				failIf(checkResult == false, "The bot cannot see this channel")
				failIf(checkResult == null, "Cannot find this channel!")
			}
		}

		public val approvalQueue: Channel? by optionalChannel {
			name = "approval-queue".toKey()
			description = "The channel for questions to get sent to, for approval".toKey()
			requiredChannelTypes = mutableSetOf(ChannelType.GuildText)
			validate {
				val checkResult = value.checkPermission(Permissions(Permission.ViewChannel, Permission.SendMessages))
				failIf(checkResult == false, "The bot cannot see this channel")
			}
		}

		public val flaggedQuestionChannel: Channel? by optionalChannel {
			name = "flagged-question-channel".toKey()
			description = "The channel for questions flagged by moderators to be sent too".toKey()
			requiredChannelTypes = mutableSetOf(ChannelType.GuildText)
			validate {
				val checkResult = value.checkPermission(Permissions(Permission.ViewChannel, Permission.SendMessages))
				failIf(checkResult == false, "The bot cannot see this channel")
			}
		}
	}

	@Suppress("MagicNumber")
	public inner class AmaConfigModal : ModalForm() {
		public override var title: Key = "Configure your AMA Session".toKey()

		public val header: LineTextWidget = lineText {
			label = "AMA Embed Title".toKey()
			placeholder = "Ask me anything session!".toKey()
			maxLength = 200
			required = true
		}

		public val body: ParagraphTextWidget = paragraphText {
			label = "AMA Embed body".toKey()
			placeholder = "Ask me any question you can think of!".toKey()
			maxLength = 1_800
			required = false
		}

		public val image: LineTextWidget = lineText {
			label = "AMA Embed image".toKey()
			placeholder = "https://i.imgur.com/yRgfdVJ.png".toKey()
			required = false
		}
	}

	public inner class AskModal : ModalForm() {
		override var title: Key = "Ask a question".toKey()

		public val question: ParagraphTextWidget = paragraphText {
			label = "Question".toKey()
			placeholder = "What are you doing today?".toKey()
			required = true
		}

		public val pkId: LineTextWidget = lineText {
			label = "PluralKit Member (Optional)".toKey()
			placeholder = "Either a member name, ID, or UUID".toKey()
			required = false
		}
	}
}
