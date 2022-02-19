package com.pirogsoft.tradelabaccountbalance.utils

import io.r2dbc.spi.Row
import org.springframework.r2dbc.core.DatabaseClient

inline fun <reified T> Row.getMandatory(str: String): T = get(str, T::class.java)!!

inline fun <reified T> DatabaseClient.GenericExecuteSpec.bindNullable(name: String, value: T?): DatabaseClient.GenericExecuteSpec {
    return if (value == null) bindNull(name, T::class.java) else bind(name, value)
}