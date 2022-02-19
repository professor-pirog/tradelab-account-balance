package com.pirogsoft.tradelabaccountbalance.model

import java.math.BigDecimal

data class UserBalance(
    val actionId: String,
    val userId: String,
    val balanceUSDT: BigDecimal,
    val balanceProportion: BigDecimal
)