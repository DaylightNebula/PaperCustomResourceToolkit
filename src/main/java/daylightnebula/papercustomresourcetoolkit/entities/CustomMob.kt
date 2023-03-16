package daylightnebula.papercustomresourcetoolkit.entities

import com.ticxo.modelengine.api.ModelEngineAPI
import daylightnebula.papercustomresourcetoolkit.PaperCustomResourceToolkit
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.Mob
import org.json.JSONObject
import java.io.File
import kotlin.IllegalArgumentException
import kotlin.math.pow

class CustomMob(
    val id: String,
    displayName: String,
    val supertype: CustomMobType,

    // stats
    private val maxHealth: Double = -1.0,
    private val armor: Double = -1.0,
    private val armorToughness: Double = -1.0,
    private val movementSpeed: Double = -1.0,
    private val attackDamage: Double = -1.0,
    private val attackKnockBack: Double = -1.0,
    private val attackSpeed: Double = -1.0,
    private val followRange: Double = -1.0,
    private val flyingSpeed: Double = -1.0,
    private val knockBackResistance: Double = -1.0,
    private val luck: Double = -1.0,

    // other stuffs
    private val tasks: Array<EntityTask?> = arrayOf(),
    private val onMobCreate: (mob: Mob) -> Unit = {}
) {
    companion object {
        val mobs = hashMapOf<String, CustomMob>()
        val waitingJSON = mutableListOf<Pair<String, JSONObject>>()

        fun loadJSONFromFolder(folder: File) {
            // loop through all json files
            folder.listFiles()?.forEach { file ->
                if (file.extension == "json") {
                    val id = file.nameWithoutExtension
                    waitingJSON.add(Pair(id, JSONObject(file.readText())))
                } else if (file.isDirectory)
                    loadJSONFromFolder(file)
            }
        }

        fun loadRemainingJSON() {
            waitingJSON.forEach { CustomMob(it.first, it.second) }
            waitingJSON.clear()
        }

        fun startUpdateLoop() {
            Bukkit.getScheduler().runTaskTimer(PaperCustomResourceToolkit.plugin, Runnable {
                mobs.forEach { it.value.updateAllEntities() }
            }, 1L, 1L)
        }

        fun removeAllActiveEntities() {
            mobs.forEach { mob -> mob.value.removeAll() }
        }

        fun disable() {
            mobs.forEach { it.value.disable() }
        }

        fun convertStringToCustomMobType(str: String): CustomMobType {
            val entityType = EntityType.values().firstOrNull { it.name.equals(str, ignoreCase = true) }
            return if (entityType != null) MCMobType(entityType)
            else CustomModelMobType(str)
        }
    }

    val dnComponents = Component.text(displayName)
    val entities = hashMapOf<Mob, Triple<Location, Int, Boolean>>() // Format: bukkit entity relative to spawn location, current task index, and save

    init {
        mobs[id] = this
    }
    constructor(id: String, json: JSONObject, onMobCreate: (mob: Mob) -> Unit = {}): this(
        id,
        json.getString("displayName"),
        convertStringToCustomMobType(json.getString("supertype")),
        json.optDouble("maxHealth", -1.0),
        json.optDouble("armor", -1.0),
        json.optDouble("armorToughness", -1.0),
        json.optDouble("movementSpeed", -1.0),
        json.optDouble("attackDamage", -1.0),
        json.optDouble("attackKnockBack", -1.0),
        json.optDouble("attackSpeed", -1.0),
        json.optDouble("followRange", -1.0),
        json.optDouble("flyingSpeed", -1.0),
        json.optDouble("knockBackResistance", -1.0),
        json.optDouble("luck", -1.0),
        EntityTask.convertJSONArrayToTasks(json.optJSONArray("tasks")),
        onMobCreate
    )
    constructor(id: String, onMobCreate: (mob: Mob) -> Unit): this(
        id, waitingJSON.firstOrNull { it.first == id }?.second ?: throw IllegalArgumentException("Not waiting json with id $id"), onMobCreate
    ) {
        waitingJSON.removeIf { it.first == id }
    }

    fun spawnEntityAtLocation(location: Location, save: Boolean): Mob {
        // spawn entity and make sure it is a mob
        val entity = supertype.spawn(location)

        // set flags
        entity.isPersistent = false
        entity.isSilent = true
        entity.removeWhenFarAway = false
        entity.canPickupItems = false
        entity.equipment.clear()

        Bukkit.getMobGoals().removeAllGoals(entity)

        // set stats
        if (maxHealth != -1.0) entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = maxHealth
        if (armor != -1.0) entity.getAttribute(Attribute.GENERIC_ARMOR)?.baseValue = armor
        if (armorToughness != -1.0) entity.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS)?.baseValue = armorToughness
        if (movementSpeed != -1.0) entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue = movementSpeed
        if (attackSpeed != -1.0) entity.getAttribute(Attribute.GENERIC_ATTACK_SPEED)?.baseValue = attackSpeed
        if (attackDamage != -1.0) entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.baseValue = attackDamage
        if (attackKnockBack != -1.0) entity.getAttribute(Attribute.GENERIC_ATTACK_KNOCKBACK)?.baseValue = attackKnockBack
        if (followRange != -1.0) entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE)?.baseValue = followRange
        if (knockBackResistance != -1.0) entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)?.baseValue = knockBackResistance
        if (flyingSpeed != -1.0) entity.getAttribute(Attribute.GENERIC_FLYING_SPEED)?.baseValue = flyingSpeed
        if (luck != -1.0) entity.getAttribute(Attribute.GENERIC_LUCK)?.baseValue = luck

        // set display name
        if (dnComponents.content().isNotBlank()) {
            entity.customName(dnComponents)
            entity.isCustomNameVisible = true
        }

        // call on mob create
        onMobCreate(entity)

        // track the entity
        entities[entity] = Triple(location, getTaskForEntity(entity), save)
        startCurrentTaskForEntity(entity)

        // return the final entity
        return entity
    }

    private fun updateAllEntities() {
        // remove dead entities
        val deadEntities = entities.keys.filter { it.isDead }
        deadEntities.forEach {
            stopCurrentTaskForEntity(it)
            entities.remove(it)
        }

        // for each active entity
        entities.forEach { (mob, pair) ->
            val curTaskIdx = pair.second

            // check if its task has changed
            val taskID = getTaskForEntity(mob)
            if (taskID == curTaskIdx) {
                // if task has not changed, update and cancel
                tasks[taskID]!!.updateForEntity(this, mob)
                return@forEach
            }

            // stop the current task and swap to and then star the next task
            stopCurrentTaskForEntity(mob)
            entities[mob] = Triple(entities[mob]?.first ?: mob.location, taskID, entities[mob]?.third ?: false)
            startCurrentTaskForEntity(mob)
        }
    }

    private fun getTaskForEntity(entity: Mob): Int {
        val task = tasks.maxBy { it?.getPriority(this, entity) ?: -1f }
        return tasks.indexOf(task)
    }

    private fun startCurrentTaskForEntity(entity: Mob) {
        tasks[entities[entity]?.second ?: return]!!.startForEntity(this, entity)
    }

    private fun stopCurrentTaskForEntity(entity: Mob) {
        tasks[entities[entity]?.second ?: return]!!.stopForEntity(this, entity)
    }

    fun removeInRange(location: Location, range: Float) {
        val rangeSq = range.pow(2f)
        val toRemove = entities.filter { it.key.location.distanceSquared(location) < rangeSq }
        toRemove.forEach {
            stopCurrentTaskForEntity(it.key)
            it.key.remove()
            entities.remove(it.key)
        }
    }

    fun removeAll() {
        entities.forEach {
            stopCurrentTaskForEntity(it.key)
            it.key.remove()
        }
        entities.clear()
    }

    fun disable() {
        removeAll()
    }
}
abstract class CustomMobType {
    abstract fun getType(): EntityType
    abstract fun spawn(location: Location): Mob
}
class MCMobType(val entityType: EntityType): CustomMobType() {
    override fun getType(): EntityType = entityType
    override fun spawn(location: Location): Mob {
        return location.world.spawnEntity(location, entityType) as? Mob
            ?: throw IllegalArgumentException("Entity type give must be a mob")
    }
}
class CustomModelMobType(val name: String): CustomMobType() {
    override fun getType(): EntityType = EntityType.ZOMBIE
    override fun spawn(location: Location): Mob {
        val mob = location.world.spawnEntity(location, EntityType.ZOMBIE) as? Mob
            ?: throw IllegalArgumentException("Entity type give must be a mob")
        val model = ModelEngineAPI.createActiveModel(name)
            ?: throw IllegalArgumentException("No custom model with name $name registered with model engine")
        val modeledEntity = ModelEngineAPI.createModeledEntity(mob)
            ?: throw IllegalArgumentException("Could not create modeled entity with model named $name")
        modeledEntity.addModel(model, true)
        modeledEntity.isBaseEntityVisible = false
        return mob
    }
}