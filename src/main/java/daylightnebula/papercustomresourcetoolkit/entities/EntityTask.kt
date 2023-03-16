package daylightnebula.papercustomresourcetoolkit.entities

import daylightnebula.papercustomresourcetoolkit.entities.tasks.AttackNearbyPlayersTask
import daylightnebula.papercustomresourcetoolkit.entities.tasks.WanderNearPointTask
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Mob
import org.json.JSONArray
import org.json.JSONObject

abstract class EntityTask {
    companion object {
        fun convertJSONArrayToTasks(jsonArray: JSONArray?): Array<EntityTask?> {
            if (jsonArray == null) return arrayOf()
            return Array(jsonArray.length()) { idx -> convertJSONObjectToTask(jsonArray.get(idx) as JSONObject) }
        }

        fun convertJSONObjectToTask(json: JSONObject): EntityTask? {
            return when (json.getString("type").lowercase()) {
                "stay" -> StayAtPointTask(json)
                "wander" -> WanderNearPointTask(json)
                "attack_players" -> AttackNearbyPlayersTask(json)
                else -> null
            }
        }
    }

    abstract fun getPriority(handler: CustomMob, entity: Mob): Float
    abstract fun startForEntity(handler: CustomMob, entity: Mob)
    abstract fun updateForEntity(handler: CustomMob, entity: Mob)
    abstract fun stopForEntity(handler: CustomMob, entity: Mob)
}
class StayAtPointTask(
    private val location: Location? = null
): EntityTask() {

    constructor(json: JSONObject): this(
        Location(
            Bukkit.getWorlds()[json.optInt("world", 0)],
            json.getJSONArray("location").getDouble(0),
            json.getJSONArray("location").getDouble(1),
            json.getJSONArray("location").getDouble(2),
        )
    )

    private fun getCenterLocation(handler: CustomMob, mob: Mob): Location {
        return location ?: handler.entities[mob]!!.first
    }
    override fun getPriority(handler: CustomMob, entity: Mob): Float { return 0.01f }
    override fun startForEntity(handler: CustomMob, entity: Mob) {}
    override fun stopForEntity(handler: CustomMob, entity: Mob) {}
    override fun updateForEntity(handler: CustomMob, entity: Mob) {
        if (entity.location.distanceSquared(getCenterLocation(handler, entity)) > 0.1f)
            entity.pathfinder.moveTo(getCenterLocation(handler, entity))
    }
}