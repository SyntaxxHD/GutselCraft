package de.gutselcraft.gutselCraft.welcomebook

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
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
                Component.text("Vor dem Start bitte kurz durchlesen!\n\n")
                    .color(NamedTextColor.DARK_GREEN)
                    .decorate(TextDecoration.BOLD)
            )
            .append(
                Component.text("Server Befehle:\n\n")
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
        val serverMods = config.getMapList("welcome-book.server-supported-mods")

        val page = Component.text()
            .append(
                Component.text("Fabric Mods\n")
                    .color(NamedTextColor.DARK_GREEN)
                    .decorate(TextDecoration.BOLD)
            )
            .append(
                Component.text("(Vom Server unterstützt)\n\n")
                    .color(NamedTextColor.DARK_GREEN)
            )
            .append(
                Component.text("Diese Mods sind vom Server unterstützt und sind dringend empfohlen:\n\n")
                    .color(NamedTextColor.BLACK)
            )

        // Add each server-supported mod
        serverMods.forEach { modMap ->
            val modName = modMap["name"] as? String ?: ""
            val modLink = modMap["link"] as? String
            
            page.append(
                Component.text("• ")
                    .color(NamedTextColor.DARK_GREEN)
            )
            
            // Create mod name component
            val modComponent = Component.text("$modName\n")
                .color(NamedTextColor.DARK_GREEN)
                .decorate(TextDecoration.UNDERLINED)
            
            // Add click event if link is provided
            if (modLink != null && modLink.isNotEmpty()) {
                page.append(modComponent.clickEvent(ClickEvent.openUrl(modLink)))
            } else {
                page.append(modComponent)
            }
        }

        return page.build()
    }

    private fun createRecommendedModsPage(): Component {
        val config = plugin.config
        val recommendedMods = config.getMapList("welcome-book.recommended-mods")

        val page = Component.text()
            .append(
                Component.text("Empfohlene Fabric Mods\n\n")
                    .color(NamedTextColor.DARK_GREEN)
                    .decorate(TextDecoration.BOLD)
            )
            .append(
                Component.text("Folgende Mods sind empfohlen die beste Spielerfahrung:\n\n")
                    .color(NamedTextColor.BLACK)
            )

        // Add each recommended mod
        recommendedMods.forEach { modMap ->
            val modName = modMap["name"] as? String ?: ""
            val modLink = modMap["link"] as? String
            
            page.append(
                Component.text("• ")
                    .color(NamedTextColor.DARK_GREEN)
            )
            
            // Create mod name component
            val modComponent = Component.text("$modName\n")
                .color(NamedTextColor.DARK_GREEN)
                .decorate(TextDecoration.UNDERLINED)
            
            // Add click event if link is provided
            if (modLink != null && modLink.isNotEmpty()) {
                page.append(modComponent.clickEvent(ClickEvent.openUrl(modLink)))
            } else {
                page.append(modComponent)
            }
        }

        return page.build()
    }
}
