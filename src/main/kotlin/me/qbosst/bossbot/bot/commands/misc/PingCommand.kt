package me.qbosst.bossbot.bot.commands.misc

import me.qbosst.bossbot.bot.commands.meta.Command
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

object PingCommand : Command(
        "ping",
        description = "Checks bots ping",
        usage_raw = listOf(""),
        guildOnly = false
)
{
    override fun execute(event: MessageReceivedEvent, args: List<String>, flags: Map<String, String?>)
    {
        val ping = System.currentTimeMillis()
        event.channel.sendMessage("Pinging...").queue()
        { message ->
            val pong = System.currentTimeMillis() - ping
            event.jda.restPing.queue()
            { restPing ->
                message.editMessage("\uD83C\uDFD3 : ${pong}ms\nRest Ping : ${restPing}ms\nGateway Ping : ${event.jda.gatewayPing}ms").queue()
            }
        }
    }
}