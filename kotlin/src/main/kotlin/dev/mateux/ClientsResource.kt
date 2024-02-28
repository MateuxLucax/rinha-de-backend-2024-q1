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
        if (body.tipo == null || body.valor == null || body.descricao.isNullOrEmpty() || body.descricao.length > 10 || body.valor !is Int || body.valor < 1 || body.tipo !in listOf("c", "d")) {
            throw WebApplicationException(422)
        }

        if (id.toInt() !in 1..5) {
            throw WebApplicationException(404)
        }

        try {
            val (balance, limit) = datasource.connection.use { connection ->
                connection.createStatement().use { ptmt ->
                    ptmt.executeQuery("CALL adiciona_transacao($id::INT2, ${body.valor}::INT4, ${if (body.tipo == "c") body.valor else -body.valor}::INT4, '${body.tipo}'::CHAR(1), '${body.descricao}'::VARCHAR(10), NULL, NULL)").use { rs ->
                        rs.next()
                        rs.getInt(1) to rs.getInt(2)
                    }
                }
            }

            return TransactionResponse(
                saldo = balance,
                limite = limit,
            )
        } catch (ignored: Exception) {
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
            connection.createStatement().use { ptmt ->
                ptmt.executeQuery("SELECT saldo, limite FROM clientes WHERE id = $id").use { rs ->
                    rs.next()
                    rs.getInt(1) to rs.getInt(2)
                }
            }
        }

        val transactions = datasource.connection.use { connection ->
            connection.createStatement().use { ptmt ->
                ptmt.executeQuery("SELECT tipo, valor, descricao, realizada_em FROM transacoes WHERE cliente_id = $id ORDER BY id DESC LIMIT 10").use { rs ->
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