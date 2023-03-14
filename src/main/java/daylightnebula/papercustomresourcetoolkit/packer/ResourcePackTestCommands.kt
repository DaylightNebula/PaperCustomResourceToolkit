package daylightnebula.papercustomresourcetoolkit.packer

import daylightnebula.papercustomresourcetoolkit.PaperCustomResourceToolkit
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask

val activeTestStands = hashMapOf<BukkitTask, List<ArmorStand>>()
class CreateAnimatedModelCommand: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        ResourcePack.getAllResourcesOfType(ResourceType.TEXT_IMAGE)?.forEach {
            sender.sendMessage("Text Image Resource named ${it.name} = ${(it as TextImageResource).offset.toChar()}")
        }

//        if (args == null || args.isEmpty()) {
//            sender.sendMessage("No args supplied")
//            return true
//        }
//        if (!sender.isOp || sender !is Player) {
//            sender.sendMessage("Must be oped player")
//            return true
//        }
//
//        val resource = ResourcePack.getResource(args.first()) as? AnimatedModelResource
//
//        if (resource == null) {
//            sender.sendMessage("No resource found with name ${args.first()}")
//            return true
//        }
//
//        val stands = mutableListOf<ArmorStand>()
//        resource.default.stack.forEach { pair ->
//            val armorStand = sender.world.spawn(sender.location, ArmorStand::class.java)
//            armorStand.setGravity(false)
//            armorStand.isInvisible = true
//            armorStand.isPersistent = false
//            armorStand.equipment.helmet = ItemStack(pair.first).apply {
//                val meta = this.itemMeta
//                meta.setCustomModelData(pair.second)
//                itemMeta = meta
//            }
//            stands.add(armorStand)
//        }
//        sender.sendMessage("Done")
//
//        var idx = 0
//        var frameIdx = 0
//        val task = Bukkit.getScheduler().runTaskTimer(PaperCustomResourceToolkit.plugin,
//            Runnable {
//                // update frame and animation index counters
//                frameIdx++
//                if (frameIdx >= resource.animations[idx].frames.frames.size) {
//                    frameIdx = 0
////                    idx++
//                }
////                if (idx >= resource.animations.size)
////                    idx = 0
//
//                // update
//                val frame = resource.animations[idx].frames.frames[frameIdx]
//                stands.forEachIndexed { stackIdx, armorStand ->
//                    if (!frame.stack.indices.contains(stackIdx)) return@forEachIndexed
//                    val pair = frame.stack[stackIdx]
//                    armorStand.equipment.helmet = ItemStack(pair.first).apply {
//                        val meta = this.itemMeta
//                        meta.setCustomModelData(pair.second)
//                        this.itemMeta = meta
//                    }
//                }
//        }, 1L, 1L)
//        activeTestStands[task] = stands

        return true
    }
}

class RemoveAnimatedModelsCommand: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (!sender.isOp || sender !is Player) {
            sender.sendMessage("Must be oped player")
            return true
        }
        activeTestStands.forEach {
            it.key.cancel()
            it.value.forEach { ass -> ass.remove() }
        }
        return true
    }
}