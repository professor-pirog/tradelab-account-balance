package com.pirogsoft.tradelabaccountbalance.repository

import com.pirogsoft.tradelabaccountbalance.model.Action

interface ActionRepository {
    suspend fun saveAction(action: Action)
}