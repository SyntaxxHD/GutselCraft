package de.gutselcraft.gutselCraft.antidespawn.listeners

import org.bukkit.NamespacedKey
import org.bukkit.entity.Item
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.ItemDespawnEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import java.util.UUID

class DeathItemProtectionListener(private val plugin: Plugin) : Listener {
    
    private val deathItemKey = NamespacedKey(plugin, "death_item")
    
    // Temporary storage for death drops to match against spawned items
    private val pendingDeathDrops = mutableMapOf<UUID, MutableList<ItemStack>>()
    
    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (!plugin.config.getBoolean("anti-despawn.enabled", true)) {
            return
        }
        
        val despawnTime = plugin.config.getInt("anti-despawn.despawn-time", -1)
        val playerId = event.player.uniqueId
        
        // Store the death drops for matching
        if (event.drops.isNotEmpty()) {
            pendingDeathDrops[playerId] = event.drops.map { it.clone() }.toMutableList()
        }
        
        // Schedule a task to mark the items after they spawn
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            val location = event.player.location
            val nearbyItems = location.world?.getNearbyEntities(location, 5.0, 5.0, 5.0)
                ?.filterIsInstance<Item>()
                ?.toMutableList() ?: return@Runnable
            
            val expectedDrops = pendingDeathDrops.remove(playerId) ?: return@Runnable
            
            // Match spawned items with expected death drops
            expectedDrops.forEach { expectedStack ->
                val matchingItem = nearbyItems.firstOrNull { item ->
                    val itemStack = item.itemStack
                    itemStack.type == expectedStack.type &&
                    itemStack.amount == expectedStack.amount &&
                    itemStack.itemMeta == expectedStack.itemMeta
                }
                
                matchingItem?.let { item ->
                    // Remove from list so we don't match it again
                    nearbyItems.remove(item)
                    
                    // Mark the item as a death drop
                    item.persistentDataContainer.set(deathItemKey, PersistentDataType.BYTE, 1)
                    
                    // Configure despawn time
                    when {
                        despawnTime == -1 -> {
                            // Never despawn
                            item.setUnlimitedLifetime(true)
                        }
                    despawnTime > 0 -> {
                        // Custom despawn time
                        val targetTicks = despawnTime * 20 // Convert seconds to ticks
                        item.ticksLived = 1
                        item.setUnlimitedLifetime(false)
                            
                            // Schedule despawn
                            plugin.server.scheduler.runTaskLater(plugin, Runnable {
                                if (item.isValid) {
                                    item.remove()
                                }
                            }, targetTicks.toLong())
                        }
                        else -> {
                            // despawnTime == 0, use normal despawn behavior
                            item.setUnlimitedLifetime(false)
                        }
                    }
                }
            }
        }, 1L) // Run after 1 tick to ensure items have spawned
        
        // Clean up old pending drops after 5 seconds (just in case)
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            pendingDeathDrops.remove(playerId)
        }, 100L)
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    fun onItemDespawn(event: ItemDespawnEvent) {
        val item = event.entity
        
        // Check if this item is marked as a death drop
        if (item.persistentDataContainer.has(deathItemKey, PersistentDataType.BYTE)) {
            // Check if feature is still enabled
            if (!plugin.config.getBoolean("anti-despawn.enabled", true)) {
                return
            }
            
            val despawnTime = plugin.config.getInt("anti-despawn.despawn-time", -1)
            
            // If set to never despawn or custom time, cancel the natural despawn event
            if (despawnTime == -1) {
                event.isCancelled = true
            }
            // For custom times, we handle it with scheduled tasks, so cancel natural despawn
            else if (despawnTime > 0) {
                event.isCancelled = true
            }
        }
    }
}
