package me.qbosst.bossbot.bot.commands.misc.greeting

import me.qbosst.bossbot.bot.BossBot
import me.qbosst.bossbot.bot.commands.dev.DeveloperCommand
import me.qbosst.bossbot.database.tables.UserDataTable
import me.qbosst.bossbot.entities.database.UserData
import me.qbosst.bossbot.util.getUserByString
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

object GreetingUpdateCommand : DeveloperCommand(
        "update",
        botPermissions = listOf(Permission.MESSAGE_WRITE)
) {
    override fun execute(event: MessageReceivedEvent, args: List<String>) {
        if (args.isNotEmpty())
        {
            val user = BossBot.shards.getUserByString(args[0])
            if (user != null)
            {
                if (args.size > 1)
                {
                    val message = args.drop(1).joinToString(" ")
                    if (message.length < UserDataTable.max_greeting_length)
                    {
                        UserData.update(user, UserDataTable.greeting, message)
                        event.channel.sendMessage("$user greeting has been updated").queue()
                    }
                    else
                    {
                        event.channel.sendMessage("too long").queue()
                    }
                }
                else
                {
                    event.channel.sendMessage(UserData.get(user).greeting ?: "${user.asTag} does not have a greeting message").queue()
                }
            }
            else
            {
                event.channel.sendMessage("could not find user").queue()
            }
        }
        else
        {
            event.channel.sendMessage("please mention user.").queue()
        }
    }

}