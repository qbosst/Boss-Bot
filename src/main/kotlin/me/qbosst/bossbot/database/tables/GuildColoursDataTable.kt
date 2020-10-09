package me.qbosst.bossbot.database.tables

import org.jetbrains.exposed.sql.Table

object GuildColoursDataTable: Table()
{
    const val MAX_COLOUR_NAME_LENGTH = 32
    const val MAX_COLOURS_PER_GUILD = 100

    val guild_id = long("GUILD_ID").default(0L)
    val name = varchar("COLOUR_NAME", MAX_COLOUR_NAME_LENGTH)
    val red = integer("RED").default(255)
    val green = integer("GREEN").default(255)
    val blue = integer("BLUE").default(255)
    val alpha = integer("ALPHA").default(255)

    override val tableName
        get() = "GUILD_COLOURS_DATA"

    override val primaryKey
        get() = PrimaryKey(guild_id, name)
}