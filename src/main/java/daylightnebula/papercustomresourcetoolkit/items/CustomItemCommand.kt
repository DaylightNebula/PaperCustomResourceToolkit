package daylightnebula.papercustomresourcetoolkit.items

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

// /getcustomitem <item_id or name> [amount]
class CustomItemCommand: CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        // make sure sender is a player, if not cancel
        if (sender !is Player) {
            sender.sendMessage("This command must be used by players")
            return false
        }
        if (!sender.isOp) return false

        // make sure arguments are of the right length, if not, send argument template and cancel
        if (args == null || args.size < 1) {
            sender.sendMessage("/getcustomitem <item_id or name> [amount]")
            return true
        }

        // get given id or name
        val identifier = args[0]

        // try to get a custom name with the given mode and identifier
        val item = CustomItem.items[identifier]

        // if an item was not found with the given mode and identifier, error and cancel
        if (item == null) {
            sender.sendMessage("Could not find an item with an id of $identifier, options are: ${CustomItem.items.keys}")
            return true
        }

        // if a third argument is given, try to convert it into an int for an amount
        var amount = 1
        if (args.size >= 2)
            amount = args[1].toIntOrNull() ?: 1

        // give the item to the player
        item.giveToPlayer(sender)

        return true
    }
}