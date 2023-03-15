package daylightnebula.papercustomresourcetoolkit.items

import daylightnebula.papercustomresourcetoolkit.getCustomItem
import daylightnebula.papercustomresourcetoolkit.isCustomItem
import net.axay.kspigot.extensions.events.interactItem
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class CustomItemEventListener: Listener {
    @EventHandler
    fun onEntityPickupItemEvent(event: EntityPickupItemEvent) {
        // make sure we have a custom item
        val player = event.entity as? Player ?: return
        if (!event.item.itemStack.isCustomItem()) return

        // call event
        val newEvent = CustomItemPickupEvent(player, event.item.itemStack, event)
        Bukkit.getPluginManager().callEvent(newEvent)

        // call custom items listeners
        event.item.itemStack.getCustomItem()?.pickupListeners?.forEach { it(newEvent) }
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        // make sure we have a custom item
        if (event.item == null) return
        if (!event.item!!.isCustomItem()) return

        // call event
        val newEvent = CustomItemInteractEvent(event.item!!, event.action, event.clickedBlock, event)
        Bukkit.getPluginManager().callEvent(newEvent)

        // call custom items listeners
        event.item?.getCustomItem()?.interactListeners?.forEach { it(newEvent) }
    }

    @EventHandler
    fun onPlayerInteractEntityEvent(event: PlayerInteractEntityEvent) {
        // make sure we have a custom item
        if (event.interactItem == null) return
        if (!event.interactItem!!.isCustomItem()) return

        // call event
        val newEvent = CustomItemEntityInteractEvent(event.interactItem!!, event.rightClicked, event)
        Bukkit.getPluginManager().callEvent(newEvent)

        // call custom items listeners
        event.interactItem?.getCustomItem()?.interactEntityListeners?.forEach { it(newEvent) }
    }

    @EventHandler
    fun onEntityDamageByEntityEvent(event: EntityDamageByEntityEvent) {
        // make sure we have an item and player
        val player = event.damager as? Player ?: return
        val item = player.inventory.itemInMainHand ?: return

        // make sure item is event
        if (!item.isCustomItem()) return

        // call event
        val newEvent = CustomItemAttackEntityEvent(item, event.entity, event)
        Bukkit.getPluginManager().callEvent(newEvent)

        // call custom items listeners
        item.getCustomItem()?.attackListeners?.forEach { it(newEvent) }
    }
}
class CustomItemInteractEvent(val item: ItemStack, val action: Action, val block: Block?, val srcEvent: PlayerInteractEvent): Event() {
    companion object {
        @JvmStatic
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }
    override fun getHandlers(): HandlerList {
        return handlerList
    }
}
class CustomItemEntityInteractEvent(val item: ItemStack, val rightClicked: Entity, val srcEvent: PlayerInteractEntityEvent): Event() {
    companion object {
        @JvmStatic
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }
    override fun getHandlers(): HandlerList {
        return handlerList
    }
}
class CustomItemAttackEntityEvent(val item: ItemStack, val target: Entity, val srcEvent: EntityDamageByEntityEvent): Event() {
    companion object {
        @JvmStatic
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }
    override fun getHandlers(): HandlerList {
        return handlerList
    }
}
class CustomItemPickupEvent(val player: Player, val item: ItemStack, val srcEvent: EntityPickupItemEvent): Event() {
    companion object {
        @JvmStatic
        private val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }
    override fun getHandlers(): HandlerList {
        return handlerList
    }
}