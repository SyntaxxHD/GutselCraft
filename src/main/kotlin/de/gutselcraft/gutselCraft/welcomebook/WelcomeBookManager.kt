package de.gutselcraft.gutselCraft.welcomebook

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.plugin.Plugin

class WelcomeBookManager(private val plugin: Plugin) {

    fun createWelcomeBook(): ItemStack {
        val book = ItemStack(Material.WRITTEN_BOOK)
        val meta = book.itemMeta as BookMeta

        meta.title(Component.text("GutselCraft Guide").color(NamedTextColor.DARK_GREEN))
        meta.author(Component.text("GutselCraft Team"))

        // Add all pages
        meta.addPages(
            createCommandsPage(),
            createServerModsPage(),
            createRecommendedModsPage()
        )

        book.itemMeta = meta
        return book
    }

    private fun createCommandsPage(): Component {
        val config = plugin.config
        val commands = config.getMapList("welcome-book.commands")

        val page = Component.text()
            .append(
                Component.text("═══════════════\n")
                    .color(NamedTextColor.DARK_GREEN)
                    .decorate(TextDecoration.BOLD)
            )
            .append(
                Component.text("Server Befehle\n")
                    .color(NamedTextColor.DARK_GREEN)
                    .decorate(TextDecoration.BOLD)
            )
            .append(
                Component.text("═══════════════\n\n")
                    .color(NamedTextColor.DARK_GREEN)
                    .decorate(TextDecoration.BOLD)
            )

        // Add each command from config
        commands.forEach { commandMap ->
            val command = commandMap["command"] as? String ?: ""
            val description = commandMap["description"] as? String ?: ""
            val note = commandMap["note"] as? String
            
            page.append(
                Component.text("$command\n")
                    .color(NamedTextColor.DARK_GREEN)
                    .decorate(TextDecoration.BOLD)
            )
            page.append(
                Component.text("$description\n")
                    .color(NamedTextColor.BLACK)
            )
            
            // Add note if present
            if (note != null) {
                page.append(
                    Component.text("$note\n")
                        .color(NamedTextColor.GRAY)
                )
            }
            
            // Add spacing between commands
            page.append(Component.text("\n"))
        }

        return page.build()
    }

    private fun createServerModsPage(): Component {
        val config = plugin.config
        val serverMods = config.getStringList("welcome-book.server-supported-mods")

        val page = Component.text()
            .append(
                Component.text("═══════════════\n")
                    .color(NamedTextColor.DARK_GREEN)
                    .decorate(TextDecoration.BOLD)
            )
            .append(
                Component.text("Fabric Mods\n")
                    .color(NamedTextColor.DARK_GREEN)
                    .decorate(TextDecoration.BOLD)
            )
            .append(
                Component.text("Server-Unterstützt\n")
                    .color(NamedTextColor.DARK_GREEN)
                    .decorate(TextDecoration.BOLD)
            )
            .append(
                Component.text("═══════════════\n\n")
                    .color(NamedTextColor.DARK_GREEN)
                    .decorate(TextDecoration.BOLD)
            )
            .append(
                Component.text("Diese Mods nutzen\n")
                    .color(NamedTextColor.BLACK)
            )
            .append(
                Component.text("Server-Features:\n\n")
                    .color(NamedTextColor.BLACK)
            )

        // Add each server-supported mod
        serverMods.forEach { mod ->
            page.append(
                Component.text("• ")
                    .color(NamedTextColor.DARK_GREEN)
            )
            page.append(
                Component.text("$mod\n")
                    .color(NamedTextColor.DARK_GREEN)
                    .decorate(TextDecoration.BOLD)
            )
        }

        return page.build()
    }

    private fun createRecommendedModsPage(): Component {
        val config = plugin.config
        val recommendedMods = config.getStringList("welcome-book.recommended-mods")

        val page = Component.text()
            .append(
                Component.text("═══════════════\n")
                    .color(NamedTextColor.DARK_GREEN)
                    .decorate(TextDecoration.BOLD)
            )
            .append(
                Component.text("Empfohlene\n")
                    .color(NamedTextColor.DARK_GREEN)
                    .decorate(TextDecoration.BOLD)
            )
            .append(
                Component.text("Client Mods\n")
                    .color(NamedTextColor.DARK_GREEN)
                    .decorate(TextDecoration.BOLD)
            )
            .append(
                Component.text("═══════════════\n\n")
                    .color(NamedTextColor.DARK_GREEN)
                    .decorate(TextDecoration.BOLD)
            )
            .append(
                Component.text("Für die beste\n")
                    .color(NamedTextColor.BLACK)
            )
            .append(
                Component.text("Spielerfahrung:\n\n")
                    .color(NamedTextColor.BLACK)
            )

        // Add each recommended mod
        recommendedMods.forEach { mod ->
            page.append(
                Component.text("• ")
                    .color(NamedTextColor.DARK_GREEN)
            )
            page.append(
                Component.text("$mod\n")
                    .color(NamedTextColor.DARK_GREEN)
                    .decorate(TextDecoration.BOLD)
            )
        }

        return page.build()
    }
}
