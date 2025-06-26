/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.gabereal.minecraft.database

import dev.kord.common.entity.Snowflake
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone

public object MinecraftNotificationConfigs : LongIdTable("minecraft_notification_configs") {
    public val guildId: Column<Long> = long("guild_id").uniqueIndex()
    public val channelId: Column<Long> = long("channel_id")
    public val pingRoleId: Column<Long?> = long("ping_role_id").nullable()
    public val enabled: Column<Boolean> = bool("enabled").default(true)
    public val createdAt: Column<kotlinx.datetime.LocalDateTime> = datetime("created_at").defaultExpression(CurrentDateTime)
    public val updatedAt: Column<kotlinx.datetime.LocalDateTime> = datetime("updated_at").defaultExpression(CurrentDateTime)
}

public data class MinecraftNotificationConfig(
    val id: Long,
    val guildId: Long,
    val channelId: Long,
    val pingRoleId: Long?,
    val enabled: Boolean
)

public object MinecraftNotificationService {
    private val logger = io.github.oshai.kotlinlogging.KotlinLogging.logger { }
    
    private fun isDatabaseAvailable(): Boolean {
        return try {
            transaction {
                // Simple query to test database connectivity
                MinecraftNotificationConfigs.selectAll().limit(1).count() >= 0
            }
            true
        } catch (e: Exception) {
            logger.warn(e) { "Database is not available" }
            false
        }
    }
    
    public fun getConfig(guildId: Snowflake): MinecraftNotificationConfig? {
        if (!isDatabaseAvailable()) return null
        
        return try {
            transaction {
                MinecraftNotificationConfigs
                    .select { MinecraftNotificationConfigs.guildId eq guildId.value.toLong() }
                    .singleOrNull()
                    ?.let {
                        MinecraftNotificationConfig(
                            id = it[MinecraftNotificationConfigs.id].value,
                            guildId = it[MinecraftNotificationConfigs.guildId],
                            channelId = it[MinecraftNotificationConfigs.channelId],
                            pingRoleId = it[MinecraftNotificationConfigs.pingRoleId],
                            enabled = it[MinecraftNotificationConfigs.enabled]
                        )
                    }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to get config for guild $guildId" }
            null
        }
    }
    
    public fun setConfig(
        guildId: Snowflake,
        channelId: Snowflake,
        pingRoleId: Snowflake? = null
    ): MinecraftNotificationConfig? {
        if (!isDatabaseAvailable()) return null
        
        return try {
            transaction {
                val existing = MinecraftNotificationConfigs
                    .select { MinecraftNotificationConfigs.guildId eq guildId.value.toLong() }
                    .singleOrNull()
                
                if (existing != null) {
                    // Update existing config
                    MinecraftNotificationConfigs.update({ MinecraftNotificationConfigs.guildId eq guildId.value.toLong() }) {
                        it[MinecraftNotificationConfigs.channelId] = channelId.value.toLong()
                        it[MinecraftNotificationConfigs.pingRoleId] = pingRoleId?.value?.toLong()
                        it[MinecraftNotificationConfigs.updatedAt] = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.UTC)
                    }
                } else {
                    // Insert new config
                    MinecraftNotificationConfigs.insert {
                        it[MinecraftNotificationConfigs.guildId] = guildId.value.toLong()
                        it[MinecraftNotificationConfigs.channelId] = channelId.value.toLong()
                        it[MinecraftNotificationConfigs.pingRoleId] = pingRoleId?.value?.toLong()
                    }
                }
                
                getConfig(guildId)
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to set config for guild $guildId" }
            null
        }
    }
    
    public fun removeConfig(guildId: Snowflake): Boolean {
        if (!isDatabaseAvailable()) return false
        
        return try {
            transaction {
                val deletedCount = MinecraftNotificationConfigs.deleteWhere { 
                    MinecraftNotificationConfigs.guildId.eq(guildId.value.toLong()) 
                }
                deletedCount > 0
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to remove config for guild $guildId" }
            false
        }
    }
    
    public fun setEnabled(guildId: Snowflake, enabled: Boolean): Boolean {
        if (!isDatabaseAvailable()) return false
        
        return try {
            transaction {
                MinecraftNotificationConfigs.update({ MinecraftNotificationConfigs.guildId eq guildId.value.toLong() }) {
                    it[MinecraftNotificationConfigs.enabled] = enabled
                    it[MinecraftNotificationConfigs.updatedAt] = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.UTC)
                } > 0
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to set enabled status for guild $guildId" }
            false
        }
    }
    
    public fun getAllEnabledConfigs(): List<MinecraftNotificationConfig> {
        if (!isDatabaseAvailable()) {
            logger.warn { "Database is not available, returning empty list of enabled configs" }
            return emptyList()
        }
        
        return try {
            transaction {
                MinecraftNotificationConfigs
                    .select { MinecraftNotificationConfigs.enabled eq true }
                    .map {
                        MinecraftNotificationConfig(
                            id = it[MinecraftNotificationConfigs.id].value,
                            guildId = it[MinecraftNotificationConfigs.guildId],
                            channelId = it[MinecraftNotificationConfigs.channelId],
                            pingRoleId = it[MinecraftNotificationConfigs.pingRoleId],
                            enabled = it[MinecraftNotificationConfigs.enabled]
                        )
                    }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to get enabled configs" }
            emptyList()
        }
    }
}
