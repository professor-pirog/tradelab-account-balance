package com.pirogsoft.tradelabaccountbalance.service.balance.current

import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class MockCurrentBalanceService : CurrentBalanceService {

    override suspend fun getCurrentBalance(): BigDecimal =
        BigDecimal("1000")

}