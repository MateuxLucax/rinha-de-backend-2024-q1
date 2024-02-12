package dev.mateux

import dev.mateux.model.Balance
import dev.mateux.model.StatementResponse
import dev.mateux.model.Transaction
import dev.mateux.model.TransactionResponse
import io.agroal.api.AgroalDataSource
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.WebApplicationException
import java.sql.Timestamp

@ApplicationScoped
class ClientService(
    @Inject private var dataSource: AgroalDataSource
) {
    fun addTransaction(id: Int, value: Int, type: String, description: String): TransactionResponse {
        dataSource.connection.use { connection ->
            try {
                connection.autoCommit = false

                val (balance, limit) = connection.prepareStatement("SELECT saldo, limite FROM clientes WHERE id = ? FOR UPDATE")
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

                if (type == "d" && balance - value < -limit) {
                    throw WebApplicationException(422)
                }

                val newBalance = when (type) {
                    "c" -> balance + value
                    "d" -> balance - value
                    else -> throw WebApplicationException(422)
                }

                connection.prepareStatement("UPDATE clientes SET saldo = ? WHERE id = ?")
                    .use { statement ->
                        statement.setInt(1, newBalance)
                        statement.setInt(2, id)
                        statement.executeUpdate()
                    }

                connection.prepareStatement("INSERT INTO transacoes (cliente_id, valor, tipo, descricao) VALUES (?, ?, ?, ?)")
                    .use { statement ->
                        statement.setInt(1, id)
                        statement.setInt(2, value)
                        statement.setString(3, type)
                        statement.setString(4, description)
                        statement.executeUpdate()
                    }

                connection.commit()
                return TransactionResponse(newBalance, limit)
            } catch (e: Exception) {
                connection.rollback()
                throw WebApplicationException(422)
            }
        }
    }

    fun getStatement(id: Int): StatementResponse {
        dataSource.connection.use { connection ->
            val (balance, limit) = connection.prepareStatement("SELECT saldo, limite FROM clientes WHERE id = ?")
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

            val transactions = connection.prepareStatement("SELECT descricao, tipo, valor, realizada_em FROM transacoes WHERE cliente_id = ? ORDER BY id DESC LIMIT 10")
                .use {
                    it.setInt(1, id)
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
}