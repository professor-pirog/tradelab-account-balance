package com.pirogsoft.tradelabaccountbalance.service.operation

interface OperationService {

    fun registerOperation() : String

    suspend fun awaitOperation(operationId: String)

    fun completeOperation(operationId: String)

}