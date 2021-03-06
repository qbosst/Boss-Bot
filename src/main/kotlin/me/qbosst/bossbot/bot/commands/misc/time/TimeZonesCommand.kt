package me.qbosst.bossbot.bot.commands.misc.time

import me.qbosst.bossbot.bot.commands.meta.Command
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.time.ZoneId
import java.util.*

object TimeZonesCommand: Command(
        "zones",
        description = "Gives a list of time zones that can be used",
        botPermissions = listOf(Permission.MESSAGE_ATTACH_FILES),
        guildOnly = false
)
{
    override fun execute(event: MessageReceivedEvent, args: List<String>, flags: Map<String, String?>)
    {
        event.channel
            .sendMessage("Here is a list of zones that you can use.")
            .addFile(
                    ZoneId.getAvailableZoneIds()
                            .sorted()
                            .joinToString("\n") { id ->
                                val timeZone = TimeZone.getTimeZone(id)
                                "$id | ${timeZone.getDisplayName(true, 0)} | ${timeZone.getDisplayName(false, 0)}"
                            }
                        .toByteArray(), "zone_ids.txt")
            .queue()
    }
}