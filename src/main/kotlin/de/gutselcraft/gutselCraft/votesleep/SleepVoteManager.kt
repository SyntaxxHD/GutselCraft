package de.gutselcraft.gutselCraft.votesleep

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.UUID

object SleepVoteManager {
    
    private var bossBar: BossBar? = null
    private var votes: MutableMap<UUID, Boolean>? = null
    private var voteWorld: World? = null
    private var voteEndCheckTask: BukkitTask? = null
    private lateinit var plugin: Plugin
    
    fun initialize(plugin: Plugin) {
        this.plugin = plugin
    }
    
    fun startVote(player: Player) {
        // Check if player is in bed and no vote is running
        if (voteWorld != null) return
        if (!player.isSleeping) return
        if (player.gameMode != GameMode.SURVIVAL && player.gameMode != GameMode.ADVENTURE) return
        
        votes = mutableMapOf()
        voteWorld = player.world
        
        // Count eligible players
        val eligiblePlayers = Bukkit.getOnlinePlayers().filter { p ->
            p.world == voteWorld &&
            (p.gameMode == GameMode.SURVIVAL || p.gameMode == GameMode.ADVENTURE)
        }
        
        // Don't start vote if less than 2 players (single player uses normal sleep)
        if (eligiblePlayers.size < 2) {
            voteWorld = null
            votes = null
            return
        }
        
        // Create boss bar
        bossBar = Bukkit.createBossBar(
            "Müdis 0/${eligiblePlayers.size}",
            BarColor.GREEN,
            BarStyle.SOLID
        ).apply {
            progress = 0.0
        }
        
        // Add all eligible players to vote
        eligiblePlayers.forEach { p ->
            votes!![p.uniqueId] = false
            bossBar!!.addPlayer(p)
            updateTabListPrefix(p, false)
            
            // Send clickable message
            val message = Component.text("${player.displayName} will Heia Bubu machen! ")
                .color(NamedTextColor.WHITE)
                .append(
                    Component.text("[Mitmachen]")
                        .color(NamedTextColor.DARK_GREEN)
                        .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/mitmachen"))
                )
            
            p.sendMessage(message)
        }
        
        startVoteEndCheck()
        agreeToSleep(player)
    }
    
    fun agreeToSleep(player: Player) {
        val playerUuid = player.uniqueId
        votes?.let { voteMap ->
            if (voteMap.containsKey(playerUuid) && voteMap[playerUuid] == false) {
                voteMap[playerUuid] = true
                updateTabListPrefix(player, true)
                updateBossBar()
                checkVote()
            }
        }
    }
    
    fun unvotePlayer(player: Player) {
        val playerUuid = player.uniqueId
        votes?.let { voteMap ->
            if (voteMap.containsKey(playerUuid)) {
                // Set vote back to false (player left bed)
                voteMap[playerUuid] = false
                updateTabListPrefix(player, false)
                updateBossBar()
                checkVote()
            }
        }
    }
    
    fun removeFromVotes(player: Player) {
        val playerUuid = player.uniqueId
        votes?.let { voteMap ->
            if (voteMap.containsKey(playerUuid)) {
                voteMap.remove(playerUuid)
                bossBar?.removePlayer(player)
                clearTabListPrefix(player)
                updateBossBar()
                checkVote()
            }
        }
    }
    
    fun addToVotes(player: Player) {
        if (player.world == voteWorld && 
            (player.gameMode == GameMode.SURVIVAL || player.gameMode == GameMode.ADVENTURE)) {
            votes?.let { voteMap ->
                voteMap[player.uniqueId] = false
                bossBar?.addPlayer(player)
                updateTabListPrefix(player, false)
                updateBossBar()
                
                val message = Component.text("Jemand will Heia Bubu machen! ")
                    .color(NamedTextColor.WHITE)
                    .append(
                        Component.text("[Mitmachen]")
                            .color(NamedTextColor.DARK_GREEN)
                            .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/mitmachen"))
                    )
                
                player.sendMessage(message)
            }
        }
    }
    
    private fun updateBossBar() {
        votes?.let { voteMap ->
            val agreeCount = voteMap.values.count { it }
            
            bossBar?.apply {
                setTitle("Müdis $agreeCount/${voteMap.size}")
                progress = if (voteMap.isNotEmpty()) {
                    agreeCount.toDouble() / voteMap.size
                } else {
                    0.0
                }
            }
        }
    }
    
    private fun checkVote() {
        votes?.let { voteMap ->
            if (voteMap.isNotEmpty() && voteMap.values.all { it }) {
                // All players voted yes - reset sleep statistics for players still in bed
                voteMap.keys.forEach { playerUuid ->
                    Bukkit.getPlayer(playerUuid)?.let { player ->
                        if (player.isSleeping) {
                            player.setStatistic(Statistic.TIME_SINCE_REST, 0)
                        }
                    }
                }
                
                // Start smooth night skip
                NightSkipTask().runTaskTimer(plugin, 0, 1)
            }
        }
    }
    
    private fun startVoteEndCheck() {
        voteEndCheckTask = object : BukkitRunnable() {
            override fun run() {
                voteWorld?.let { world ->
                    if (world.time in 1..12010) {
                        endVote()
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20L)
    }
    
    fun endVote() {
        // Clear tab list prefixes for all voting players
        votes?.keys?.forEach { playerUuid ->
            Bukkit.getPlayer(playerUuid)?.let { player ->
                clearTabListPrefix(player)
            }
        }
        
        bossBar?.removeAll()
        bossBar = null
        votes = null
        voteWorld = null
        
        voteEndCheckTask?.cancel()
        voteEndCheckTask = null
    }
    
    fun getVoteWorld(): World? = voteWorld
    
    fun isNight(time: Long): Boolean = time in 12541..23500
    
    private fun updateTabListPrefix(player: Player, hasVoted: Boolean) {
        val prefix = if (hasVoted) {
            Component.text("✔ ").color(NamedTextColor.GREEN)
        } else {
            Component.text("✖ ").color(NamedTextColor.RED)
        }
        player.playerListName(prefix.append(Component.text(player.displayName).color(NamedTextColor.WHITE)))
    }
    
    private fun clearTabListPrefix(player: Player) {
        player.playerListName(null)
    }
}
