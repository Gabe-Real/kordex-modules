/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package org.quiltmc.community.logs.plugins.powergems.processors

import org.quiltmc.community.cozy.modules.logs.data.Log
import org.quiltmc.community.cozy.modules.logs.data.Order
import org.quiltmc.community.cozy.modules.logs.types.LogProcessor

private val POWERGEMS_PERFORMANCE_WARNING_REGEX =
	"""\[PowerGems] Performance warning: (.+)""".toRegex()

private val POWERGEMS_TICK_LAG_REGEX =
	"""\[PowerGems] Gem abilities causing tick lag: (\d+)ms average""".toRegex()

private val POWERGEMS_MEMORY_WARNING_REGEX =
	"""\[PowerGems] High memory usage: ([0-9.]+)MB""".toRegex()

private val POWERGEMS_INCOMPATIBLE_VERSION_REGEX =
	"""\[PowerGems] Warning: Running on unsupported server version (.+)""".toRegex()

private val POWERGEMS_PARTICLE_LAG_REGEX =
	"""\[PowerGems] Particle effects causing performance issues""".toRegex()

private val POWERGEMS_TOO_MANY_EFFECTS_REGEX =
	"""\[PowerGems] Too many active effects \((\d+)\), some may be skipped""".toRegex()

private val POWERGEMS_ASYNC_ERROR_REGEX =
	"""\[PowerGems] Async operation failed: (.+)""".toRegex()

private val POWERGEMS_COMPATIBILITY_WARNING_REGEX =
	"""\[PowerGems] Compatibility issue with (.+): (.+)""".toRegex()

private val POWERGEMS_UPDATE_AVAILABLE_REGEX =
	"""\[PowerGems] Update available: (.+) -> (.+)""".toRegex()

private val POWERGEMS_WORLDGUARD_FLAG_ERROR_REGEX =
	"""\[PowerGems] Failed to register WorldGuard flag: (.+)""".toRegex()

public class PowerGemsPerformanceProcessor : LogProcessor() {
	override val identifier: String = "powergems_performance_processor"
	override val order: Order = Order.Earlier

