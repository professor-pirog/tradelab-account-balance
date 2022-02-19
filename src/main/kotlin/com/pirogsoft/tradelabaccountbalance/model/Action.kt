package com.pirogsoft.tradelabaccountbalance.model

import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class Action(
    val id: String = UUID.randomUUID().toString(),
    val timeStamp: Instant = Instant.now(),
    val actionType: ActionType,
    val userId: String?,
    val amountUSDT: BigDecimal?
)