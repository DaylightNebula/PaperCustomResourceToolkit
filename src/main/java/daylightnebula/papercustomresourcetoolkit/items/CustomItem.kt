package daylightnebula.papercustomresourcetoolkit.items

import daylightnebula.papercustomresourcetoolkit.addItemWithEvent
import daylightnebula.papercustomresourcetoolkit.item
import daylightnebula.papercustomresourcetoolkit.packer.ImageResource
import daylightnebula.papercustomresourcetoolkit.packer.ResourcePack
import daylightnebula.papercustomresourcetoolkit.packer.ResourceType
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.json.JSONObject
import java.io.File
import java.lang.IllegalArgumentException

class CustomItem(
    val id: String,
    matRef: CustomItemMaterialReference,
    val name: String,
    description: String,
    attackDamage: Double = -1.0,
    attackSpeed: Double = -1.0,
    knockback: Double = -1.0,
) {

    companion object {
        val items = hashMapOf<String, CustomItem>() // every item will be stored here, each custom item instance is to be treated as a SINGLETON
        val waitingJSON = mutableListOf<Pair<String, JSONObject>>()

        fun loadJSONFromFolder(folder: File) {
            // loop through all json files
            folder.listFiles()?.forEach { file ->
                if (file.extension == "item") {
                    val id = file.nameWithoutExtension
                    waitingJSON.add(Pair(id, JSONObject(file.readText())))
                } else if (file.isDirectory)
                    loadJSONFromFolder(file)
            }
        }

        fun loadRemainingJSON() {
            waitingJSON.forEach { CustomItem(it.first, it.second) }
            waitingJSON.clear()
        }

        private fun convertStringToMatRef(str: String): CustomItemMaterialReference {
            val mat = Material.values().firstOrNull { it.name.equals(str, ignoreCase = true) }
            return if (mat != null) MCMaterialReference(mat) else CustomMaterialReference(str)
        }
    }

    constructor(id: String): this(
        id,
        waitingJSON.firstOrNull { it.first.equals(id, true) }?.second
            ?: throw IllegalArgumentException("Could not find waiting json with id $id")
    ) {
        waitingJSON.removeIf { it.first.equals(id, true) }
    }
    constructor(id: String, json: JSONObject): this(
        id,
        convertStringToMatRef(json.getString("sourceMaterial")),
        json.getString("displayName"),
        json.getString("description"),
        json.optDouble("damage", -1.0),
        json.optDouble("speed", -1.0),
        json.optDouble("knockback", -1.0)
    )

    private val itemStack = item(
        matRef.getMaterial(),
        customModelData = matRef.getCustomModelID(),
        name = name, description = description,
        customItemReferenceID = id,
        attackDamage = attackDamage,
        attackSpeed = attackSpeed,
        knockback = knockback
    )

    init {
        // save item
        items[id] = this
    }

    fun giveToPlayer(player: Player, amount: Int = 1) {
        for (i in 0 until amount) {
            player.inventory.addItemWithEvent(itemStack)
        }
    }
}
abstract class CustomItemMaterialReference() {
    abstract fun getMaterial(): Material
    abstract fun getCustomModelID(): Int
}
class MCMaterialReference(private val material: Material): CustomItemMaterialReference() {
    override fun getMaterial(): Material = material
    override fun getCustomModelID(): Int = 0
}
class CustomMaterialReference(name: String): CustomItemMaterialReference() {
    private val resource = ResourcePack.getResource(ResourceType.IMAGE, name) as? ImageResource ?: throw IllegalArgumentException("No texture registered with name $name")
    override fun getMaterial(): Material = resource.material
    override fun getCustomModelID(): Int = resource.customModelID
}