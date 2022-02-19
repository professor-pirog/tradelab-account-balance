package com.pirogsoft.tradelabaccountbalance.service.balance

import com.pirogsoft.tradelabaccountbalance.model.Action
import com.pirogsoft.tradelabaccountbalance.model.ActionType
import com.pirogsoft.tradelabaccountbalance.model.UserBalance
import com.pirogsoft.tradelabaccountbalance.repository.ActionRepository
import com.pirogsoft.tradelabaccountbalance.repository.UserBalanceRepository
import com.pirogsoft.tradelabaccountbalance.service.balance.current.CurrentBalanceService
import com.pirogsoft.tradelabaccountbalance.service.operation.OperationService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.concurrent.CompletableFuture

@Service
class BalanceServiceImpl(
    private val userBalanceRepository: UserBalanceRepository,
    private val currentBalanceService: CurrentBalanceService,
    private val operationService: OperationService,
    private val actionRepository: ActionRepository
) : BalanceService {

    override suspend fun startReplenishProcess(userId: String): String {
        return startReplenishOrWithdrawProcess(userId, ActionType.REPLENISH)
    }

    override suspend fun startWithdrawProcess(userId: String): String {
        return startReplenishOrWithdrawProcess(userId, ActionType.WITHDRAW)
    }

    private suspend fun startReplenishOrWithdrawProcess(userId: String, actionType: ActionType): String {
        val operationIdFuture = CompletableFuture<String>()
        coroutineScope {
            launch(Dispatchers.IO) {
                //Запускаем процесс пополнения/снятия
                replenishOrWithdraw(userId, operationIdFuture, actionType)
            }
        }
        return operationIdFuture.await();
    }

    @Transactional
    suspend fun replenishOrWithdraw(userId: String, operationIdFuture: CompletableFuture<String>, actionType: ActionType) {
        //TODO: Блокируем табличку actions на создание новых записей
        //Считываем текущий баланс пользователей (по текущему балансу на бирже и записям из БД)
        val userBalances = getCurrentUserBalances()
        //Регистрируем операцию (для того чтобы в будущем можно было ее асинхронно завершить)
        val operationId = operationService.registerOperation()
        operationIdFuture.complete(operationId)
        //Ждем пока пользователь переведет деньги
        operationService.awaitOperation(operationId)
        //Считываем из бинанса новый баланс и пересчитываем балансы
        val (newAction, newUserBalances) = recalculateBalances(userBalances, actionType, userId)
        //Сохраняем в БД
        saveActionAndUserBalances(newAction, newUserBalances)
    }

    private suspend fun recalculateBalances(userBalances: Map<String, BigDecimal>, actionType: ActionType, userId: String): Pair<Action, List<UserBalance>> {
        // Получаем счет на бирже
        val newAccountBalance = currentBalanceService.getCurrentBalance()
        // Считаем сколько денег мы перекинули
        val changeAmountUSDT = userBalances.values.fold(newAccountBalance) { acc, userBalanceUSDT -> acc - userBalanceUSDT }
        // Создаем новое событие
        val newAction = Action(
            actionType = actionType,
            userId = userId,
            amountUSDT = changeAmountUSDT.run { if (actionType == ActionType.WITHDRAW) negate() else this }
        )
        // Пересчитываем балансы пользователей
        val newUserBalances = userBalances.map {
            val newUserUSDTBalance = if (it.key == userId) it.value + changeAmountUSDT else it.value
            UserBalance(
                actionId = newAction.id,
                userId = it.key,
                balanceUSDT = newUserUSDTBalance,
                balanceProportion = newAccountBalance / newUserUSDTBalance
            )
        }
        return newAction to newUserBalances
    }

    private suspend fun saveActionAndUserBalances(action: Action, balances: List<UserBalance>) {
        balances.forEach { userBalanceRepository.saveUserBalance(it) }
        actionRepository.saveAction(action)
    }

    private suspend fun getCurrentBalanceAndLastActionBalances(): Pair<BigDecimal, List<UserBalance>> = coroutineScope {
        val currentBalanceFuture = async {
            currentBalanceService.getCurrentBalance()
        }
        val userBalancesFuture = async {
            userBalanceRepository.getUserBalancesByLastAction()
        }
        return@coroutineScope currentBalanceFuture.await() to userBalancesFuture.await()
    }

    private suspend fun getCurrentUserBalances(): Map<String, BigDecimal> {
        val (generalBalance, lastActionUserBalances) = getCurrentBalanceAndLastActionBalances()
        return lastActionUserBalances.associate { it.userId to generalBalance * it.balanceProportion }
    }


}