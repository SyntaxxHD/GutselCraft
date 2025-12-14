package de.gutselcraft.gutselCraft.votesleep

import org.bukkit.scheduler.BukkitRunnable
import kotlin.random.Random

class NightSkipTask : BukkitRunnable() {
    
    override fun run() {
        val world = SleepVoteManager.getVoteWorld()
        
        if (world == null) {
            cancel()
            return
        }
        
        val currentTime = world.time
        
        if (currentTime >= 12541) {
            world.time = currentTime + 10
        } else {
            // Clear weather
            if (world.hasStorm()) {
                world.setStorm(false)
                world.isThundering = false
            }
            
            SleepVoteManager.endVote()
            cancel()
        }
    }
}
