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
    
    public fun getConfig(guildId: Snowflake): MinecraftNotificationConfig? = transaction {
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
    
    public fun setConfig(
        guildId: Snowflake,
        channelId: Snowflake,
        pingRoleId: Snowflake? = null
    ): MinecraftNotificationConfig = transaction {
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
        
        getConfig(guildId)!!
    }
    
    public fun removeConfig(guildId: Snowflake): Boolean = transaction {
        val deletedCount = MinecraftNotificationConfigs.deleteWhere { 
            MinecraftNotificationConfigs.guildId.eq(guildId.value.toLong()) 
        }
        deletedCount > 0
    }
    
    public fun setEnabled(guildId: Snowflake, enabled: Boolean): Boolean = transaction {
        MinecraftNotificationConfigs.update({ MinecraftNotificationConfigs.guildId eq guildId.value.toLong() }) {
            it[MinecraftNotificationConfigs.enabled] = enabled
            it[MinecraftNotificationConfigs.updatedAt] = kotlinx.datetime.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.UTC)
        } > 0
    }
    
    public fun getAllEnabledConfigs(): List<MinecraftNotificationConfig> = transaction {
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
}
