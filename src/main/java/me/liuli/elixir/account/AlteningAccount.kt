package me.liuli.elixir.account

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mojang.authlib.Agent
import com.mojang.authlib.exceptions.AuthenticationException
import com.mojang.authlib.exceptions.AuthenticationUnavailableException
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication
import com.thealtening.api.TheAltening
import com.thealtening.api.TheAlteningException
import com.thealtening.api.response.AccountDetails
import com.thealtening.auth.TheAlteningAuthentication
import me.liuli.elixir.compat.Session
import me.liuli.elixir.exception.LoginException
import me.liuli.elixir.utils.set
import me.liuli.elixir.utils.string
import java.net.Proxy

class AlteningAccount : MinecraftAccount("Altening") {

    override var name = ""
    var token = ""

    private var uuid = ""
    private var accessToken = ""

    var accountDetails: AccountDetails? = null

    override val session: Session
        get() {
            if(name.isEmpty() || uuid.isEmpty() || accessToken.isEmpty()) {
                update()
            }

            return Session(name, uuid, accessToken, "mojang")
        }

    override fun update() {
        // Go to altening authentication service
        TheAlteningAuthentication.theAltening()

        val userAuthentication = YggdrasilAuthenticationService(Proxy.NO_PROXY, "").createUserAuthentication(Agent.MINECRAFT) as YggdrasilUserAuthentication

        userAuthentication.setUsername(token)
        userAuthentication.setPassword("LiquidBounce")

        try {
            userAuthentication.logIn()
            name = userAuthentication.selectedProfile.name
            uuid = userAuthentication.selectedProfile.id.toString()
            accessToken = userAuthentication.authenticatedToken
        } catch (exception: AuthenticationUnavailableException) {
            throw LoginException("Mojang server is unavailable")
        } catch (exception: AuthenticationException) {
            throw LoginException(exception.message ?: "Unknown error")
        }

        // Go back to mojang authentication service
        TheAlteningAuthentication.mojang()
    }

    override fun toRawJson(json: JsonObject) {
        json["name"] = name
        json["token"] = token
        json["details"] = Gson().toJson(accountDetails ?: return)
    }

    override fun fromRawJson(json: JsonObject) {
        name = json.string("name")!!
        token = json.string("token")!!
        accountDetails = Gson().fromJson(json.get("details") ?: return, AccountDetails::class.java)
    }

    companion object {

        @Throws(TheAlteningException::class)
        fun generateAccount(apiKey: String): AlteningAccount {
            val retriever = TheAltening.newBasicRetriever(apiKey)
            val newAccount = retriever.account
            val alteningAccount = AlteningAccount()

            alteningAccount.name = newAccount.username
            alteningAccount.token = newAccount.token
            alteningAccount.accountDetails = newAccount.info

            return alteningAccount
        }

    }

}