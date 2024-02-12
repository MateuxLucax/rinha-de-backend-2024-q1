package dev.mateux.entities

import io.quarkus.runtime.annotations.RegisterForReflection
import jakarta.persistence.Cacheable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.ws.rs.WebApplicationException

@Entity
@Table(name = "clientes")
@RegisterForReflection
@Cacheable
class Client(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    @Column(name = "saldo")
    val balance: Int = 0,
    @Column(name = "limite")
    val limit: Int = 0,
) {
    fun updateBalance(value: Int, type: String): Int {
        return when (type) {
            "c" -> balance + value
            "d" -> balance - value
            else -> throw WebApplicationException(422)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Client

        return id == other.id
    }

    override fun hashCode(): Int {
        return id
    }
}