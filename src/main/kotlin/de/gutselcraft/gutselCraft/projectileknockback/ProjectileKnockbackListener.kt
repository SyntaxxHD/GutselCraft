package de.gutselcraft.gutselCraft.projectileknockback

import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class ProjectileKnockbackListener : Listener {
    
    @EventHandler(priority = EventPriority.NORMAL)
    fun onEntityHit(event: EntityDamageByEntityEvent) {
        // Early return if not a player
        val player = event.entity as? Player ?: return
        
        // Check if projectile type matches
        when (event.damager.type) {
            EntityType.SNOWBALL, EntityType.EGG, EntityType.ENDER_PEARL -> {
                // Set minimal damage
                event.damage = 1.0E-4
                
                // Apply knockback in projectile direction
                val knockbackDirection = event.damager.location.direction
                knockbackDirection.multiply(1.25)
                
                player.velocity = knockbackDirection
            }
            else -> return
        }
    }
}
