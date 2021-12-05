package me.liuli.elixir.manage

import com.beust.klaxon.JsonObject
import me.liuli.elixir.account.CrackedAccount
import me.liuli.elixir.account.MicrosoftAccount
import me.liuli.elixir.account.MinecraftAccount
import me.liuli.elixir.account.MojangAccount

object AccountSerializer {
    /**
     * write [account] to [JsonObject]
     */
    fun toJson(account: MinecraftAccount): JsonObject {
        val json = JsonObject()
        account.toRawJson(json)
        json["type"] = account.javaClass.canonicalName
        return json
    }

    /**
     * read [MinecraftAccount] from [json]
     */
    fun fromJson(json: JsonObject): MinecraftAccount {
        val account = Class.forName(json["type"].toString()).newInstance() as MinecraftAccount
        account.fromRawJson(json)
        return account
    }

    /**
     * get an instance of [MinecraftAccount] from [name] and [password]
     */
    fun accountInstance(name: String, password: String): MinecraftAccount {
        return if (name.startsWith("ms@")) {
            val realName = name.substring(3)
            if(password.isEmpty()) {
                MicrosoftAccount.buildFromAuthCode(realName)
            } else {
                MicrosoftAccount.buildFromPassword(realName, password)
            }
        } else if(password.isEmpty()) {
            CrackedAccount().also { it.name = name }
        } else {
            MojangAccount().also { it.name = name; it.password = password }
        }
    }
}