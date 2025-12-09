package de.gutselcraft.gutselCraft.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PingCommand : CommandExecutor {
    
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) {
            sender.sendMessage(
                Component.text("Nur Gutselspieler können den Befehl ausführen!")
                    .color(NamedTextColor.RED)
            )
            return true
        }
        
        val ping = sender.ping
        val pingComponent = Component.text("Dein Ping: ")
            .color(NamedTextColor.WHITE)
            .append(
                Component.text("${ping} ms")
                    .color(NamedTextColor.DARK_GREEN)
                    .decorate(TextDecoration.BOLD)
            )
        
        sender.sendMessage(pingComponent)
        return true
    }
}
