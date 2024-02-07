package dev.mateux.model

import com.fasterxml.jackson.annotation.JsonProperty

data class TransactionPost(
    @field:JsonProperty("valor")
    val valor: Int? = null,

    @field:JsonProperty("tipo")
    val tipo: String? = null,


    @field:JsonProperty("descricao")
    val descricao: String? = null
)