package daylightnebula.papercustomresourcetoolkit.entities.tasks

import daylightnebula.papercustomresourcetoolkit.entities.CustomMob
import daylightnebula.papercustomresourcetoolkit.entities.EntityTask
import org.bukkit.Bukkit
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import org.json.JSONObject
import kotlin.math.pow

class AttackNearbyPlayersTask(
    detectRange: Float = 5f,
    attackRange: Float = 1f,
    private val attackCooldownTicks: Int = 20,
    private val onAttack: ((mob: Mob, target: Player) -> Unit)? = null // if null, do default attack
): EntityTask() {

    constructor(json: JSONObject, onAttack: ((mob: Mob, target: Player) -> Unit)? = null): this(
        json.optFloat("detectRange", 5f),
        json.optFloat("attackRange", 1f),
        json.optInt("cooldown", 20),
        onAttack
    )

    private val dtRangeSq = detectRange.pow(2f)
    private val atRangeSq = attackRange.pow(2f)
    private var curTargets = hashMapOf<Mob, Pair<Player, Int>?>()

    override fun getPriority(handler: CustomMob, entity: Mob): Float {
        // if the current target is null or outside the detection range, try to get a new target
        if (curTargets[entity] == null || curTargets[entity]!!.first.location.distanceSquared(entity.location) > dtRangeSq) {
            curTargets[entity] = Pair(
                Bukkit.getOnlinePlayers()
                    .filter { it.location.distanceSquared(entity.location) < dtRangeSq }
                    .minByOrNull { it.location.distanceSquared(entity.location) } ?: return 0f,
                0
            )
        }

        // if the entity has a target, return 10, otherwise, return 0 so that movement tasks can take priority
        return if (curTargets[entity] != null) {
            10f
        } else 0f
    }

    override fun startForEntity(handler: CustomMob, entity: Mob) {}

    override fun updateForEntity(handler: CustomMob, entity: Mob) {
        val targetPair = curTargets[entity] ?: return
        val target = targetPair.first
        val timeSinceLast = targetPair.second

        // move to the target
        entity.pathfinder.moveTo(target)

        // if time since last attack is greater than time between attacks, and we are in attack range, attack
        if (timeSinceLast > attackCooldownTicks && target.location.distanceSquared(entity.location) < atRangeSq) {
            if (onAttack != null)
                onAttack!!(entity, target) // Intellij is fucking annoying sometimes
            else
                entity.attack(target)
        }
        // otherwise, increment the time since last counter
        else {
            curTargets[entity] = Pair(target, timeSinceLast + 1)
        }
    }

    override fun stopForEntity(handler: CustomMob, entity: Mob) {
        entity.pathfinder.moveTo(entity.location)
    }
}