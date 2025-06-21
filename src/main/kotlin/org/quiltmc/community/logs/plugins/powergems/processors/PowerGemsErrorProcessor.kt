/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.logs.plugins.powergems.processors

import org.quiltmc.community.cozy.modules.logs.data.Log
import org.quiltmc.community.cozy.modules.logs.data.Order
import org.quiltmc.community.cozy.modules.logs.types.LogProcessor

private val POWERGEMS_CONFIG_ERROR_REGEX =
	"""\[PowerGems] (?:WARN|ERROR|SEVERE): (.+)""".toRegex()

private val POWERGEMS_DEPENDENCY_ERROR_REGEX =
	"""\[PowerGems] (.+) dependency not found""".toRegex()

private val POWERGEMS_DATABASE_ERROR_REGEX =
	"""\[PowerGems] (?:Database|MySQL|SQLite) (?:connection )?(?:error|failed): (.+)""".toRegex()

private val POWERGEMS_WORLDGUARD_ERROR_REGEX =
	"""\[PowerGems] WorldGuard integration (?:failed|error): (.+)""".toRegex()

private val POWERGEMS_GEM_CREATION_FAILED_REGEX =
	"""\[PowerGems] Failed to create gem (?:of type )?([A-Za-z]+)""".toRegex()

private val POWERGEMS_INVALID_CONFIG_REGEX =
	"""\[PowerGems] Invalid configuration (?:value|setting) (?:for )?([^:]+): (.+)""".toRegex()

private val POWERGEMS_PERMISSION_ERROR_REGEX =
	"""\[PowerGems] Permission (?:error|denied) (?:for )?(.+)""".toRegex()

public class PowerGemsErrorProcessor : LogProcessor() {
	override val identifier: String = "powergems_error_processor"
	override val order: Order = Order.Earlier
	
	override suspend fun process(log: Log) {
		// Check for dependency errors
		val dependencyError = POWERGEMS_DEPENDENCY_ERROR_REGEX.find(log.content)
		if (dependencyError != null) {
			val dependency = dependencyError.groupValues[1]
			log.addMessage(
				"**PowerGems Dependency Missing** \n" +
					"Required dependency `$dependency` not found. " +
					if (dependency.contains("SealLib", ignoreCase = true)) {
						"Download SealLib from [Modrinth](https://modrinth.com/plugin/seallib)."
					} else {
						"Make sure all required plugins are installed and enabled."
					}
			)
			log.hasProblems = true
			return
		}

		// Check for database errors
		val databaseError = POWERGEMS_DATABASE_ERROR_REGEX.find(log.content)
		if (databaseError != null) {
			val errorDetails = databaseError.groupValues[1]
			log.addMessage(
				"**PowerGems Database Error** \n" +
					"Database connection failed: `$errorDetails`\n" +
					"• Check your database configuration in the PowerGems config\n" +
					"• Ensure your database server is running and accessible\n" +
					"• Verify database credentials and permissions\n" +
					"• Check if the database/tables exist"
			)
			log.hasProblems = true
		}

		// Check for WorldGuard integration errors
		val worldGuardError = POWERGEMS_WORLDGUARD_ERROR_REGEX.find(log.content)
		if (worldGuardError != null) {
			val errorDetails = worldGuardError.groupValues[1]
			log.addMessage(
				"**PowerGems WorldGuard Integration Error** \n" +
					"WorldGuard integration failed: `$errorDetails`\n" +
					"• Ensure WorldGuard is installed and enabled\n" +
					"• Check WorldGuard version compatibility\n" +
					"• Verify PowerGems WorldGuard support is enabled in config"
			)
			log.hasProblems = true
		}

		// Check for gem creation failures
		val gemCreationError = POWERGEMS_GEM_CREATION_FAILED_REGEX.find(log.content)
		if (gemCreationError != null) {
			val gemType = gemCreationError.groupValues[1]
			log.addMessage(
				"**PowerGems Gem Creation Failed** \n" +
					"Failed to create $gemType gem. This could be due to:\n" +
					"• Invalid gem configuration\n" +
					"• Missing materials or recipes\n" +
					"• Conflicting plugins\n" +
					"• Insufficient permissions"
			)
			log.hasProblems = true
		}

		// Check for configuration errors
		val configError = POWERGEMS_INVALID_CONFIG_REGEX.find(log.content)
		if (configError != null) {
			val setting = configError.groupValues[1]
			val details = configError.groupValues[2]
			log.addMessage(
				"**PowerGems Configuration Error** \n" +
					"Invalid configuration for `$setting`: $details\n" +
					"• Check your PowerGems configuration files\n" +
					"• Ensure all values are in the correct format\n" +
					"• Consider regenerating the config if corrupted"
			)
			log.hasProblems = true
		}

		// Check for permission errors
		val permissionError = POWERGEMS_PERMISSION_ERROR_REGEX.find(log.content)
		if (permissionError != null) {
			val details = permissionError.groupValues[1]
			log.addMessage(
				"**PowerGems Permission Error** \n" +
					"Permission denied: $details\n" +
					"• Check your permission plugin configuration\n" +
					"• Ensure PowerGems permissions are properly assigned\n" +
					"• Verify player/group has necessary permissions"
			)
			log.hasProblems = true
		}

		// Check for general configuration/error messages
		val generalError = POWERGEMS_CONFIG_ERROR_REGEX.find(log.content)
		if (generalError != null) {
			val errorMessage = generalError.groupValues[1]
			log.addMessage(
				"**PowerGems Error/Warning** \n" +
					"$errorMessage\n" +
					"Check the PowerGems documentation or configuration for more details."
			)

			// Only mark as problem for errors, not warnings
			if (!errorMessage.contains("warn", ignoreCase = true)) {
				log.hasProblems = true
			}
		}
	}
}
