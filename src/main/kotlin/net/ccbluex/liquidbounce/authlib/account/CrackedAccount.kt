package net.ccbluex.liquidbounce.authlib.account

import com.google.gson.JsonObject
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import com.mojang.authlib.yggdrasil.YggdrasilEnvironment
import net.ccbluex.liquidbounce.authlib.compat.GameProfile
import net.ccbluex.liquidbounce.authlib.compat.Session
import net.ccbluex.liquidbounce.authlib.utils.MojangApi
import net.ccbluex.liquidbounce.authlib.utils.parseUuid
import net.ccbluex.liquidbounce.authlib.utils.set
import net.ccbluex.liquidbounce.authlib.utils.string
import java.net.Proxy
import java.util.*

/**
 * A minecraft cracked account - has no password and no access to premium online servers
 */
class CrackedAccount(private val username: String) : MinecraftAccount("Cracked") {

    /**
     * Used for JSON deserialize.
     */
    @Suppress("unused")
    constructor() : this("")

    override fun refresh() {
        val uuid = runCatching {
            MojangApi.getUuid(username)
        }.getOrNull() ?: UUID.randomUUID()

        profile = GameProfile(username, uuid)
    }

    override fun login(): Pair<Session, YggdrasilAuthenticationService> {
        if (profile == null) {
            refresh()
        }

        val session = profile!!.toSession("-", "legacy")
        val service = YggdrasilAuthenticationService(Proxy.NO_PROXY, YggdrasilEnvironment.PROD.environment)

        return session to service
    }

    override fun toRawJson(json: JsonObject) {
        json["name"] = profile!!.username
        json["uuid"] = profile!!.uuid.toString()
    }

    override fun fromRawJson(json: JsonObject) {
        val name = json.string("name")!!
        val uuid = runCatching {
            parseUuid(json.string("uuid")!!)
        }.getOrElse { MojangApi.getUuid(name) }
        profile = GameProfile(name, uuid!!)
    }
}
