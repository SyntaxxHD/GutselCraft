package de.gutselcraft.gutselCraft.antidespawn

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.plugin.Plugin

class AntiDespawnCommand(private val plugin: Plugin) : CommandExecutor, TabCompleter {
    
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (!sender.hasPermission("gutselcraft.antidespawn")) {
            sender.sendMessage(
                Component.text("You don't have permission to use this command!")
                    .color(NamedTextColor.RED)
            )
            return true
        }
        
        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }
        
        when (args[0].lowercase()) {
            "toggle" -> handleToggle(sender)
            "settime" -> handleSetTime(sender, args)
            else -> sendHelp(sender)
        }
        
        return true
    }
    
    private fun handleToggle(sender: CommandSender) {
        val currentState = plugin.config.getBoolean("anti-despawn.enabled", true)
        val newState = !currentState
        
        plugin.config.set("anti-despawn.enabled", newState)
        plugin.saveConfig()
        
        val status = if (newState) "enabled" else "disabled"
        val color = if (newState) NamedTextColor.GREEN else NamedTextColor.RED
        
        sender.sendMessage(
            Component.text("Anti-Despawn has been ")
                .color(NamedTextColor.WHITE)
                .append(
                    Component.text(status)
                        .color(color)
                        .decorate(TextDecoration.BOLD)
                )
        )
    }
    
    private fun handleSetTime(sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.sendMessage(
                Component.text("Usage: /antidespawn settime <seconds|-1>")
                    .color(NamedTextColor.RED)
            )
            return
        }
        
        val time = args[1].toIntOrNull()
        if (time == null) {
            sender.sendMessage(
                Component.text("Invalid time! Use a number or -1 for never.")
                    .color(NamedTextColor.RED)
            )
            return
        }
        
        if (time < -1) {
            sender.sendMessage(
                Component.text("Time must be -1 or greater!")
                    .color(NamedTextColor.RED)
            )
            return
        }
        
        plugin.config.set("anti-despawn.despawn-time", time)
        plugin.saveConfig()
        
        val timeText = when (time) {
            -1 -> "never"
            0 -> "instantly (normal)"
            1 -> "1 second"
            else -> "$time seconds"
        }
        
        sender.sendMessage(
            Component.text("Despawn time set to ")
                .color(NamedTextColor.WHITE)
                .append(
                    Component.text(timeText)
                        .color(NamedTextColor.DARK_GREEN)
                        .decorate(TextDecoration.BOLD)
                )
        )
    }
    
    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage(
            Component.text("=== Anti-Despawn Commands ===")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD)
        )
        sender.sendMessage(
            Component.text("/antidespawn toggle")
                .color(NamedTextColor.YELLOW)
                .append(
                    Component.text(" - Enable/disable the feature")
                        .color(NamedTextColor.WHITE)
                )
        )
        sender.sendMessage(
            Component.text("/antidespawn settime <seconds|-1>")
                .color(NamedTextColor.YELLOW)
                .append(
                    Component.text(" - Set despawn time")
                        .color(NamedTextColor.WHITE)
                )
        )
        sender.sendMessage(
            Component.text("Note: -1 = never despawn")
                .color(NamedTextColor.GRAY)
        )
    }
    
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {
        if (!sender.hasPermission("gutselcraft.antidespawn")) {
            return emptyList()
        }
        
        return when (args.size) {
            1 -> listOf("toggle", "settime").filter { it.startsWith(args[0].lowercase()) }
            2 -> if (args[0].lowercase() == "settime") {
                listOf("-1", "0", "300", "600", "1800", "3600").filter { it.startsWith(args[1]) }
            } else emptyList()
            else -> emptyList()
        }
    }
}
