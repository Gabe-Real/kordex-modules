/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.logs.plugins.powergems

import org.quiltmc.community.cozy.modules.logs.data.Log
import org.quiltmc.community.cozy.modules.logs.data.Order
import org.quiltmc.community.cozy.modules.logs.types.LogProcessor

private val POWERGEMS_GEM_DISABLED_REGEX =
	"""\[PowerGems] Player (.+) tried to use disabled gem: ([A-Za-z]+)""".toRegex()

private val POWERGEMS_COOLDOWN_REGEX =
	"""\[PowerGems] Player (.+) on cooldown for ([A-Za-z]+) gem \((\d+)s remaining\)""".toRegex()

private val POWERGEMS_INVALID_GEM_REGEX =
	"""\[PowerGems] Player (.+) has invalid/corrupted gem: (.+)""".toRegex()

private val POWERGEMS_GEM_LEVEL_ERROR_REGEX =
	"""\[PowerGems] Gem level (\d+) exceeds maximum \((\d+)\) for player (.+)""".toRegex()

private val POWERGEMS_REGION_RESTRICTION_REGEX =
	"""\[PowerGems] Player (.+) cannot use gems in region: (.+)""".toRegex()

private val POWERGEMS_PERMISSION_DENIED_REGEX =
	"""\[PowerGems] Player (.+) lacks permission: (.+)""".toRegex()

private val POWERGEMS_INVENTORY_FULL_REGEX =
	"""\[PowerGems] Could not give gem to (.+): inventory full""".toRegex()

private val POWERGEMS_UPGRADE_FAILED_REGEX =
	"""\[PowerGems] Failed to upgrade gem for player (.+): (.+)""".toRegex()

private val POWERGEMS_MULTIPLE_GEMS_REGEX =
	"""\[PowerGems] Player (.+) has multiple gems but only one allowed""".toRegex()

private val POWERGEMS_GEM_DECAY_REGEX =
	"""\[PowerGems] Gem decayed for player (.+): ([A-Za-z]+) gem level (\d+) -> (\d+)""".toRegex()

public class PowerGemsPlayerProcessor : LogProcessor() {
	override val identifier: String = "powergems_player_processor"
	override val order: Order = Order.Earlier

