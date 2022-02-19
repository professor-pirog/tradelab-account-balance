package com.pirogsoft.tradelabaccountbalance.service.balance

import java.math.BigDecimal

interface BalanceService {

    suspend fun startReplenishProcess(userId: String): String

    suspend fun startWithdrawProcess(userId: String): String

}