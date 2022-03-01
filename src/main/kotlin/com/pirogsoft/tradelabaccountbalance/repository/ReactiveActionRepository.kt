package com.pirogsoft.tradelabaccountbalance.repository

import com.pirogsoft.tradelabaccountbalance.model.Action
import com.pirogsoft.tradelabaccountbalance.utils.bindNullable
import kotlinx.coroutines.reactor.awaitSingle
import org.intellij.lang.annotations.Language
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
class ReactiveActionRepository(private val databaseClient: DatabaseClient) : ActionRepository {

    override suspend fun saveAction(action: Action) {
        databaseClient.sql(INSERT_USER_BALANCE)
            .bind(ColumnNames.ID, action.id)
            .bind(ColumnNames.ACTION_TYPE, action.actionType)
            .bind(ColumnNames.TIMESTAMP, action.timeStamp)
            .bind(ColumnNames.AMOUNT_USDT, action.amountUSDT)
            .bindNullable(ColumnNames.USER_ID, action.userId)
            .then()
            .awaitSingle()
    }

    companion object {

        object ColumnNames {

            const val ID = "id"

            const val ACTION_TYPE = "action_type"

            const val TIMESTAMP = "timestamp"

            const val USER_ID = "user_id"

            const val AMOUNT_USDT = "amount_usdt"
        }

        @Language("SQL")
        const val INSERT_USER_BALANCE = """
                INSERT INTO action (id, action_type, timestamp, user_id, amount_usdt) 
                VALUES (:id, :action_type, :timestamp, :user_id, :amount_usdt) 
        """
    }

}