package de.gutselcraft.gutselCraft

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.plugin.Plugin

class ReloadCommand(private val plugin: Plugin) : CommandExecutor, TabCompleter {
    
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        plugin.reloadConfig()
        sender.sendMessage(
            Component.text("GutselCraft Plugin reloaded!")
                .color(NamedTextColor.GREEN)
        )
        return true
    }
    
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {
        return when (args.size) {
            1 -> listOf("reload").filter { it.startsWith(args[0].lowercase()) }
            else -> emptyList()
        }
    }
}
