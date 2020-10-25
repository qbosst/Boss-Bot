package me.qbosst.bossbot.bot.commands.misc.time

import me.qbosst.bossbot.bot.commands.meta.Command
import me.qbosst.bossbot.bot.userNotFound
import me.qbosst.bossbot.database.managers.getUserData
import me.qbosst.bossbot.util.TimeUtil
import me.qbosst.bossbot.util.getMemberByString
import me.qbosst.bossbot.util.loadObjects
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

object TimeCommand: Command(
        "time",
        "Shows the time that users are in",
        usage_raw = listOf("[@user] [duration]"),
        examples_raw = listOf("@boss", "@boss 3h")
)
{
    init
    {
        addCommands(loadObjects(this::class.java.`package`.name, Command::class.java).filter { it != this })
    }

    override fun execute(event: MessageReceivedEvent, args: List<String>)
    {
        val authorZoneId = getZoneId(event.author)
        if(args.isNotEmpty())
        {
            val target = event.guild.getMemberByString(args[0])?.user
            if(target == null)
                event.channel.sendMessage(userNotFound(args[0])).queue()
            else if(args.size > 1)
            {
                val targetZoneId = getZoneId(target)
                if(targetZoneId == null)
                    event.channel.sendMessage("${target.asTag} does not have a timezone setup").queue()
                else
                {
                    val seconds = TimeUtil.parseTime(args.drop(1).joinToString(" "))
                    var date = ZonedDateTime.now(targetZoneId)
                    if(seconds > 0)
                        date = date.plusSeconds(seconds)
                    else if(seconds < 0)
                        date = date.minusSeconds(-seconds)

                    event.channel.sendMessage("The time for ${target.asTag} in `${TimeUtil.secondsToString(seconds) { unit, count -> "$count ${unit.longName}" }}` will be `${formatZonedDateTime(date)}`").queue()
                }
            }
            else
                event.channel.sendMessage(getZoneInfo(Pair(event.author, authorZoneId), Pair(target, getZoneId(target)))).queue()
        }
        else
        {
            val zoneId = getZoneId(event.author)
            event.channel.sendMessage(getZoneInfo(Pair(event.author, zoneId))).queue()
        }
    }

    private fun getZoneId(user: User): ZoneId? = user.getUserData().zone_id

    private fun getZoneInfo(author: Pair<User, ZoneId?>, target: Pair<User, ZoneId?> = author): String
    {
        val isSelf = author.first == target.first
        if(target.second == null)
            return "${if(isSelf) "You" else target.first.asTag} does not have a timezone setup"

        val sb = StringBuilder()
                .append("The time for ${if(isSelf) "you" else target.first.asTag} is `${getCurrentTime(target.second!!)}`. ")
                .append("${if(isSelf) "Your" else target.first.asTag.plus("'s")} time zone is `${target.second!!.id}`. ")

        if(author.second != null)
            if(author.second != target.second)
            {
                val differenceInSeconds = getZoneDifference(author.second!!, target.second!!)
                val timeString = TimeUtil.secondsToString(differenceInSeconds) { unit, count -> "$count ${unit.longName}" }
                val isBehind = timeString.startsWith("-")
                sb.append("${target.first.asTag} is `${if(isBehind) timeString.substring(1) else timeString}` ${if(isBehind) "behind" else "ahead of"} you")
            }
            else if(!isSelf)
                sb.append("They are in the same timezone as you!")

        return sb.toString()
    }

    private fun getZoneDifference(zone1: ZoneId, zone2: ZoneId): Int
    {
        val now = OffsetDateTime.now()
        val zone1Time = now.atZoneSameInstant(zone1)
        val zone2Time = now.atZoneSameInstant(zone2)

        return zone2Time.offset.totalSeconds - zone1Time.offset.totalSeconds
    }

    private fun getCurrentTime(zoneId: ZoneId): String = formatZonedDateTime(ZonedDateTime.now(zoneId))

    private fun formatZonedDateTime(time: ZonedDateTime): String = TimeUtil.DATE_TIME_FORMATTER.format(time)
}