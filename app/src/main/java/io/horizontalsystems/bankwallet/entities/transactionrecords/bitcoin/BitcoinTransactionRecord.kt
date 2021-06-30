package io.horizontalsystems.bankwallet.entities.transactionrecords.bitcoin

import io.horizontalsystems.bankwallet.entities.LastBlockInfo
import io.horizontalsystems.bankwallet.entities.transactionrecords.TransactionRecord
import io.horizontalsystems.bankwallet.modules.transactions.TransactionLockInfo
import io.horizontalsystems.coinkit.models.Coin
import java.math.BigDecimal
import java.util.*

abstract class BitcoinTransactionRecord(
        val coin: Coin,
        uid: String,
        transactionHash: String,
        transactionIndex: Int,
        blockHeight: Int?,
        confirmationsThreshold: Int?,
        date: Date,
        fee: BigDecimal?,
        failed: Boolean,
        val lockInfo: TransactionLockInfo?,
        val conflictingHash: String?,
        val showRawTransaction: Boolean
) : TransactionRecord(
        uid = uid,
        transactionHash = transactionHash,
        transactionIndex = transactionIndex,
        blockHeight = blockHeight,
        confirmationsThreshold = confirmationsThreshold,
        timestamp = date.time,
        fee = fee,
        failed = failed
) {

    override val mainCoin: Coin?
        get() = coin

    override fun changedBy(oldBlockInfo: LastBlockInfo?, newBlockInfo: LastBlockInfo?): Boolean {
        return super.changedBy(oldBlockInfo, newBlockInfo) || becomesUnlocked(oldBlockInfo?.timestamp, newBlockInfo?.timestamp)
    }

    fun lockState(lastBlockTimestamp: Long?) : TransactionLockState? {
        val lockInfo = lockInfo ?: return null

        var locked = true

        lastBlockTimestamp?.let {
            locked = it < lockInfo.lockedUntil.time
        }

        return TransactionLockState(locked, lockInfo.lockedUntil)
    }

    private fun becomesUnlocked(oldTimestamp: Long?, newTimestamp: Long?) : Boolean {
        //todo check this division by 1000
        val lockTime = lockInfo?.lockedUntil?.time?.div(1000) ?: return false
        newTimestamp ?: return false

        return lockTime > (oldTimestamp ?: 0L) && // was locked
        lockTime <= newTimestamp       // now unlocked
    }
}

data class TransactionLockState(val locked: Boolean, val date: Date)