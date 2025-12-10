package de.gutselcraft.gutselCraft

import de.gutselcraft.gutselCraft.commands.AntiDespawnCommand
import de.gutselcraft.gutselCraft.commands.DynamicSlotsCommand
import de.gutselcraft.gutselCraft.commands.PingCommand
import de.gutselcraft.gutselCraft.commands.ReloadCommand
import de.gutselcraft.gutselCraft.listeners.DeathItemProtectionListener
import de.gutselcraft.gutselCraft.listeners.FabricRecommendationListener
import de.gutselcraft.gutselCraft.listeners.ProjectileKnockbackListener
import de.gutselcraft.gutselCraft.listeners.ServerListPingListener
import de.gutselcraft.gutselCraft.votesleep.SleepVoteManager
import de.gutselcraft.gutselCraft.votesleep.commands.MitmachenCommand
import de.gutselcraft.gutselCraft.votesleep.listeners.GameModeChangeListener
import de.gutselcraft.gutselCraft.votesleep.listeners.PlayerBedEnterListener
import de.gutselcraft.gutselCraft.votesleep.listeners.PlayerBedLeaveListener
import de.gutselcraft.gutselCraft.votesleep.listeners.PlayerJoinQuitListener
import de.gutselcraft.gutselCraft.votesleep.listeners.WorldChangeListener
import de.gutselcraft.gutselCraft.welcomebook.WelcomeBookListener
import org.bukkit.plugin.java.JavaPlugin

class GutselCraft : JavaPlugin() {

    override fun onEnable() {
        // Save default config
        saveDefaultConfig()
        
        // Initialize sleep vote manager
        SleepVoteManager.initialize(this)
        
        // Register listeners
        server.pluginManager.registerEvents(DeathItemProtectionListener(this), this)
        server.pluginManager.registerEvents(FabricRecommendationListener(this), this)
        server.pluginManager.registerEvents(ProjectileKnockbackListener(), this)
        server.pluginManager.registerEvents(ServerListPingListener(this), this)
        server.pluginManager.registerEvents(WelcomeBookListener(this), this)
        server.pluginManager.registerEvents(PlayerBedEnterListener(this), this)
        server.pluginManager.registerEvents(PlayerBedLeaveListener(this), this)
        server.pluginManager.registerEvents(PlayerJoinQuitListener(this), this)
        server.pluginManager.registerEvents(GameModeChangeListener(this), this)
        server.pluginManager.registerEvents(WorldChangeListener(this), this)
        
        // Register commands
        getCommand("ping")?.setExecutor(PingCommand())

        val antiDespawnCommand = AntiDespawnCommand(this)
        getCommand("antidespawn")?.apply {
            setExecutor(antiDespawnCommand)
            tabCompleter = antiDespawnCommand
        }
        
        val reloadCommand = ReloadCommand(this)
        getCommand("gutselcraft")?.apply {
            setExecutor(reloadCommand)
            tabCompleter = reloadCommand
        }
        
        getCommand("dynamicslots")?.setExecutor(DynamicSlotsCommand(this))
        
        getCommand("mitmachen")?.setExecutor(MitmachenCommand())

        logger.info("GutselCraft plugin has been enabled!")
    }

    override fun onDisable() {
        logger.info("GutselCraft plugin has been disabled!")
    }
}
