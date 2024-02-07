package dev.mateux

import dev.mateux.model.*
import io.agroal.api.AgroalDataSource
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.sql.Timestamp

@Path("/clientes")
class ClientsResource(
    @Inject
    private val service: ClientService
) {

    @POST
    @Path("/{id}/transacoes")
    @Produces(MediaType.APPLICATION_JSON)
    fun transaction(body: TransactionPost, id: String): TransactionResponse {
        if (body.tipo == null || body.valor == null || body.descricao == null) {
            throw WebApplicationException(400)
        }


        return service.addTransaction(id, body.valor, body.tipo, body.descricao)
    }

    @GET
    @Path("/{id}/extrato")
    @Produces(MediaType.APPLICATION_JSON)
    fun statement(id: String): StatementResponse {
        return service.getStatement(id)
    }
}
