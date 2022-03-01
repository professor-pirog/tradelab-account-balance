package com.pirogsoft.tradelabaccountbalance.service.balance

import com.pirogsoft.tradelabaccountbalance.model.Action
import com.pirogsoft.tradelabaccountbalance.model.UserBalance
import java.math.BigDecimal

interface BalanceService {

    suspend fun startReplenishProcess(userId: String): String

    suspend fun startWithdrawProcess(userId: String): String

    suspend fun startCommissionProcess(): String
}