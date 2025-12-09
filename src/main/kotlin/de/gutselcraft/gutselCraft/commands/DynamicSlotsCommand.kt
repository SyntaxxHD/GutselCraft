package de.gutselcraft.gutselCraft.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin

class DynamicSlotsCommand(private val plugin: Plugin) : CommandExecutor {
    
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (!sender.hasPermission("gutselcraft.dynamicslots")) {
            sender.sendMessage(
                Component.text("You don't have permission to use this command!")
                    .color(NamedTextColor.RED)
            )
            return true
        }
        
        val currentState = plugin.config.getBoolean("dynamic-slots.enabled", false)
        val newState = !currentState
        
        plugin.config.set("dynamic-slots.enabled", newState)
        plugin.saveConfig()
        
        val status = if (newState) "enabled" else "disabled"
        val color = if (newState) NamedTextColor.GREEN else NamedTextColor.RED
        
        sender.sendMessage(
            Component.text("Dynamic Slots has been ")
                .color(NamedTextColor.WHITE)
                .append(
                    Component.text(status)
                        .color(color)
                        .decorate(TextDecoration.BOLD)
                )
        )
        
        return true
    }
}
