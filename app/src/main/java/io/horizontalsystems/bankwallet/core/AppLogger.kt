package io.horizontalsystems.bankwallet.core

import io.horizontalsystems.core.security.KeyStoreManager
import java.util.*

class AppLogger(private val scope: List<String> = listOf()) : KeyStoreManager.Logger {

    constructor(group: String) : this(listOf(group))

    private val actionId: String
        get() = scope.joinToString(":")

    fun getScopedUnique() : AppLogger {
        return getScoped(UUID.randomUUID().toString())
    }

    fun getScoped(scope: String) : AppLogger {
        return AppLogger(this.scope + scope)
    }

    override fun info(message: String) {
        io.horizontalsystems.bankwallet.core.AppLog.info(actionId, message)
    }

    override fun warning(message: String, e: Throwable) {
        io.horizontalsystems.bankwallet.core.AppLog.warning(actionId, message, e)
    }
}