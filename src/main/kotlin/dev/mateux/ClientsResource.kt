package dev.mateux

import dev.mateux.entities.Client
import dev.mateux.entities.Transaction
import dev.mateux.model.*
import io.smallrye.common.annotation.RunOnVirtualThread
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import java.sql.Timestamp

@Path("/clientes")
class ClientsResource(
    @Inject private var entityManager: EntityManager,
) {
    @POST
    @Path("/{id}/transacoes")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @RunOnVirtualThread
    fun transaction(body: TransactionPost, id: String): TransactionResponse {
        if (body.tipo == null || body.valor == null || body.descricao.isNullOrEmpty() || body.descricao.length > 10 || body.valor !is Int || body.tipo !in listOf("c", "d")) {
            throw WebApplicationException(422)
        }

        if (id.toInt() !in 1..5) {
            throw WebApplicationException(404)
        }

        val client: Client = entityManager.find(Client::class.java, id, LockModeType.PESSIMISTIC_WRITE)
            ?: throw WebApplicationException(404)

        if (body.tipo == "d" && client.balance - body.valor < -client.limit) {
            throw WebApplicationException(422)
        }

        val transaction = Transaction(
            id = 0,
            clientId = id.toInt(),
            value = body.valor,
            type = body.tipo.toCharArray()[0],
            description = body.descricao,
        )
        entityManager.persist(transaction)
        client.updateBalance(body.valor, body.tipo)
        entityManager.merge(client)

        return TransactionResponse(
            saldo = client.balance,
            limite = client.limit,
        )
    }

    @GET
    @Path("/{id}/extrato")
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    fun statement(id: String): StatementResponse {
        if (id.toInt()  !in 1..5) {
            throw WebApplicationException(404)
        }

        val client: Client = entityManager.find(Client::class.java, id)
            ?: throw WebApplicationException(404)

        val transactions: List<StatementTransaction>? = entityManager.createQuery(
            """
                SELECT t
                  FROM Transaction t
                 WHERE t.clientId = :client_id
              ORDER BY t.id DESC
                 LIMIT 10
            """,
            Transaction::class.java
        )
            .setParameter("client_id", id)
            .resultList
            .stream()
            .map { transaction ->
                StatementTransaction(
                    description = transaction.description,
                    type = transaction.type.toString(),
                    value = transaction.value,
                    performedIn = transaction.createdAt.toInstant().toString()
                )
            }.toList()

        return StatementResponse(
            balance = Balance(
                description = client.balance,
                date = Timestamp(System.currentTimeMillis()).toInstant().toString(),
                value = client.limit,
            ),
            statementTransactions = transactions,
        )
    }
}
