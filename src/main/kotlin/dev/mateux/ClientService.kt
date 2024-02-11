package dev.mateux

import dev.mateux.model.Balance
import dev.mateux.model.StatementResponse
import dev.mateux.model.Transaction
import dev.mateux.model.TransactionResponse
import io.agroal.api.AgroalDataSource
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.WebApplicationException
import java.sql.Connection
import java.sql.Timestamp

@ApplicationScoped
class ClientService(
    @Inject private var dataSource: AgroalDataSource
) {
    fun addTransaction(id: String, value: Int, type: String, description: String): TransactionResponse {
        dataSource.connection.use { connection ->
            connection.autoCommit = false

            try {
                var (balance, limit) = connection.prepareStatement("SELECT saldo, limite FROM clientes WHERE id = ? FOR UPDATE")
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

                if (type == "d") {
                    if (balance - value < -limit) {
                        throw WebApplicationException(400)
                    }
                }

                balance += if (type == "d") -value else value

                connection.prepareStatement("UPDATE clientes SET saldo = ? WHERE id = ?")
                    .use {
                        it.setInt(1, value)
                            it.setInt(2, id.toInt())
                        it.executeUpdate()
                    }

                connection.prepareStatement("INSERT INTO transacoes (cliente_id, valor, tipo, descricao) VALUES (?, ?, ?, ?)")
                    .use { statement ->
                        statement.setInt(1, id.toInt())
                        statement.setInt(2, value)
                        statement.setString(3, type)
                        statement.setString(4, description)
                        statement.executeUpdate()
                    }

                connection.commit()
                return TransactionResponse(balance, limit)
            } catch (e: Exception) {
                connection.rollback()
                throw e
            } finally {
                connection.autoCommit = true
            }
        }
    }

    fun getStatement(id: String): StatementResponse {
        dataSource.connection.use { connection ->
            val (balance, limit) = connection.prepareStatement("SELECT saldo, limite FROM clientes WHERE id = ?")
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

            val transactions = connection.prepareStatement("SELECT descricao, tipo, valor, realizada_em FROM transacoes WHERE cliente_id = ? ORDER BY id DESC LIMIT 10")
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

            return StatementResponse(
                Balance(
                    balance,
                    Timestamp(System.currentTimeMillis()).toInstant().toString(),
                    limit
                ), transactions
            )
        }
    }

    private fun getBalanceAndLimit(id: Int, connection: Connection, forUpdate: Boolean = false): Pair<Int, Int> {
        return connection.prepareStatement("SELECT saldo, limite FROM clientes WHERE id = ?" + if (forUpdate) " FOR UPDATE" else "")
            .use {
                it.setInt(1, id)
                it.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        resultSet.getInt("saldo") to resultSet.getInt("limite")
                    } else {
                        throw WebApplicationException(404)
                    }
                }
            }
    }
}