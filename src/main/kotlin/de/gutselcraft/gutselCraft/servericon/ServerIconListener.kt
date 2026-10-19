package de.gutselcraft.gutselCraft.servericon

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerListPingEvent
import org.bukkit.plugin.Plugin
import org.bukkit.util.CachedServerIcon
import java.util.concurrent.ThreadLocalRandom
import javax.imageio.ImageIO

class ServerIconListener(private val plugin: Plugin) : Listener {

    // Immutable list swapped atomically: pings run on network threads
    @Volatile
    private var icons: List<CachedServerIcon> = emptyList()

    init {
        reload()
    }

    fun reload() {
        val folder = plugin.dataFolder.resolve("server-icons")
        if (!folder.exists()) folder.mkdirs()

        icons = folder.listFiles { file -> file.isFile && file.extension.equals("png", ignoreCase = true) }
            ?.sortedBy { it.name }
            ?.mapNotNull { file ->
                try {
                    val image = ImageIO.read(file) ?: throw IllegalArgumentException("not a valid image")
                    plugin.server.loadServerIcon(image)
                } catch (e: Exception) {
                    plugin.logger.warning("Could not load server icon ${file.name}: ${e.message}")
                    null
                }
            }
            ?: emptyList()
    }

    @EventHandler
    fun onServerListPing(event: ServerListPingEvent) {
        if (!plugin.config.getBoolean("server-icon.enabled", false)) return

        val current = icons
        if (current.isEmpty()) return
        event.setServerIcon(current[ThreadLocalRandom.current().nextInt(current.size)])
    }
}
