package com.pirogsoft.tradelabaccountbalance.service.balance.current

import java.math.BigDecimal

interface CurrentBalanceService {

    suspend fun getCurrentBalance() : BigDecimal
}