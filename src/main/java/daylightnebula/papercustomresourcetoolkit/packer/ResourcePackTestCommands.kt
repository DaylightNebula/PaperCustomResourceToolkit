package daylightnebula.papercustomresourcetoolkit.packer

import daylightnebula.papercustomresourcetoolkit.PaperCustomResourceToolkit
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

val activeTestStands = mutableListOf<ArmorStand>()
class CreateAnimatedModelCommand: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (args == null || args.isEmpty()) {
            sender.sendMessage("No args supplied")
            return true
        }
        if (!sender.isOp || sender !is Player) {
            sender.sendMessage("Must be oped player")
            return true
        }

        val resource = ResourcePack.getResource(args.first()) as? AnimatedModelResource

        if (resource == null) {
            sender.sendMessage("No resource found with name ${args.first()}")
            return true
        }

        resource.default.stack.forEach { pair ->
            val armorStand = sender.world.spawn(sender.location, ArmorStand::class.java)
            armorStand.setGravity(false)
            armorStand.isInvisible = true
            armorStand.isPersistent = false
            armorStand.equipment.helmet = ItemStack(pair.first).apply {
                val meta = this.itemMeta
                meta.setCustomModelData(pair.second)
                itemMeta = meta
            }
            activeTestStands.add(armorStand)
        }
        sender.sendMessage("Done")

        return true
    }
}

class RemoveAnimatedModelsCommand: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (!sender.isOp || sender !is Player) {
            sender.sendMessage("Must be oped player")
            return true
        }
        activeTestStands.forEach { it.remove() }
        return true
    }
}