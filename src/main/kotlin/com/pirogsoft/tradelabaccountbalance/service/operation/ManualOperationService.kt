package com.pirogsoft.tradelabaccountbalance.service.operation

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.await
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Service
class ManualOperationService : OperationService {

    private val operations: ConcurrentMap<String, CompletionStage<Any>> = ConcurrentHashMap()

    override fun registerOperation(): String {
        return UUID.randomUUID().toString().also { operations[it] = CompletableFuture() }
    }

    override suspend fun awaitOperation(operationId: String) {
        val future: CompletionStage<Any> = operations[operationId]!!
        future.await()
        operations.remove(operationId)
    }

    override fun completeOperation(operationId: String) {
        operations[operationId]!!.toCompletableFuture().complete(null)
    }

}