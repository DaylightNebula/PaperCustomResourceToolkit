package daylightnebula.papercustomresourcetoolkit.entities

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SpawnMobCommand: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        // make sure sender is a player, if not cancel
        if (sender !is Player) {
            sender.sendMessage("This command must be used by players")
            return false
        }
        if (!sender.isOp) return false

        // make sure arguments are of the right length, if not, send argument template and cancel
        if (args == null || args.isEmpty()) {
            sender.sendMessage("/spawnmob <mob id>")
            return true
        }

        // try to get the mob with the given id
        val mobID = args.first()
        val mob = CustomMob.mobs[mobID]
        if (mob == null) {
            sender.sendMessage("Could not find a mob with id $mobID, options are ${CustomMob.mobs.map { it.key }}")
            return true
        }

        // spawn the mob at the player
        mob.spawnEntityAtLocation(sender.location, true)

        return true
    }
}
class RemoveNearbyMobsCommand: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        // make sure sender is a player, if not cancel
        if (sender !is Player) {
            sender.sendMessage("This command must be used by players")
            return false
        }
        if (!sender.isOp) return false

        // make sure arguments are of the right length, if not, send argument template and cancel
        if (args == null || args.size != 2) {
            sender.sendMessage("/removenearbymobs <mob_id or * for all mobs> <range>")
            return true
        }

        // try to get the mob with the given id
        val mobID = args.first()
        val mob = CustomMob.mobs[mobID]
        if (mob == null && mobID != "*") {
            sender.sendMessage("Could not find a mob with id $mobID")
            return true
        }

        // try to get the remove range
        val range = args.last().toFloatOrNull()
        if (range == null) {
            sender.sendMessage("The range given, ${args.last()}, could not be converted to a usable float.")
            return true
        }

        // remove nearby entities
        if (mob == null)
            CustomMob.mobs.forEach { it.value.removeInRange(sender.location, range) }
        else
            mob.removeInRange(sender.location, range)

        // success
        return true
    }
}