package dev.mateux

import dev.mateux.model.*
import io.agroal.api.AgroalDataSource
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.WebApplicationException
import java.sql.Timestamp

@ApplicationScoped
class ClientService(
    @Inject
    private val dataSource: AgroalDataSource
) {
    private val keyLock = KeyLock<String>()

    fun addTransaction(id: String, value: Int, type: String, description: String): TransactionResponse {
        return keyLock.withLock(id) {
            var (balance, limit) = dataSource.connection.use { connection ->
                connection.prepareStatement("SELECT saldo, limite FROM clientes WHERE id = ?")
                    .use {
                        it.setInt(1, id.toInt())
                        it.executeQuery().use { resultSet ->
                            if (resultSet.next()) {
                                resultSet.getInt("saldo") to resultSet.getInt("limite")
                            } else {
                                throw WebApplicationException(404)
                            }
                        }
                    }
            }

            when (type) {
                TransactionTypes.DEBIT.value -> {
                    balance -= value

                    if (balance < -limit) {
                        throw WebApplicationException(422)
                    }

                    dataSource.connection.use { connection ->
                        connection.prepareStatement("UPDATE clientes SET saldo = saldo - ? WHERE id = ?")
                            .use {
                                it.setInt(1, value)
                                it.setInt(2, id.toInt())
                                it.executeUpdate()
                            }
                    }
                }
                TransactionTypes.CREDIT.value -> {
                    dataSource.connection.use { connection ->
                        connection.prepareStatement("UPDATE clientes SET saldo = saldo + ? WHERE id = ?")
                            .use {
                                it.setInt(1, value)
                                it.setInt(2, id.toInt())
                                it.executeUpdate()
                            }
                    }

                    balance += value
                }
                else -> {
                    throw WebApplicationException(400)
                }
            }

            dataSource.connection.use { connection ->
                connection.prepareStatement("INSERT INTO transacoes (cliente_id, valor, tipo, descricao) VALUES (?, ?, CAST(? AS tipo_transacao), ?)")
                    .use { statement ->
                        statement.setInt(1, id.toInt())
                        statement.setInt(2, value)
                        statement.setString(3, type)
                        statement.setString(4, description)
                        statement.executeUpdate()
                    }
            }

            return@withLock TransactionResponse(balance, limit)
        }
    }

    fun getStatement(id: String): StatementResponse {
        return keyLock.withLock(id) {
            val (balance, limit) = dataSource.connection.use { connection ->
                connection.prepareStatement("SELECT saldo, limite FROM clientes WHERE id = ?")
                    .use {
                        it.setInt(1, id.toInt())
                        it.executeQuery().use { resultSet ->
                            if (resultSet.next()) {
                                resultSet.getInt("saldo") to resultSet.getInt("limite")
                            } else {
                                throw WebApplicationException(404)
                            }
                        }
                    }
            }

            val transactions = dataSource.connection.use { connection ->
                connection.prepareStatement("SELECT descricao, tipo, valor, realizada_em FROM transacoes WHERE cliente_id = ?")
                    .use {
                        it.setInt(1, id.toInt())
                        it.executeQuery().use { resultSet ->
                            generateSequence {
                                if (resultSet.next()) {
                                    Transaction(
                                        resultSet.getString("descricao"),
                                        resultSet.getString("tipo"),
                                        resultSet.getInt("valor"),
                                        resultSet.getTimestamp("realizada_em").toInstant().toString()
                                    )
                                } else {
                                    null
                                }
                            }.toList()
                        }
                    }
            }

            return@withLock StatementResponse(
                Balance(
                    balance,
                    Timestamp(System.currentTimeMillis()).toInstant().toString(),
                    limit
                ), transactions
            )
        }
    }
}