	override suspend fun process(log: Log) {
		// Check for performance warnings
		val performanceWarning = POWERGEMS_PERFORMANCE_WARNING_REGEX.find(log.content)
		if (performanceWarning != null) {
			val warning = performanceWarning.groupValues[1]
			log.addMessage(
				"**PowerGems Performance Warning** \n" +
					"$warning\n" +
					"• Consider adjusting gem cooldowns or effect durations\n" +
					"• Reduce particle effect density if causing lag\n" +
					"• Check if server hardware can handle current gem load\n" +
					"• Review gem configuration for performance optimization"
			)
			log.hasProblems = true
		}

		// Check for tick lag
		val tickLag = POWERGEMS_TICK_LAG_REGEX.find(log.content)
		if (tickLag != null) {
			val avgMs = tickLag.groupValues[1].toIntOrNull() ?: 0
			val severity = when {
				avgMs > 50 -> "**SEVERE**"
				avgMs > 20 -> "**HIGH**"
				else -> "**MODERATE**"
			}

			log.addMessage(
				"**PowerGems Tick Lag Detected** \n" +
					"$severity - Average gem processing time: ${avgMs}ms\n" +
					"Recommendations:\n" +
					"• Increase gem cooldowns to reduce frequency\n" +
					"• Disable cosmetic particle effects temporarily\n" +
					"• Limit concurrent gem abilities\n" +
					"• Consider reducing gem effect ranges\n" +
					"• Check for inefficient gem configurations"
			)
			log.hasProblems = true
		}

		// Check for memory warnings
		val memoryWarning = POWERGEMS_MEMORY_WARNING_REGEX.find(log.content)
		if (memoryWarning != null) {
			val memoryMB = memoryWarning.groupValues[1]
			log.addMessage(
				"**PowerGems High Memory Usage** \n" +
					"Memory usage: ${memoryMB}MB\n" +
					"• Consider reducing gem cache size\n" +
					"• Lower particle effect counts\n" +
					"• Increase garbage collection frequency\n" +
					"• Check for memory leaks in gem abilities\n" +
					"• Monitor active gem effects and cleanup"
			)
		}

		// Check for server version compatibility
		val incompatibleVersion = POWERGEMS_INCOMPATIBLE_VERSION_REGEX.find(log.content)
		if (incompatibleVersion != null) {
			val serverVersion = incompatibleVersion.groupValues[1]
			log.addMessage(
				"**PowerGems Version Compatibility Warning** \n" +
					"Running on unsupported server version: `$serverVersion`\n" +
					"• Update to a supported server version if possible\n" +
					"• Check PowerGems documentation for compatibility info\n" +
					"• Some features may not work correctly\n" +
					"• Consider updating PowerGems to latest version"
			)
		}

		// Check for particle performance issues
		val particleLag = POWERGEMS_PARTICLE_LAG_REGEX.find(log.content)
		if (particleLag != null) {
			log.addMessage(
				"**PowerGems Particle Performance Issues** \n" +
					"Particle effects are causing performance problems\n" +
					"• Set `allowCosmeticParticleEffects` to `false` in config\n" +
					"• Increase `cosmeticParticleEffectInterval` value\n" +
					"• Reduce particle density in gem configurations\n" +
					"• Consider disabling particles during high server load"
			)
			log.hasProblems = true
		}

		// Check for too many active effects
		val tooManyEffects = POWERGEMS_TOO_MANY_EFFECTS_REGEX.find(log.content)
		if (tooManyEffects != null) {
			val effectCount = tooManyEffects.groupValues[1]
			log.addMessage(
				"**PowerGems Effect Overload** \n" +
					"Too many active effects ($effectCount), some may be skipped\n" +
					"• Increase cooldowns to reduce concurrent effects\n" +
					"• Limit the number of players using gems simultaneously\n" +
					"• Consider implementing effect queuing system\n" +
					"• Review gem configurations for optimization"
			)
			log.hasProblems = true
		}

		// Check for async operation failures
		val asyncError = POWERGEMS_ASYNC_ERROR_REGEX.find(log.content)
		if (asyncError != null) {
			val error = asyncError.groupValues[1]
			log.addMessage(
				"**PowerGems Async Operation Failed** \n" +
					"Async operation error: $error\n" +
					"• This may cause gem abilities to fail silently\n" +
					"• Check server thread pool configuration\n" +
					"• Consider synchronous alternatives if persistent\n" +
					"• Monitor for related gem functionality issues"
			)
			log.hasProblems = true
		}

		// Check for plugin compatibility issues
		val compatibilityWarning = POWERGEMS_COMPATIBILITY_WARNING_REGEX.find(log.content)
		if (compatibilityWarning != null) {
			val plugin = compatibilityWarning.groupValues[1]
			val issue = compatibilityWarning.groupValues[2]
			log.addMessage(
				"**PowerGems Compatibility Issue** \n" +
					"Issue with plugin `$plugin`: $issue\n" +
					"• Check if both plugins are up to date\n" +
					"• Look for compatibility patches or updates\n" +
					"• Consider disabling conflicting features\n" +
					"• Contact plugin developers if issue persists"
			)
		}

		// Check for available updates
		val updateAvailable = POWERGEMS_UPDATE_AVAILABLE_REGEX.find(log.content)
		if (updateAvailable != null) {
			val currentVersion = updateAvailable.groupValues[1]
			val newVersion = updateAvailable.groupValues[2]
			log.addMessage(
				"**PowerGems Update Available** \n" +
					"Current: `$currentVersion` → Available: `$newVersion`\n" +
					"• Download from [Modrinth](https://modrinth.com/plugin/powergems)\n" +
					"• Check changelog for bug fixes and new features\n" +
					"• Backup your configuration before updating\n" +
					"• Updates may include performance improvements"
			)
		}

		// Check for WorldGuard flag registration failures
		val worldGuardFlagError = POWERGEMS_WORLDGUARD_FLAG_ERROR_REGEX.find(log.content)
		if (worldGuardFlagError != null) {
			val error = worldGuardFlagError.groupValues[1]
			log.addMessage(
				"**PowerGems WorldGuard Flag Error** \n" +
					"Failed to register WorldGuard flag: $error\n" +
					"• Ensure WorldGuard is installed and enabled first\n" +
					"• Check for conflicting plugins registering the same flag\n" +
					"• Verify WorldGuard version compatibility\n" +
					"• PowerGems region restrictions may not work properly"
			)
			log.hasProblems = true
		}
	}
}
