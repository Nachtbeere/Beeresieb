package net.nachtbeere.minecraft.beeresieb

import khttp.get
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.json.JSONObject
import java.util.*

class BeeresiebLogic(
    instance: Beeresieb,
    val basePath: String,
    val authPath: String,
    val timeout: Double,
    val canFailsafe: Boolean,
    val failsafeUrl: String,
    val failsafeAuthPath: String,
    val canDeadmanSwitch: Boolean,
    val messages: BeeresiebMessages
    ) {
    private var minecraftServer: Server = bukkitServer()
    private val currentPlugin = instance

    companion object {
        fun timeout(time: Double): Double {
            return time * 0.001
        }
    }

    private fun bukkitServer(): Server = Bukkit.getServer()

    private fun convertUUID(uuid: UUID): String {
        return uuid.toString().replace("-", "")
    }

    private fun isUserExist(payload: JSONObject, uuid: UUID): Boolean {
        return payload.getString("uuid") == convertUUID(uuid)
    }

    private fun isVerifiedUser(payload: JSONObject): Boolean {
        return payload.getBoolean("verified")
    }

    private fun dummyResponse(): JSONObject {
        val result = JSONObject()
        result.put("uuid", "00000000000000000000000000000000")
        result.put("verified", false)
        return result
    }

    private fun request(requestUrl: String, uuid: String): JSONObject? {
        return if (requestUrl == "") {
            currentPlugin.logger.severe("Can't find request url! Beeresieb couldn't handle event. check config.yml please.")
            null
        } else {
            try {
                val resp = get(basePath + authPath + uuid, timeout = timeout)
                return if (resp.statusCode in 200..299) {
                    resp.jsonObject
                } else {
                    if (resp.statusCode == 404) {
                        try {
                            resp.jsonObject
                        } catch (e: org.json.JSONException) {
                            dummyResponse()
                        }
                    }
                    null
                }
            } catch (e: java.net.MalformedURLException) {
                null
            }
        }
    }

    fun verify(event: AsyncPlayerPreLoginEvent) {
        val future = futureTask {
            val result = request(basePath + authPath, convertUUID(event.uniqueId))
            if (result != null) {
                resultChecker(result, event)
            } else {
                onFailsafe(event)
            }
        }
        if (future == null) {
            deadmanSwitch(event)
        }
    }

    private fun onFailsafe(event: AsyncPlayerPreLoginEvent) {
        if (canFailsafe) {
            val result = request(failsafeUrl + failsafeAuthPath, convertUUID(event.uniqueId))
            if (result != null) {
                resultChecker(result, event)
            } else {
                deadmanSwitch(event)
            }
        } else {
            deadmanSwitch(event)
        }
    }

    private fun resultChecker(result: JSONObject, event: AsyncPlayerPreLoginEvent) {
        if (isUserExist(result, event.uniqueId)) {
            if (!isVerifiedUser(result)) {
                denyUser(event)
            }
        } else {
            kickUser(event)
        }
    }

    private fun deadmanSwitch(event: AsyncPlayerPreLoginEvent) {
        if (!canDeadmanSwitch) {
            kickUser(event)
        }
    }

    private fun kickUser(event: AsyncPlayerPreLoginEvent) {
        this.currentPlugin.logger.info(
            messages.kickLog.replace(
                "\${USERNAME}",
                event.name ?: event.uniqueId.toString())
        )
        event.disallow(
            AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST,
            messages.kickMessage
        )
    }

    private fun denyUser(event: AsyncPlayerPreLoginEvent) {
        this.currentPlugin.logger.info(
            messages.denialLog.replace(
                "\${USERNAME}",
                event.name ?: event.uniqueId.toString())
        )
        event.disallow(
            AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST,
            messages.denialMessage
        )
    }

    private fun futureTask(task: () -> Any): Any? {
        val future = this.minecraftServer.scheduler.callSyncMethod(this.currentPlugin) { task() }
        return try {
            future.get()
        } catch (e: Throwable) {
            this.currentPlugin.logger.severe(e.toString())
            null
        }
    }
}

class BeeresiebMessages(
    val kickLog: String,
    val denialLog: String,
    val kickMessage: String,
    val denialMessage: String
)
