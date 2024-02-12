package dev.mateux

import dev.mateux.model.StatementResponse
import dev.mateux.model.TransactionPost
import dev.mateux.model.TransactionResponse
import io.smallrye.common.annotation.RunOnVirtualThread
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.Blocking
import kotlin.coroutines.coroutineContext

@Path("/clientes")
class ClientsResource(
    @Inject private var service: ClientService
) {
    @POST
    @Path("/{id}/transacoes")
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    fun transaction(body: TransactionPost, id: String): TransactionResponse {
        if (body.tipo == null || body.valor == null || body.descricao.isNullOrEmpty() || body.descricao.length > 10 || body.valor !is Int || body.tipo !in listOf("c", "d")) {
            throw WebApplicationException(422)
        }

        return service.addTransaction(id.toInt(), body.valor, body.tipo, body.descricao)
    }

    @GET
    @Path("/{id}/extrato")
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    fun statement(id: String): StatementResponse {
        return service.getStatement(id.toInt())
    }
}
