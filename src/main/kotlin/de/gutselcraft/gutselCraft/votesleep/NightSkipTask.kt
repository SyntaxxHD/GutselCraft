package de.gutselcraft.gutselCraft.votesleep

import org.bukkit.scheduler.BukkitRunnable

class NightSkipTask : BukkitRunnable() {
    
    override fun run() {
        val world = SleepVoteManager.getVoteWorld()
        
        if (world == null) {
            cancel()
            return
        }
        
        val currentTime = world.time
        
        // Smoothly transition from night to day
        if (currentTime >= 12541) {
            world.time = currentTime + 100
        } else {
            // Reached day time
            world.time = 0
            SleepVoteManager.endVote()
            cancel()
        }
    }
}
