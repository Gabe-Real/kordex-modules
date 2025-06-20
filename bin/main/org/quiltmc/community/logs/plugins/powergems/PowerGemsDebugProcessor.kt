/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.logs.plugins.powergems

import org.quiltmc.community.cozy.modules.logs.data.Log
import org.quiltmc.community.cozy.modules.logs.data.Order
import org.quiltmc.community.cozy.modules.logs.types.LogProcessor

private val POWERGEMS_DEBUG_EXCEPTION_REGEX =
	"""\[SealUtils] Exception triggered by ([a-zA-Z0-9.]+)""".toRegex()

private val POWERGEMS_FAKE_EXCEPTION_REGEX =
	"""\[SealUtils] The exception message is This is a fake exception for debugging purposes\.""".toRegex()

private val POWERGEMS_SEALUTILS_ERROR_REGEX =
	"""\[SealUtils] The error message is ([A-Z_]+)""".toRegex()

private val POWERGEMS_EXCEPTION_MESSAGE_REGEX =
	"""\[SealUtils] The exception message is (.+)""".toRegex()

private val POWERGEMS_STACKTRACE_REGEX =
	"""\[SealUtils] The stacktrace and all of its details known are as follows:""".toRegex()

private val POWERGEMS_VERSION_REGEX =
	"""PowerGems-([0-9.]+(?:-[A-Z0-9]+)?)\.jar//""".toRegex()

private val SEALLIB_VERSION_REGEX =
	"""SealLib \(([0-9.]+(?:-[A-Z0-9]+)?)\)""".toRegex()

private val POWERGEMS_CONFIG_DUMP_REGEX =
	"""\[SealUtils] Dump from: ([a-zA-Z]+) -> ([a-zA-Z]+): (.+)""".toRegex()

private val POWERGEMS_MANAGER_DUMP_REGEX =
	"""\[SealUtils] Dump from: ([a-zA-Z]+) -> ([a-zA-Z]+): (.+)""".toRegex()

private val POWERGEMS_PLUGIN_VERSION_REGEX =
	"""\[PowerGems] PowerGems v([0-9.]+(?:-[A-Z0-9]+)?)""".toRegex()

private val POWERGEMS_COMMAND_ERROR_REGEX =
	"""\[PowerGems] Loading server plugin PowerGems v([0-9.]+(?:-[A-Z0-9]+)?)""".toRegex()

private val POWERGEMS_VERSION_REQUIREMENT_REGEX =
	"""Powergems requires Seallib version ([0-9.]+(?:\.[0-9]+)*)""".toRegex()

private val POWERGEMS_PLUGIN_DISABLED_REGEX =
	"""PowerGems Plugin Disabled""".toRegex()

// Additional error patterns based on source code analysis
private val POWERGEMS_DEPENDENCY_ERROR_REGEX =
	"""The plugin ([A-Za-z]+) \(version ([0-9.]+)\) is required for this plugin to work""".toRegex()

private val POWERGEMS_VERSION_MISMATCH_REGEX =
	"""The plugin ([A-Za-z]+) is using the wrong version! Please install version ([0-9.]+)""".toRegex()

private val POWERGEMS_RECIPE_ERROR_REGEX =
	"""\[PowerGems] (RECIPE_REGISTER_CRAFT|RECIPE_REGISTER_UPGRADE)""".toRegex()

private val POWERGEMS_I18N_ERROR_REGEX =
	"""Failed to set bundle|FAILED_SET_BUNDLE""".toRegex()

private val POWERGEMS_WORLDGUARD_ERROR_REGEX =
	"""ATTEMPT_REGISTER_WG_FLAG_FAILED|FLAG_REGISTERING_FAILED""".toRegex()

private val POWERGEMS_GEM_CREATION_ERROR_REGEX =
	"""Invalid gem (name|ID)|Invalid gem name in gemMaterials\.yml""".toRegex()

