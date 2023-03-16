package daylightnebula.papercustomresourcetoolkit.entities.tasks

import daylightnebula.papercustomresourcetoolkit.entities.CustomMob
import daylightnebula.papercustomresourcetoolkit.entities.EntityTask
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.Mob
import org.bukkit.util.Vector
import org.json.JSONObject
import kotlin.math.pow
import kotlin.random.Random

class WanderNearPointTask(
    private val location: Location? = null,
    private val wanderRange: Float = 2f,
    private val minTicksBetweenMove: Int = 20,
    private val maxTicksBetweenMove: Int = 100,
    private val takeOverWhenOutOfRange: Boolean = false
): EntityTask() {

    constructor(json: JSONObject): this(
        if (json.has("location")) Location(
            Bukkit.getWorlds()[json.optInt("world", 0)],
            json.getJSONArray("location").getDouble(0),
            json.getJSONArray("location").getDouble(1),
            json.getJSONArray("location").getDouble(2),
        ) else null,
        json.optFloat("range", 2f),
        json.optInt("minWaitTicks", 20),
        json.optInt("maxWaitTicks", 100),
        json.optBoolean("forceWhenOutOfRange", false)
    )

    private val rangeSq = wanderRange.pow(2f)
    private val active = hashMapOf<Entity, MoveEntry>()

    private fun getCenterLocation(handler: CustomMob, entity: Mob): Location {
        return location ?: handler.entities[entity]!!.first
    }

    override fun getPriority(handler: CustomMob, entity: Mob): Float {
        return if (takeOverWhenOutOfRange && entity.location.distanceSquared(getCenterLocation(handler, entity)) > rangeSq)
            1000000f
        else
            1f
    }

    private val heightSearchThreshold = 10.0
    private fun getRandomLocationInRange(handler: CustomMob, mob: Mob): Location? {
        // get xz vector first
        val vec = Vector(Random.nextFloat() - 0.5f, 0f, Random.nextFloat() - 0.5f)
        if (vec.lengthSquared() > 1.0)
            vec.normalize()
        val baseLocation = vec.multiply(wanderRange).toLocation(mob.world).add(getCenterLocation(handler, mob))

        // if this block is valid just return this location
        if (isBlockValid(baseLocation.block)) return baseLocation

        // search along the y-axis for the first valid block
        var i = 1.0
        while (i < heightSearchThreshold) {
            // check if block above is valid
            val above = baseLocation.clone().add(0.0, i, 0.0)
            if (isBlockValid(above.block)) return above

            // check if block below is valid
            val below = baseLocation.clone().add(0.0, -i, 0.0)
            if (isBlockValid(below.block)) return below

            i++
        }

        // if nothing found, return nothing
        return null
    }

    private fun getRandomWaitTime(): Int {
        return Random.nextInt(minTicksBetweenMove, maxTicksBetweenMove)
    }

    private fun isBlockValid(block: Block): Boolean {
        return block.getRelative(BlockFace.DOWN).isSolid && !block.isSolid && !block.getRelative(BlockFace.UP).isSolid
    }

    override fun startForEntity(handler: CustomMob, entity: Mob) {
        val startMoving = entity.location.distanceSquared(getCenterLocation(handler, entity)) > rangeSq || Random.nextBoolean()
        if (startMoving) {
            val targetLocation = getRandomLocationInRange(handler, entity) ?: entity.location
            active[entity] = MoveEntry(
                targetLocation,
                false,
                getRandomWaitTime()
            )
            entity.pathfinder.moveTo(targetLocation)
        } else {
            active[entity] = MoveEntry(
                entity.location,
                true,
                getRandomWaitTime()
            )
        }
    }

    override fun updateForEntity(handler: CustomMob, entity: Mob) {
        // get move entry
        val moveEntry = active[entity] ?: return

        // if the entity is moving, and they have reached the target location, mark them as such
        if (!moveEntry.isWaiting && moveEntry.targetLocation.distanceSquared(entity.location) < 1) {
            moveEntry.isWaiting = true
        } else if (!moveEntry.isWaiting) {
            if (!entity.pathfinder.hasPath())
                entity.pathfinder.moveTo(moveEntry.targetLocation)
        }

        // if the entity is waiting, increment their counter, if there counter has exceeded their wait time, start their next move
        if (moveEntry.isWaiting) {
            moveEntry.waitingTicks++
            if (moveEntry.waitingTicks > moveEntry.maxWaitTicks) {
                val target = getRandomLocationInRange(handler, entity) ?: entity.location
                active[entity] = MoveEntry(
                    target,
                    false,
                    getRandomWaitTime()
                )
                entity.pathfinder.moveTo(target)
            }
        }
    }

    override fun stopForEntity(handler: CustomMob, entity: Mob) {
        entity.pathfinder.moveTo(entity.location)
        active.remove(entity)
    }
}
data class MoveEntry(
    val targetLocation: Location,
    var isWaiting: Boolean,
    val maxWaitTicks: Int,
    var waitingTicks: Int = 0,
) {
    override fun toString(): String {
        return "MoveEntry: Target: (${targetLocation.x}, ${targetLocation.y}, ${targetLocation.z}), isWaiting? $isWaiting, Max Wait: $maxWaitTicks, Waiting Ticks: $waitingTicks"
    }
}