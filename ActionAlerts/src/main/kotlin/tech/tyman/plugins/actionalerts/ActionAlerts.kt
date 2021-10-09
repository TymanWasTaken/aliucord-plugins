package tech.tyman.plugins.actionalerts

import android.content.Context
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.NotificationsAPI
import com.aliucord.entities.NotificationData
import com.aliucord.entities.Plugin
import com.aliucord.utils.RxUtils.await
import com.aliucord.utils.RxUtils.subscribe
import com.discord.models.user.CoreUser
import com.discord.stores.StoreStream
import com.discord.utilities.rest.RestAPI

@AliucordPlugin
class ActionAlerts : Plugin() {
    private var relationshipCache = mutableMapOf<Long, Int>()

    override fun start(context: Context) {
        StoreStream.getUserRelationships().observe().subscribe {
            if (relationshipCache.size > this.size) {
                val removed = relationshipCache.keys.find {
                    !this.containsKey(it)
                } ?: return@subscribe
                val user = StoreStream.getUsers().users[removed] ?: CoreUser(
                    RestAPI.getApi().userGet(removed).await().first ?: return@subscribe
                )
                NotificationsAPI.display(
                    NotificationData().apply {
                        title = "ActionAlerts"
                        body = "${user.username}#${String.format("%04d", user.discriminator)} removed you as a friend."
                        autoDismissPeriodSecs = 5
                        onClick = {}
                    }
                )
            }
            relationshipCache = this
        }
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}