private val POWERGEMS_CONFIG_ERROR_REGEX =
	"""CRAFT_INGREDIENTS_NOT_FOUND|SHAPE_NOT_FOUND_KEY|INGREDIENTS_NOT_FOUND_KEY""".toRegex()

private val POWERGEMS_COMMAND_MESSAGE_REGEX =
	"""(GEM_DISABLED|NO_PERMISSION|NOT_PLAYER|INVALID_SUBCOMMAND|ON_COOLDOWN_GEMS|CANNOT_MOVE_GEMS|CANNOT_PLACE_GEMS_IN_CONTAINERS|INVENTORY_FULL)""".toRegex()

private val POWERGEMS_UPDATE_ERROR_REGEX =
	"""UPDATE_CHECK_(STARTED|COMPLETED)|Update test failed""".toRegex()

public class PowerGemsDebugProcessor : LogProcessor() {
	override val identifier: String = "powergems_debug_processor"
	override val order: Order = Order.Earlier
	override suspend fun process(log: Log) {
		val debugException = POWERGEMS_DEBUG_EXCEPTION_REGEX.find(log.content)
		val fakeException = POWERGEMS_FAKE_EXCEPTION_REGEX.find(log.content)
		val errorMessage = POWERGEMS_SEALUTILS_ERROR_REGEX.find(log.content)?.groupValues?.get(1)
		val exceptionMessage = POWERGEMS_EXCEPTION_MESSAGE_REGEX.find(log.content)?.groupValues?.get(1)
		val hasStacktrace = POWERGEMS_STACKTRACE_REGEX.find(log.content) != null
		val powerGemsVersion = POWERGEMS_VERSION_REGEX.find(log.content)?.groupValues?.get(1)
			?: POWERGEMS_PLUGIN_VERSION_REGEX.find(log.content)?.groupValues?.get(1)
		val sealLibVersion = SEALLIB_VERSION_REGEX.find(log.content)?.groupValues?.get(1)
		val configDumps = POWERGEMS_CONFIG_DUMP_REGEX.findAll(log.content).toList()
		val hasVersionRequirementMessage = POWERGEMS_VERSION_REQUIREMENT_REGEX.find(log.content) != null
		val isPluginDisabled = POWERGEMS_PLUGIN_DISABLED_REGEX.find(log.content) != null
		
		// Check for additional error types
		val dependencyError = POWERGEMS_DEPENDENCY_ERROR_REGEX.find(log.content)
		val versionMismatch = POWERGEMS_VERSION_MISMATCH_REGEX.find(log.content)
		val recipeError = POWERGEMS_RECIPE_ERROR_REGEX.find(log.content)
		val i18nError = POWERGEMS_I18N_ERROR_REGEX.find(log.content)
		val worldGuardError = POWERGEMS_WORLDGUARD_ERROR_REGEX.find(log.content)
		val gemCreationError = POWERGEMS_GEM_CREATION_ERROR_REGEX.find(log.content)
		val configError = POWERGEMS_CONFIG_ERROR_REGEX.find(log.content)
		val commandMessage = POWERGEMS_COMMAND_MESSAGE_REGEX.find(log.content)
		val updateError = POWERGEMS_UPDATE_ERROR_REGEX.find(log.content)
		
		// Handle fake debug exceptions
		if (fakeException != null && errorMessage == "FAKE_EXCEPTION") {
			log.addMessage(
				"**PowerGems Debug Command Detected** \n" +
					"This appears to be a fake exception generated by PowerGems' debug command. " +
					"This is not a real error and can be safely ignored. " +
					"If you're experiencing actual issues, please run the debug command to help identify the problem."
			)
			return // Don't mark as problem since this is intentional
		}		// Handle real PowerGems exceptions
		if (debugException != null && fakeException == null) {
			val exceptionClass = debugException.groupValues[1]
			val messageBuilder = StringBuilder("**PowerGems Exception Detected** \n")
			
			// Only show version information if the log doesn't already contain version requirement messages
			if (!hasVersionRequirementMessage) {
				powerGemsVersion?.let { messageBuilder.append("PowerGems version: `$it`\n") }
				sealLibVersion?.let { messageBuilder.append("SealLib version: `$it`\n") }
			}
			
			messageBuilder.append("Exception in class: `$exceptionClass`")
					errorMessage?.let { 
				messageBuilder.append("\nError type: `$it`")
				
				// Provide specific help for common error types
				when (it) {
					"GIVE_GEM_COMMAND" -> {
						messageBuilder.append("\n\n**Common Cause:** This often happens when using the `/givegem` command without proper arguments.")
						messageBuilder.append("\n**Solution:** Use the command like `/givegem <player> <gemtype> [level]`")
						messageBuilder.append("\nExample: `/givegem Steve fire 3`")
					}
					"INVALID_GEM_TYPE" -> {
						messageBuilder.append("\n\n**Common Cause:** Invalid gem type specified.")
						messageBuilder.append("\n**Solution:** Use valid gem types: Fire, Water, Earth, Air, Lightning, Ice, Healing, Strength, Iron, Sand")
					}
					"FAKE_EXCEPTION" -> {
						messageBuilder.append("\n\n**Note:** This is a debug command, not a real error.")
					}
					"RECIPE_REGISTER_CRAFT", "RECIPE_REGISTER_UPGRADE" -> {
						messageBuilder.append("\n\n**Common Cause:** Recipe registration failed due to invalid ingredients or malformed recipe config.")
						messageBuilder.append("\n**Solution:** Check recipes.yml for syntax errors or delete it to regenerate defaults")
					}					"FAILED_SET_BUNDLE", "FAIL_SET_BUNDLE" -> {
						messageBuilder.append("\n\n**Common Cause:** Localization bundle failed to load.")
						messageBuilder.append("\n**Solution:** Check language/country codes in config or reinstall PowerGems")
					}
					"FLAG_REGISTERING_FAILED" -> {
						messageBuilder.append("\n\n**Common Cause:** WorldGuard flag registration failed.")
						messageBuilder.append("\n**Solution:** Check WorldGuard compatibility or disable WorldGuard support")
					}
					"DUMPING_CLASSES" -> {
						messageBuilder.append("\n\n**Note:** This is a debug dump command, showing all class information.")
					}
					"CREATE_DEFAULT_EFFECT_SETTINGS", "CREATE_DEFAULT_LEVEL_SETTINGS" -> {
						messageBuilder.append("\n\n**Common Cause:** Invalid gem ID during configuration setup.")
						messageBuilder.append("\n**Solution:** This usually indicates a plugin bug or corrupted installation")
					}
					"FAIL_REGISTER_GEM_CLASS" -> {
						messageBuilder.append("\n\n**Common Cause:** Failed to register a custom gem class from an addon.")
						messageBuilder.append("\n**Solution:** Check addon compatibility and gem class implementation")
					}
				}
			}
			
			exceptionMessage?.let { 
				messageBuilder.append("\nException message: `$it`")
				
				// Handle specific exception messages
				if (it.contains("Index") && it.contains("out of bounds")) {
					messageBuilder.append("\n\n**Common Cause:** This is likely caused by missing command arguments or empty lists.")
					messageBuilder.append("\n**Solution:** Check that all required parameters are provided when using PowerGems commands.")
				}
			}
			
			if (hasStacktrace) {
				messageBuilder.append("\n\nFull stack trace and debug information is included above.")
			}
			
			log.addMessage(messageBuilder.toString())
			log.hasProblems = true		}
		
		// Handle plugin disabled scenarios - only add context if no other diagnostic info was provided
		if (isPluginDisabled && debugException == null && hasVersionRequirementMessage) {
			// The log already shows the version requirement message, so just add helpful next steps
			log.addMessage(
				"**PowerGems Troubleshooting** \n" +
					"PowerGems has been disabled due to a dependency issue. " +
					"To resolve this:\n" +
					"1. Update SealLib to the required version shown above\n" +
					"2. Restart your server\n" +
					"3. Check that both plugins are compatible with your Minecraft version"
			)
			log.hasProblems = true		}
		
		// Handle dependency errors (missing SealLib)
		if (dependencyError != null) {
			val dependencyName = dependencyError.groupValues[1]
			val requiredVersion = dependencyError.groupValues[2]
			log.addMessage(
				"**PowerGems Dependency Error** \n" +
					"Missing required dependency: `$dependencyName`\n" +
					"Required version: `$requiredVersion`\n\n" +
					"**Solution:**\n" +
					"1. Download $dependencyName version $requiredVersion\n" +
					"2. Install it in your plugins folder\n" +
					"3. Restart your server\n\n" +
					"PowerGems will not work without this dependency."
			)
			log.hasProblems = true
		}
		
		// Handle version mismatch errors
		if (versionMismatch != null) {
			val dependencyName = versionMismatch.groupValues[1]
			val requiredVersion = versionMismatch.groupValues[2]
			log.addMessage(
				"**PowerGems Version Mismatch** \n" +
					"Incorrect $dependencyName version detected!\n" +
					"Required version: `$requiredVersion`\n\n" +
					"**Solution:**\n" +
					"1. Remove the current $dependencyName plugin\n" +
					"2. Download $dependencyName version $requiredVersion\n" +
					"3. Install the correct version\n" +
					"4. Restart your server"
			)
			log.hasProblems = true
		}
				// Handle I18N/localization errors
		if (i18nError != null) {
			log.addMessage(
				"**PowerGems Localization Error** \n" +
					"Failed to load language bundle. This can cause missing translations.\n\n" +
					"**Common Causes:**\n" +
					"• Corrupted PowerGems installation\n" +
					"• Invalid language/country code in config\n" +
					"• Missing language files\n\n" +
					"**Solution:**\n" +
					"1. Check your config for valid language codes (e.g., 'en_US')\n" +
					"2. Re-download PowerGems if the problem persists"
			)
			log.hasProblems = true
		}
				// Handle WorldGuard integration errors - only show if there's an actual exception
		// Note: WorldGuard is optional, so only flag real errors, not missing dependency
		
		// Handle gem creation/validation errors  
		if (gemCreationError != null) {
			log.addMessage(
				"**PowerGems Gem Validation Error** \n" +
					"Invalid gem name or ID detected in configuration.\n\n" +
					"**Common Causes:**\n" +
					"• Typos in gem names in config files\n" +
					"• Invalid gem IDs in commands\n" +
					"• Custom gem names from addons\n\n" +
					"**Solution:**\n" +
					"1. Check your gemMaterials.yml for typos\n" +
					"2. Use valid gem names: Fire, Water, Earth, Air, Lightning, Ice, Healing, Strength, Iron, Sand\n" +
					"3. For addon gems, ensure proper configuration"
			)
			log.hasProblems = true
		}
				// Handle recipe/config errors - only show if there's an actual exception
		// Note: Recipe error patterns are handled in the main exception processing above
		
		// Handle common command messages (informational)
		if (commandMessage != null && debugException == null) {
			val messageType = commandMessage.groupValues[1]
			when (messageType) {
				"GEM_DISABLED" -> {
					log.addMessage(
						"**PowerGems Usage Info** \n" +
							"A player tried to use a disabled gem. This is normal if you've disabled certain gems in the ActiveGems configuration."
					)
				}
				"ON_COOLDOWN_GEMS" -> {
					log.addMessage(
						"**PowerGems Usage Info** \n" +
							"A player tried to use a gem while on cooldown. This is normal gameplay behavior."
					)
				}
				"CANNOT_MOVE_GEMS", "CANNOT_PLACE_GEMS_IN_CONTAINERS" -> {
					log.addMessage(
						"**PowerGems Protection Info** \n" +
							"Gem movement restrictions are working as intended. Check `allowMovingGems` in config if you want to change this behavior."
					)
				}
				"INVENTORY_FULL" -> {
					log.addMessage(
						"**PowerGems Command Info** \n" +
							"A command failed because the target player's inventory was full. This is normal behavior."
					)
				}
			}
		}
				// Handle update check messages (informational)
		if (updateError != null && !log.content.contains("failed")) {
			log.addMessage(
				"**PowerGems Update Check** \n" +
					"PowerGems is checking for updates. This is normal if update checking is enabled in the config."
			)
		} else if (updateError != null) {
			log.addMessage(
				"**PowerGems Update Check Failed** \n" +
					"Update check failed. This won't affect plugin functionality but you may not be notified of new versions.\n\n" +
					"**Possible Causes:**\n" +
					"• Network connectivity issues\n" +
					"• Update server temporarily unavailable\n" +
					"• Firewall blocking update checks\n\n" +
					"**Solution:**\n" +
					"1. Check your internet connection\n" +
					"2. Disable update checking in config if not needed\n" +
					"3. Manually check for updates on the plugin page"
			)
			log.hasProblems = true
		}
		
		// Analyze configuration dumps for common issues
		if (configDumps.isNotEmpty()) {
			analyzeConfigurationDumps(log, configDumps)
		}	}
	
