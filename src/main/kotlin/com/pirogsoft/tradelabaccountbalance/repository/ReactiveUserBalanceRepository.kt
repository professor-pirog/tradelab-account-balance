package com.pirogsoft.tradelabaccountbalance.repository

import com.pirogsoft.tradelabaccountbalance.model.UserBalance
import com.pirogsoft.tradelabaccountbalance.utils.getMandatory
import io.r2dbc.spi.Row
import kotlinx.coroutines.reactor.awaitSingle
import org.intellij.lang.annotations.Language
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
class ReactiveUserBalanceRepository(private val databaseClient: DatabaseClient) : UserBalanceRepository {

    override suspend fun getUserBalancesByLastAction(): List<UserBalance> =
        databaseClient
            .sql(SELECT_USER_BALANCES_BY_LAST_ACTION)
            .map(this::mapRowToUserBalance)
            .all()
            .collectList()
            .awaitSingle()

    override suspend fun saveUserBalance(userBalance: UserBalance) {
        databaseClient
            .sql(INSERT_USER_BALANCE)
            .bind(ColumnNames.ACTION_ID, userBalance.actionId)
            .bind(ColumnNames.USER_ID, userBalance.userId)
            .bind(ColumnNames.BALANCE_USDT, userBalance.balanceUSDT)
            .bind(ColumnNames.BALANCE_PROPORTION, userBalance.balanceProportion)
    }

    private fun mapRowToUserBalance(row: Row): UserBalance =
        UserBalance(
            actionId = row.getMandatory(ColumnNames.ACTION_ID),
            userId = row.getMandatory(ColumnNames.USER_ID),
            balanceUSDT = row.getMandatory(ColumnNames.BALANCE_USDT),
            balanceProportion = row.getMandatory(ColumnNames.BALANCE_PROPORTION)
        )

    companion object {

        object ColumnNames {

            const val ACTION_ID = "action_id"

            const val USER_ID = "user_id"

            const val BALANCE_USDT = "balance_usdt"

            const val BALANCE_PROPORTION = "balance_proportion"
        }

        @Language("SQL")
        const val SELECT_USER_BALANCES_BY_LAST_ACTION = """
                SELECT * FROM user_balance WHERE action_id =
                        (SELECT id FROM action order by timestamp desc limit 1)
        """

        @Language("SQL")
        const val INSERT_USER_BALANCE = """
                INSERT INTO user_balance (action_id, user_id, balance_usdt, balance_proportion) 
                VALUES (:action_id, :user_id, :balance_usdt, :balance_proportion) 
        """
    }

}