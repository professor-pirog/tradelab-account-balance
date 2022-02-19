package com.pirogsoft.tradelabaccountbalance.repository

import com.pirogsoft.tradelabaccountbalance.model.UserBalance

interface UserBalanceRepository {

    suspend fun getUserBalancesByLastAction() : List<UserBalance>

    suspend fun saveUserBalance(userBalance: UserBalance)
}