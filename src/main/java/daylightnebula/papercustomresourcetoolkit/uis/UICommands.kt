package daylightnebula.papercustomresourcetoolkit.uis

import daylightnebula.papercustomresourcetoolkit.packer.FontResource
import daylightnebula.papercustomresourcetoolkit.packer.ResourcePack
import daylightnebula.papercustomresourcetoolkit.packer.ResourceType
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TestUICommand: CommandExecutor {

    lateinit var testUIStack: UIStack

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        val player = sender as? Player ?: return true

        if (!this::testUIStack.isInitialized) {
            testUIStack = UIStack(
                UIAlignment.CENTER,
                UIBounds(300, 300),
                mutableListOf(
                    TextUIComponent(
                        ResourcePack.getResource(ResourceType.FONT, "default") as FontResource, "Test UI"
                    ),
                    TextUIComponent(
                        ResourcePack.getResource(ResourceType.FONT, "test") as FontResource, "Other Line"
                    )
                )
            )
        }

        val comp = UIManager.render(testUIStack)
        player.sendActionBar(comp)
        return true
    }
}