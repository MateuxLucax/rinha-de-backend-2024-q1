package dev.mateux.model

import com.fasterxml.jackson.annotation.JsonProperty

data class TransactionResponse(
    @field:JsonProperty("saldo")
    val saldo: Int? = null,

    @field:JsonProperty("limite")
    val limite: Int? = null,
)
