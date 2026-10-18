package de.gutselcraft.gutselCraft.votesleep

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import java.util.function.Consumer

/**
 * Smoothly advances the time of the vote world until morning. Must be scheduled on the global
 * region scheduler, which owns world time and weather on Folia.
 */
class NightSkipTask : Consumer<ScheduledTask> {

    override fun accept(task: ScheduledTask) {
        val world = SleepVoteManager.getVoteWorld()

        if (world == null) {
            task.cancel()
            return
        }

        val currentTime = world.time

        if (currentTime >= 12541) {
            world.time = currentTime + 100
        } else {
            // Clear weather
            if (world.hasStorm()) {
                world.setStorm(false)
                world.isThundering = false
            }

            SleepVoteManager.endVote()
            task.cancel()
        }
    }
}
