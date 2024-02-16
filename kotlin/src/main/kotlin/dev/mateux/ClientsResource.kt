package dev.mateux

import dev.mateux.model.*
import io.agroal.api.AgroalDataSource
import io.smallrye.common.annotation.RunOnVirtualThread
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import java.sql.Timestamp

@Path("/clientes")
class ClientsResource(
    @Inject private var datasource: AgroalDataSource,
) {
    @POST
    @Path("/{id}/transacoes")
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    fun transaction(body: TransactionPost, id: String): TransactionResponse {
        if (body.tipo == null || body.valor == null || body.descricao.isNullOrEmpty() || body.descricao.length > 10 || body.valor !is Int || body.tipo !in listOf("c", "d")) {
            throw WebApplicationException(422)
        }

        if (id.toInt() !in 1..5) {
            throw WebApplicationException(404)
        }

        try {
            val (balance, limit) = datasource.connection.use { connection ->
                connection.prepareStatement("SELECT new_saldo, limite FROM update_saldo_cliente(?, ?, ?, ?)").use { ptmt ->
                    ptmt.setInt(1, id.toInt())
                    ptmt.setInt(2, body.valor)
                    ptmt.setString(3, body.tipo)
                    ptmt.setString(4, body.descricao)
                    ptmt.executeQuery().use { rs ->
                        rs.next()
                        rs.getInt(1) to rs.getInt(2)
                    }
                }
            }

            return TransactionResponse(
                saldo = balance,
                limite = limit,
            )
        } catch (e: Exception) {
            throw WebApplicationException(422)
        }
    }

    @GET
    @Path("/{id}/extrato")
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    fun statement(id: String): StatementResponse {
        if (id.toInt()  !in 1..5) {
            throw WebApplicationException(404)
        }

        val (balance, limit) = datasource.connection.use { connection ->
            connection.prepareStatement("SELECT saldo, limite FROM clientes WHERE id = ?").use { ptmt ->
                ptmt.setInt(1, id.toInt())
                ptmt.executeQuery().use { rs ->
                    rs.next()
                    rs.getInt(1) to rs.getInt(2)
                }
            }
        }

        val transactions = datasource.connection.use { connection ->
            connection.prepareStatement("SELECT tipo, valor, descricao, realizada_em FROM transacoes WHERE cliente_id = ? ORDER BY realizada_em DESC LIMIT 10").use { ptmt ->
                ptmt.setInt(1, id.toInt())
                ptmt.executeQuery().use { rs ->
                    val transactions = mutableListOf<StatementTransaction>()
                    while (rs.next()) {
                        transactions.add(
                            StatementTransaction(
                                type = rs.getString(1)[0],
                                value = rs.getInt(2),
                                description = rs.getString(3),
                                performedIn = rs.getTimestamp(4).toInstant().toString(),
                            )
                        )
                    }
                    transactions
                }
            }
        }

        return StatementResponse(
            balance = Balance(
                description = balance,
                date = Timestamp(System.currentTimeMillis()).toInstant().toString(),
                value = limit,
            ),
            statementTransactions = transactions,
        )
    }
}
