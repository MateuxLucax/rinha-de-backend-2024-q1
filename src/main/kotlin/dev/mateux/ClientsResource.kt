package dev.mateux

import dev.mateux.model.TransactionPost
import dev.mateux.model.TransactionResponse
import dev.mateux.model.TransactionTypes
import io.agroal.api.AgroalDataSource
import jakarta.inject.Inject
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response

@Path("/clientes")
class ClientsResource(
    @Inject
    val dataSource: AgroalDataSource
) {

    @POST
    @Path("/{id}/transacoes")
    @Produces(MediaType.APPLICATION_JSON)
    fun transaction(body: TransactionPost, id: String): TransactionResponse {
        if (body.tipo == null || body.valor == null || body.descricao == null) {
            throw WebApplicationException(400)
        }

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

        when (body.tipo) {
            TransactionTypes.DEBIT.value -> {
                balance -= body.valor

                if (balance < -limit) {
                    throw WebApplicationException(422)
                }

                dataSource.connection.use { connection ->
                    connection.prepareStatement("UPDATE clientes SET saldo = saldo - ? WHERE id = ?")
                        .use {
                            it.setInt(1, body.valor)
                            it.setInt(2, id.toInt())
                            it.executeUpdate()
                        }
                }
            }
            TransactionTypes.CREDIT.value -> {
                dataSource.connection.use { connection ->
                    connection.prepareStatement("UPDATE clientes SET saldo = saldo + ? WHERE id = ?")
                        .use {
                            it.setInt(1, body.valor)
                            it.setInt(2, id.toInt())
                            it.executeUpdate()
                        }
                }

                balance += body.valor
            }
            else -> {
                throw WebApplicationException(400)
            }
        }

        dataSource.connection.use { connection ->
            connection.prepareStatement("INSERT INTO transacoes (cliente_id, valor, tipo, descricao) VALUES (?, ?, CAST(? AS tipo_transacao), ?)")
                .use { statement ->
                    statement.setInt(1, id.toInt())
                    statement.setInt(2, body.valor)
                    statement.setString(3, body.tipo)
                    statement.setString(4, body.descricao)
                    statement.executeUpdate()
            }
        }

        return TransactionResponse(balance, limit)
    }
}