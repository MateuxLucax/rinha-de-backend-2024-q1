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
    @Inject
    private var dataSource: AgroalDataSource
) {
    private val keyLock = KeyLock<String>()
    private val cache = mutableMapOf<Int, Pair<Int, Int>>()

    fun addTransaction(id: String, value: Int, type: String, description: String): TransactionResponse {
        return keyLock.withLock(id) {
            dataSource.connection.use { connection ->
                connection.autoCommit = false

                try {
                    var (balance, limit) = getBalanceAndLimit(id.toInt(), connection)

                    when (type) {
                        "d" -> {
                            balance -= value

                            if (balance < -limit) {
                                throw WebApplicationException(422)
                            }

                            connection.prepareStatement("UPDATE clientes SET saldo = saldo - ? WHERE id = ?")
                                .use {
                                    it.setInt(1, value)
                                    it.setInt(2, id.toInt())
                                    it.executeUpdate()
                                }
                        }
                        "c" -> {
                            connection.prepareStatement("UPDATE clientes SET saldo = saldo + ? WHERE id = ?")
                                .use {
                                    it.setInt(1, value)
                                    it.setInt(2, id.toInt())
                                    it.executeUpdate()
                                }

                            balance += value
                        }
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
                    cache[id.toInt()] = balance to limit
                    return@withLock TransactionResponse(balance, limit)
                } catch (e: Exception) {
                    connection.rollback()
                    throw e
                } finally {
                    connection.autoCommit = true
                }
            }
        }
    }

    fun getStatement(id: String): StatementResponse {
        return keyLock.withLock(id) {
            dataSource.connection.use { connection ->
                val (balance, limit) = getBalanceAndLimit(id.toInt(), connection)

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

    private fun getBalanceAndLimit(id: Int, connection: Connection): Pair<Int, Int>{
        return cache.getOrElse(id) {
            connection.prepareStatement("SELECT saldo, limite FROM clientes WHERE id = ? FOR UPDATE")
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
    }
}