package de.gutselcraft.gutselCraft.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin

class ReloadCommand(private val plugin: Plugin) : CommandExecutor {
    
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
}
