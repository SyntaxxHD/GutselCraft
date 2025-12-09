package de.gutselcraft.gutselCraft.votesleep.commands

import de.gutselcraft.gutselCraft.votesleep.SleepVoteManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MitmachenCommand : CommandExecutor {
    
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender is Player) {
            if (SleepVoteManager.getVoteWorld() != null) {
                SleepVoteManager.agreeToSleep(sender)
            }
            return true
        }
        return false
    }
}
