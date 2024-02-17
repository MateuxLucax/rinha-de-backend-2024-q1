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

        return datasource.connection.use { connection ->
            connection.prepareStatement("""
               SELECT c.saldo AS total,
                      c.limite,
                      t.valor,
                      t.tipo,
                      t.descricao,
                      t.realizada_em
                 FROM clientes c
            LEFT JOIN transacoes t
                   ON c.id = t.cliente_id
                WHERE c.id = ?
             GROUP BY c.id, c.saldo, c.limite, t.valor, t.tipo, t.descricao, t.realizada_em
             ORDER BY t.realizada_em DESC
                LIMIT 10
            """).use { ptmt ->
                ptmt.setInt(1, id.toInt())
                ptmt.executeQuery().use { rs ->
                    val balance = if (rs.next()) {
                        Balance(
                            description = rs.getInt("total"),
                            date = Timestamp(System.currentTimeMillis()).toString(),
                            value = rs.getInt("limite"),
                        )
                    } else {
                        null
                    }

                    val transactions = mutableListOf<StatementTransaction>()
                    while (rs.next()) {
                        if (rs.getString("tipo").isNullOrEmpty()) continue

                        transactions.add(
                            StatementTransaction(
                                description = rs.getString("descricao"),
                                type = rs.getString("tipo").first(),
                                value = rs.getInt("valor"),
                                performedIn = rs.getString("realizada_em"),
                            )
                        )
                    }

                    StatementResponse(
                        balance = balance,
                        statementTransactions = transactions,
                    )
                }
            }
        }
    }
}
