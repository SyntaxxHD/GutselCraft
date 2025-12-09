package de.gutselcraft.gutselCraft

import de.gutselcraft.gutselCraft.commands.AntiDespawnCommand
import de.gutselcraft.gutselCraft.commands.PingCommand
import de.gutselcraft.gutselCraft.commands.ReloadCommand
import de.gutselcraft.gutselCraft.listeners.DeathItemProtectionListener
import org.bukkit.plugin.java.JavaPlugin

class GutselCraft : JavaPlugin() {

    override fun onEnable() {
        // Save default config
        saveDefaultConfig()
        
        // Register listeners
        server.pluginManager.registerEvents(DeathItemProtectionListener(this), this)
        logger.info("Registered death item protection listener")
        
        // Register commands
        getCommand("ping")?.setExecutor(PingCommand())

        val antiDespawnCommand = AntiDespawnCommand(this)
        getCommand("antidespawn")?.apply {
            setExecutor(antiDespawnCommand)
            tabCompleter = antiDespawnCommand
        }
        
        getCommand("gutselcraft")?.setExecutor(ReloadCommand(this))

        logger.info("GutselCraft plugin has been enabled!")
    }

    override fun onDisable() {
        logger.info("GutselCraft plugin has been disabled!")
    }
}
