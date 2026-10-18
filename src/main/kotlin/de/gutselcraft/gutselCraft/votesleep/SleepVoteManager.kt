package de.gutselcraft.gutselCraft.votesleep

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.*
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Shared state is touched from several region threads on Folia (bed, join/quit and game mode events)
 * plus the global region scheduler tasks, so it is kept thread-safe. World time is only touched from
 * the global region scheduler, and per-player calls hop onto that player's entity scheduler.
 */
object SleepVoteManager {

    @Volatile private var bossBar: BossBar? = null
    @Volatile private var votes: MutableMap<UUID, Boolean>? = null
    @Volatile private var voteWorld: World? = null
    @Volatile private var voteEndCheckTask: ScheduledTask? = null
    @Volatile private var nightSkipTask: ScheduledTask? = null
    private lateinit var plugin: Plugin

    fun initialize(plugin: Plugin) {
        this.plugin = plugin
    }

    @Synchronized
    fun startVote(player: Player) {
        // Check if player is in bed and no vote is running
        if (voteWorld != null) return
        if (!player.isSleeping) return
        if (player.gameMode != GameMode.SURVIVAL && player.gameMode != GameMode.ADVENTURE) return

        votes = ConcurrentHashMap()
        voteWorld = player.world

        // Count eligible players (read-only look at players that may live in other regions)
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
            val message = Component.empty()
                .append(player.displayName())
                .append(Component.text(" will Heia Bubu machen! "))
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
                updateBossBar()
                
                // Delay the tab list update to ensure nickname plugin has loaded
                player.scheduler.runDelayed(plugin, {
                    updateTabListPrefix(player, false)
                }, null, 5L) // 5 tick delay
                
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
                        onPlayerThread(player) {
                            if (player.isSleeping) {
                                player.setStatistic(Statistic.TIME_SINCE_REST, 0)
                            }
                        }
                    }
                }

                // Start smooth night skip (only once per vote)
                synchronized(this) {
                    if (nightSkipTask == null) {
                        nightSkipTask = plugin.server.globalRegionScheduler
                            .runAtFixedRate(plugin, NightSkipTask(), 1L, 1L)
                    }
                }
            }
        }
    }

    private fun startVoteEndCheck() {
        voteEndCheckTask = plugin.server.globalRegionScheduler.runAtFixedRate(plugin, {
            voteWorld?.let { world ->
                if (world.time in 1..12010) {
                    endVote()
                }
            }
        }, 1L, 20L)
    }

    @Synchronized
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
        nightSkipTask?.cancel()
        nightSkipTask = null
    }
    
    fun getVoteWorld(): World? = voteWorld
    
    fun isNight(time: Long): Boolean = time in 12541..23500
    
    /** Runs [action] on the thread that owns [player] (a no-op if the player has left). */
    private fun onPlayerThread(player: Player, action: () -> Unit) {
        player.scheduler.run(plugin, { action() }, null)
    }

    private fun updateTabListPrefix(player: Player, hasVoted: Boolean) {
        onPlayerThread(player) {
            val prefix = if (hasVoted) {
                Component.text("✔ ").color(NamedTextColor.GREEN)
            } else {
                Component.text("✖ ").color(NamedTextColor.RED)
            }
            player.playerListName(prefix.append(player.displayName().color(NamedTextColor.WHITE)))
        }
    }

    private fun clearTabListPrefix(player: Player) {
        onPlayerThread(player) {
            player.playerListName(player.displayName().color(NamedTextColor.WHITE))
        }
    }
}
