package net.nachtbeere.minecraft.beeresieb

import java.io.File
import org.bukkit.plugin.java.JavaPlugin

class Beeresieb : JavaPlugin() {
    private var beeresiebListener: BeeresiebListener? = null

    override fun onEnable() {
        if (!(File(this.dataFolder, "config.yml")).exists()) this.saveDefaultConfig()
        beeresiebListener = BeeresiebListener(
            pluginInstance = this
        )
    }

    override fun onDisable() {
        this.beeresiebListener = null
    }
}
