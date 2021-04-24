/**
 *   Copyright © 2020 | vironlab.eu | All Rights Reserved.<p>
 * <p>
 *      ___    _______                        ______         ______  <p>
 *      __ |  / /___(_)______________ _______ ___  / ______ ____  /_ <p>
 *      __ | / / __  / __  ___/_  __ \__  __ \__  /  _  __ `/__  __ \<p>
 *      __ |/ /  _  /  _  /    / /_/ /_  / / /_  /___/ /_/ / _  /_/ /<p>
 *      _____/   /_/   /_/     \____/ /_/ /_/ /_____/\__,_/  /_.___/ <p>
 *<p>
 *    ____  _______     _______ _     ___  ____  __  __ _____ _   _ _____ <p>
 *   |  _ \| ____\ \   / / ____| |   / _ \|  _ \|  \/  | ____| \ | |_   _|<p>
 *   | | | |  _|  \ \ / /|  _| | |  | | | | |_) | |\/| |  _| |  \| | | |  <p>
 *   | |_| | |___  \ V / | |___| |__| |_| |  __/| |  | | |___| |\  | | |  <p>
 *   |____/|_____|  \_/  |_____|_____\___/|_|   |_|  |_|_____|_| \_| |_|  <p>
 *<p>
 *<p>
 *   This program is free software: you can redistribute it and/or modify<p>
 *   it under the terms of the GNU General Public License as published by<p>
 *   the Free Software Foundation, either version 3 of the License, or<p>
 *   (at your option) any later version.<p>
 *<p>
 *   This program is distributed in the hope that it will be useful,<p>
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of<p>
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<p>
 *   GNU General Public License for more details.<p>
 *<p>
 *   You should have received a copy of the GNU General Public License<p>
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.<p>
 *<p>
 *   Contact:<p>
 *<p>
 *     Discordserver:   https://discord.gg/wvcX92VyEH<p>
 *     Website:         https://vironlab.eu/ <p>
 *     Mail:            contact@vironlab.eu<p>
 *<p>
 */

package eu.vironlab.discordcommands

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Invite
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import javax.annotation.Nonnull


open class DefaultCommandManager(val jda: JDA, val prefix: String, val cmdNotFoundAction: (MessageReceivedEvent) -> Unit, val wrongTargetAction: (MessageReceivedEvent) -> Unit) : CommandManager, ListenerAdapter() {

    override val commands: MutableMap<SerializedCommandInfo, CommandExecutor> =  mutableMapOf()
    override val subCommands: MutableMap<String, MutableMap<SerializedSubCommandInfo, SubCommandExecutor>> = mutableMapOf()

    init {
        jda.eventManager.register(this)
    }

    override fun <T : CommandExecutor> register(instance: T) {
        if (!instance.javaClass.isAnnotationPresent(Command::class.java)) {
            throw IllegalStateException("Cannot register Command without @Command Annotation")
        }
        this.commands.put(SerializedCommandInfo.fromAnnotation(instance.javaClass.getAnnotation(Command::class.java)), instance)
    }

    override fun <T : SubCommandExecutor> registerSubCommand(instance: T) {
        if (!instance.javaClass.isAnnotationPresent(SubCommand::class.java)) {
            throw IllegalStateException("Cannot register Command without @Command Annotation")
        }
        val subCommand = instance.javaClass.getAnnotation(SubCommand::class.java)
        if (!this.subCommands.containsKey(subCommand.command)){
            this.subCommands.put(subCommand.command, mutableMapOf())
        }
        this.subCommands.get(subCommand.command)!!.put(SerializedSubCommandInfo.fromAnnotation(instance.javaClass.getAnnotation(SubCommand::class.java)), instance)
    }

    override fun unregister(command: Class<Any>) {
        this.commands.filterValues { it.javaClass.canonicalName == command.canonicalName }.forEach { (info, executor) ->
            this.commands.remove(info)
        }
        this.subCommands.filterValues { it.javaClass.canonicalName == command.canonicalName }.forEach { (info, executor) ->
            this.subCommands.remove(info)
        }
    }


    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.message.contentRaw.startsWith(this.prefix)) {
            val cmd: String?
            val args = event.message.contentRaw.split(" ").let {
                cmd = it.first()
                it.subList(1, it.size)
            }.toTypedArray()
            if (cmd == null) {
                throw IllegalStateException("There was an Error while collecting the Data from the Message")
            }
            val possibleCmds = this.commands.filterKeys { it.name == cmd || it.alias.contains(cmd) }
            if (possibleCmds.isEmpty()) {
                this.cmdNotFoundAction.invoke(event)
                return
            }
            val command = possibleCmds.entries.first()
            val commandInfo = command.key
            if (args.isNotEmpty()) {
                if (this.subCommands.containsKey(commandInfo.name)) {
                    val possibleSubCommand = this.subCommands.get(commandInfo.name)!!.filterKeys { it.name == args[0] }
                    if (possibleSubCommand.isNotEmpty()) {
                        val subCommand = possibleSubCommand.entries.first()
                        if (!checkChannelTarget(subCommand.key.target, event)) {
                            return
                        }
                        subCommand.value.proceed(cmd, args.let { it.copyOfRange(1, it.size) }, event.message, event.channel, jda)
                        return
                    }
                }
            }
            if (!checkChannelTarget(commandInfo.target, event)) {
                return
            }
            command.value.proceed(cmd, args, event.message, event.channel, jda)
        }
    }

    private fun checkChannelTarget(target: ChannelTarget, event: MessageReceivedEvent): Boolean {
        return if (event.isFromGuild) {
            if (target != ChannelTarget.PRIVATE) {
                true
            }else {
                wrongTargetAction.invoke(event)
                false
            }
        }else {
            if (target != ChannelTarget.GUILD) {
                true
            }else {
                wrongTargetAction.invoke(event)
                false
            }
        }
    }

}