	private fun analyzeConfigurationDumps(log: Log, configDumps: List<MatchResult>) {
		val configMap = mutableMapOf<String, MutableMap<String, String>>()
		
		// Parse all configuration dumps
		configDumps.forEach { match ->
			val manager = match.groupValues[1]
			val key = match.groupValues[2] 
			val value = match.groupValues[3]
			
			configMap.computeIfAbsent(manager) { mutableMapOf() }[key] = value
		}
		
		val issues = mutableListOf<String>()
				// Check cooldown configuration
		configMap["CooldownConfigManager"]?.let { cooldownConfig ->
			val cooldowns = cooldownConfig.values.mapNotNull { it.toIntOrNull() }
			if (cooldowns.isNotEmpty()) {
				val minCooldown = cooldowns.minOrNull() ?: 0
				
				// Only flag extremely problematic cooldowns
				if (minCooldown < 5) {
					issues.add("Very low cooldown times detected (min: ${minCooldown}s) - may cause ability spam")
				}
			}
		}
				// Check gem color configuration
		// Note: PowerGems uses custom model data for gem distinction, so same base color is normal
		
		// Check gem material configuration  
		// Note: PowerGems uses custom model data for gem distinction, so same base material is normal
		
		// Check permanent effects
		// Note: Some gems logically share effects (e.g., Fire and Lava both having fire resistance)
				// Check gem level effects
		configMap["GemPermanentEffectLevelConfigManager"]?.let { levelConfig ->
			val levels = levelConfig.values.mapNotNull { it.toIntOrNull() }
			if (levels.isNotEmpty()) {
				val minLevel = levels.minOrNull() ?: 0
				// Only flag actually problematic effect levels
				if (minLevel < 1) {
					issues.add("Some permanent effects have level 0 or negative - effects may not work properly")
				}
			}
		}
		
		// Look for specific error patterns in the dump
		val gemManagerDumps = configDumps.filter { it.groupValues[1] == "GemManager" }
		if (gemManagerDumps.isNotEmpty()) {
			val gemIdLookup = gemManagerDumps.find { it.groupValues[2] == "gemIdLookup" }?.groupValues?.get(3)
			if (gemIdLookup != null && gemIdLookup.contains("[]")) {
				issues.add("Empty gem ID lookup - no gems may be registered properly")
			}
		}		
		// Only show message if there are actual issues
		if (issues.isNotEmpty()) {
			log.addMessage(
				"**PowerGems Configuration Issues** \n" +
					"⚠️ Potential problems detected:\n" +
					issues.joinToString("\n") { "• $it" }
			)
		}
	}
}
