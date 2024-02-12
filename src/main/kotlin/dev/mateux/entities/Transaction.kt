package dev.mateux.entities

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.ws.rs.WebApplicationException
import java.sql.Timestamp

@Entity
@Table(name = "transacoes")
@RegisterForReflection
class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    @Column(name = "cliente_id")
    val clientId: Int = 0,
    @Column(name = "valor")
    val value: Int = 0,
    @Column(name = "tipo")
    val type: Char = 'd',
    @Column(name = "descricao")
    val description: String = "",
    @Column(name = "realizada_em")
    val createdAt: Timestamp = Timestamp(System.currentTimeMillis()),
) {
    override fun hashCode(): Int {
        return id
    }
}