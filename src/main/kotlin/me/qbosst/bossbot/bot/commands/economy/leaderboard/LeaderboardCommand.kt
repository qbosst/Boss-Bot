package me.qbosst.bossbot.bot.commands.economy.leaderboard

import me.qbosst.bossbot.bot.argumentInvalid
import me.qbosst.bossbot.bot.commands.meta.Command
import me.qbosst.bossbot.bot.listeners.MessageListener
import me.qbosst.bossbot.bot.listeners.VoiceListener
import me.qbosst.bossbot.database.tables.MemberDataTable
import me.qbosst.bossbot.util.TimeUtil
import me.qbosst.bossbot.util.embed.DescriptionMenuEmbed
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.internal.utils.tuple.MutablePair
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object LeaderboardCommand : Command(
        "leaderboard",
        "Displays stats",
        usage_raw = listOf("<stat>"),
        examples_raw = enumValues<Stats>().map { it.example },
        aliases_raw = listOf("lb"),
        botPermissions = listOf(Permission.MESSAGE_EMBED_LINKS)
)
{
    private const val RECORDS_PER_PAGE = 5

    override fun execute(event: MessageReceivedEvent, args: List<String>, flags: Map<String, String?>)
    {
        if(args.isNotEmpty())
        {
            // Tries to get the stat the user wants to see
            val type = enumValues<Stats>().firstOrNull { it.matcher.matches(args[0].toLowerCase()) || it.name.equals(args[0], true) }

            if(type != null)
                transaction {

                    // Gets the results of all the stats
                    val results = MemberDataTable
                            // Gets every single member from the database and gets that specific stat
                            .slice(type.column, MemberDataTable.user_id)
                            .select { MemberDataTable.guild_id.eq(event.guild.idLong) }

                            // Formats the stat list
                            .map { row -> MutablePair(row[MemberDataTable.user_id], row[type.column]) }
                            .format(event.guild, type)
                            .map { it.plus("\n") }

                    val max = getMaxPages(results.size)

                    // Get page number
                    val page = (args.getOrNull(1)?.toIntOrNull() ?: 0).coerceIn(1, max)

                    // Position of user that typed command
                    val pos = results.indexOfFirst { it.contains(event.author.id) }

                    // Guild name for embed
                    val guildName = event.guild.name + "'" + if(event.guild.name.endsWith("s")) "" else "s"

                    // Creates embed and sends it
                    val embed = DescriptionMenuEmbed(RECORDS_PER_PAGE, results)
                            .createPage(EmbedBuilder()
                                    .setTitle("$guildName ${type.name.split(Regex("_")).joinToString(" ") { it.toLowerCase().capitalize() }} Stats")
                                    .setColor(event.guild.selfMember.colorRaw)
                                    .setFooter("Your position: ${if(pos != -1) pos+1 else "N/A"}")
                                    .setThumbnail(event.guild.iconUrl ?: event.jda.selfUser.defaultAvatarUrl)
                                    , page)
                    event.channel.sendMessage(embed.build()).queue()
                }
            else
                event.channel.sendMessage(argumentInvalid(args[0], "leaderboard type")).queue()
        }
        else
            event.channel.sendMessage("Please provide the type of leaderboard you would like to see (${enumValues<Stats>().joinToString(", ") {it.name.toLowerCase()}})").queue()
    }

    /**
     *  Gets max pages of an embed
     *
     *  @param count The amount of members that is in the list
     *
     *  @return the max amount of pages
     */
    private fun getMaxPages(count: Int): Int
    {
        var maxPages: Int = count / RECORDS_PER_PAGE
        if(count % RECORDS_PER_PAGE == 0 && maxPages > 0)
            maxPages -= 1

        return maxPages+1
    }

    /**
     *  Extension method for MutablePair. Sets the right value of the pair and returns the object itself.
     *  This is so that it can be chained in methods
     *
     *  @param value the new right value
     *
     *  @return MutablePair object
     */
    private fun <L, R> MutablePair<L, R>.setRightAndReturn(value: R): MutablePair<L, R>
    {
        setRight(value)
        return this
    }

    /**
     *  Extension method.
     *  Exists so that it can be chained.
     *
     *  @param guild Guild
     *  @param type Stat type
     *
     *  @return formatted stats
     */
    private fun List<MutablePair<Long, Any?>>.format(guild: Guild, type: Stats): List<String>
    {
        return type.format(guild, this)
    }

    private interface IStats
    {
        /**
         *  Formats data into a list of string to make it easier to manipulate
         *
         *  @param guild The guild of where the stats are from
         *  @param data THe actual stat data of each member
         *
         *  @return List of strings which should be all sorted from top to bottom, each string representing a member along with their stat
         */
        fun format(guild: Guild, data: List<MutablePair<Long, Any?>>): List<String>
    }

    /**
     *  Class for each stat in a leaderboard
     *
     *  @param matcher The name of the stat
     *  @param column The column that represents the stat in the database
     *  @param example Example name of stat
     */
    private enum class Stats(val matcher: Regex, val column: Column<*>, val example: String): IStats
    {
        MESSAGE_COUNT(Regex("(message|msg)s?"), MemberDataTable.message_count, "message")
        {
            override fun format(guild: Guild, data: List<MutablePair<Long, Any?>>): List<String>
            {
                @Suppress("UNCHECKED_CAST")
                // Filter it by if the value is not null than cast it
                return ((data.filter { it.right != null }) as List<MutablePair<Long, Int>>)
                        // Adds the cached value
                        .map { it.setRightAndReturn(it.right + MessageListener.getCachedMessageCount(guild, it.left)) }
                        // Sorts the list
                        .sortedByDescending { it.right }
                        // Formats it
                        .mapIndexed { index, record -> "${index+1}. <@${record.left}> -> ${record.right} messages sent" }
            }
        },
        TEXT_CHAT(Regex("t(ext)?c(hat)?"), MemberDataTable.text_chat_time, "textchat")
        {
            override fun format(guild: Guild, data: List<MutablePair<Long, Any?>>): List<String>
            {
                @Suppress("UNCHECKED_CAST")
                return ((data.filter { it.right != null }) as List<MutablePair<Long, Long>>)
                        .sortedByDescending { it.right }
                        .mapIndexed { index, record -> "${index+1}. <@${record.left}> -> ${TimeUtil.timeToString(record.right)}"}
            }
        },
        VOICE_CHAT(Regex("v(oice)?c(hat)?"), MemberDataTable.voice_chat_time, "voicechat")
        {
            override fun format(guild: Guild, data: List<MutablePair<Long, Any?>>): List<String>
            {
                @Suppress("UNCHECKED_CAST")
                return ((data.filter { it.right != null }) as List<MutablePair<Long, Long>>)
                        .map { it.setRightAndReturn(it.right + VoiceListener.getCachedVoiceChatTime(guild, it.left)) }
                        .sortedByDescending { it.right }
                        .mapIndexed { index, record -> "${index+1}. <@${record.left}> -> ${TimeUtil.timeToString(record.right)}"}
            }
        },
        EXPERIENCE(Regex("e?xp(erience)?"), MemberDataTable.experience, "experience")
        {
            override fun format(guild: Guild, data: List<MutablePair<Long, Any?>>): List<String>
            {
                @Suppress("UNCHECKED_CAST")
                return ((data.filter { it.right != null }) as List<MutablePair<Long, Int>>)
                        .sortedByDescending { it.right }
                        .mapIndexed { index, record -> "${index+1}. <@${record.left}> -> ${record.right} xp"}
            }
        }
    }
}