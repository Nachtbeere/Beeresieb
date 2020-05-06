package net.nachtbeere.minecraft.beeresieb

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class BeeresiebListener(pluginInstance: Beeresieb) : Listener {
    private val basePath = pluginInstance.config.getString("baseurl") ?: ""
    private var authPath = pluginInstance.config.getString("auth_path") ?: ""
    private val failsafePath = pluginInstance.config.getString("failsafe_url") ?: ""
    private var failsafeAuthPath = pluginInstance.config.getString("failsafe_auth_path") ?: ""
    private val timeout = BeeresiebLogic.timeout(pluginInstance.config.getDouble("timeout") ?: 0.0)
    private val canFailsafe = pluginInstance.config.getBoolean("allow_failsafe") ?: false
    private val canDeadmanSwitch = pluginInstance.config.getBoolean("deadman_switch_allow_all") ?: true
    private var kickLog = pluginInstance.config.getString("messages.kick_log") ?: ""
    private var denialLog = pluginInstance.config.getString("messages.denial_log") ?: ""
    private var kickMessage = pluginInstance.config.getString("messages.kick_message")
        ?.replace("\\n", System.getProperty("line.separator")) ?: ""
    private var denialMessage = pluginInstance.config.getString("messages.denial_message")
        ?.replace("\\n", System.getProperty("line.separator")) ?: ""

    private val logic = BeeresiebLogic(
        pluginInstance,
        basePath = basePath,
        authPath = authPath,
        timeout = timeout,
        canFailsafe = canFailsafe,
        failsafeUrl = failsafePath,
        failsafeAuthPath = failsafeAuthPath,
        canDeadmanSwitch = canDeadmanSwitch,
        messages = BeeresiebMessages(
            kickLog = kickLog,
            denialLog = denialLog,
            kickMessage = kickMessage,
            denialMessage = denialMessage
        )
    )

    init {
        pluginInstance.server.pluginManager.registerEvents(this, pluginInstance)
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        logic.verify(event)
    }
}