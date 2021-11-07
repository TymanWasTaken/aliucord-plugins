package tech.tyman.plugins.actionalerts

import android.content.Context
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.NotificationsAPI
import com.aliucord.entities.NotificationData
import com.aliucord.entities.Plugin
import com.aliucord.utils.RxUtils.await
import com.aliucord.utils.RxUtils.subscribe
import com.discord.gateway.GatewaySocket
import com.discord.models.domain.ModelUserRelationship
import com.discord.models.user.CoreUser
import com.discord.restapi.RestAPIParams
import com.discord.stores.StoreBans
import com.discord.stores.StoreStream
import com.discord.stores.StoreUserRelationships
import com.discord.utilities.rest.RestAPI
import rx.Subscription

@AliucordPlugin
class ActionAlerts : Plugin() {
    private var relationshipCache = mutableMapOf<Long, Int>()
    private lateinit var relationshipListener: Subscription

    override fun start(context: Context) {
        relationshipListener = StoreStream.getUserRelationships().observe().subscribe {
            if (relationshipCache.size > this.size) {
                val removed = relationshipCache.entries.find {
                    !this.keys.contains(it.key)
                } ?: return@subscribe
                val user = StoreStream.getUsers().users[removed.key] ?: CoreUser(
                    RestAPI.getApi().userGet(removed.key).await().first ?: return@subscribe
                )
                if (removed.value != ModelUserRelationship.TYPE_FRIEND) {
                    NotificationsAPI.display(
                        NotificationData().apply {
                            title = "ActionAlerts"
                            body = "${user.username}#${String.format("%04d", user.discriminator)} removed you as a friend."
                            autoDismissPeriodSecs = 5
                            onClick = {}
                        }
                    )
                }
            }
            relationshipCache = this
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
        relationshipListener.unsubscribe()
    }
}