	override suspend fun process(log: Log) {
		// Check for disabled gem usage attempts
		val disabledGemUsage = POWERGEMS_GEM_DISABLED_REGEX.find(log.content)
		if (disabledGemUsage != null) {
			val player = disabledGemUsage.groupValues[1]
			val gemType = disabledGemUsage.groupValues[2]
			log.addMessage(
				"**PowerGems Disabled Gem Usage** \n" +
					"Player `$player` attempted to use disabled gem: `$gemType`\n" +
					"• Enable the gem in PowerGems configuration if intended\n" +
					"• Remove the gem from the player's inventory if permanently disabled\n" +
					"• Check if the gem was temporarily disabled for maintenance"
			)
			log.hasProblems = true
		}

		// Check for cooldown issues (excessive spam)
		val cooldownMatches = POWERGEMS_COOLDOWN_REGEX.findAll(log.content).toList()
		if (cooldownMatches.size > 5) { // Multiple cooldown messages suggest spam
			val players = cooldownMatches.map { it.groupValues[1] }.distinct()
			log.addMessage(
				"**PowerGems Excessive Cooldown Messages** \n" +
					"Multiple cooldown violations detected (${cooldownMatches.size} occurrences)\n" +
					"Players involved: ${players.joinToString(", ")}\n" +
					"• Consider adjusting cooldown times if too restrictive\n" +
					"• Players may be unaware of cooldown mechanics\n" +
					"• Consider adding cooldown notifications or GUI indicators"
			)
		}

		// Check for corrupted/invalid gems
		val invalidGem = POWERGEMS_INVALID_GEM_REGEX.find(log.content)
		if (invalidGem != null) {
			val player = invalidGem.groupValues[1]
			val gemDetails = invalidGem.groupValues[2]
			log.addMessage(
				"**PowerGems Corrupted Gem Detected** \n" +
					"Player `$player` has corrupted gem: $gemDetails\n" +
					"• Remove the corrupted gem from player's inventory\n" +
					"• Enable `attemptFixOldGems` in config to auto-fix\n" +
					"• This may be caused by plugin updates or config changes\n" +
					"• Consider giving the player a replacement gem"
			)
			log.hasProblems = true
		}

		// Check for gem level errors
		val levelError = POWERGEMS_GEM_LEVEL_ERROR_REGEX.find(log.content)
		if (levelError != null) {
			val currentLevel = levelError.groupValues[1]
			val maxLevel = levelError.groupValues[2]
			val player = levelError.groupValues[3]
			log.addMessage(
				"**PowerGems Level Limit Exceeded** \n" +
					"Player `$player` has gem level $currentLevel (max: $maxLevel)\n" +
					"• Adjust `maxGemLevel` in config if higher levels are intended\n" +
					"• Reset player's gem to maximum allowed level\n" +
					"• This may indicate admin commands were used incorrectly"
			)
			log.hasProblems = true
		}

		// Check for region restrictions
		val regionRestriction = POWERGEMS_REGION_RESTRICTION_REGEX.find(log.content)
		if (regionRestriction != null) {
			val player = regionRestriction.groupValues[1]
			val region = regionRestriction.groupValues[2]
			log.addMessage(
				"**PowerGems Region Restriction** \n" +
					"Player `$player` blocked from using gems in region: `$region`\n" +
					"• Check WorldGuard region flags if using WorldGuard integration\n" +
					"• Verify region permissions are configured correctly\n" +
					"• This is working as intended if the region should restrict gem usage"
			)
		}

		// Check for permission issues
		val permissionDenied = POWERGEMS_PERMISSION_DENIED_REGEX.find(log.content)
		if (permissionDenied != null) {
			val player = permissionDenied.groupValues[1]
			val permission = permissionDenied.groupValues[2]
			log.addMessage(
				"**PowerGems Permission Denied** \n" +
					"Player `$player` lacks permission: `$permission`\n" +
					"• Grant the permission through your permission plugin\n" +
					"• Check if the player is in the correct group\n" +
					"• Verify PowerGems permissions are properly configured"
			)
		}

		// Check for inventory full issues
		val inventoryFull = POWERGEMS_INVENTORY_FULL_REGEX.find(log.content)
		if (inventoryFull != null) {
			val player = inventoryFull.groupValues[1]
			log.addMessage(
				"**PowerGems Inventory Full** \n" +
					"Could not give gem to player `$player` - inventory full\n" +
					"• Player needs to make inventory space\n" +
					"• Gem may have been lost and need to be re-issued\n" +
					"• Consider implementing gem delivery queue or mail system"
			)
		}

		// Check for upgrade failures
		val upgradeFailed = POWERGEMS_UPGRADE_FAILED_REGEX.find(log.content)
		if (upgradeFailed != null) {
			val player = upgradeFailed.groupValues[1]
			val reason = upgradeFailed.groupValues[2]
			log.addMessage(
				"**PowerGems Upgrade Failed** \n" +
					"Failed to upgrade gem for player `$player`: $reason\n" +
					"• Check if player has required materials/experience\n" +
					"• Verify upgrade recipes are configured correctly\n" +
					"• Ensure gem is not at maximum level"
			)
		}

		// Check for multiple gems violation
		val multipleGems = POWERGEMS_MULTIPLE_GEMS_REGEX.find(log.content)
		if (multipleGems != null) {
			val player = multipleGems.groupValues[1]
			log.addMessage(
				"**PowerGems Multiple Gems Violation** \n" +
					"Player `$player` has multiple gems but `allowOnlyOneGem` is enabled\n" +
					"• Remove extra gems from player's inventory\n" +
					"• Disable `allowOnlyOneGem` if multiple gems should be allowed\n" +
					"• Consider implementing gem selection/switching system"
			)
			log.hasProblems = true
		}

		// Check for gem decay (informational)
		val gemDecay = POWERGEMS_GEM_DECAY_REGEX.find(log.content)
		if (gemDecay != null) {
			val player = gemDecay.groupValues[1]
			val gemType = gemDecay.groupValues[2]
			val oldLevel = gemDecay.groupValues[3]
			val newLevel = gemDecay.groupValues[4]
			log.addMessage(
				"**PowerGems Gem Decay** \n" +
					"Player `$player`'s $gemType gem decayed from level $oldLevel to $newLevel\n" +
					"• This is normal behavior if gem decay is enabled\n" +
					"• Disable `doGemDecay` to prevent gem degradation\n" +
					"• Player needs to use gems regularly to prevent decay"
			)
		}
	}
}
