package dev.mateux.model

import com.fasterxml.jackson.annotation.JsonProperty

data class StatementResponse(
    @field:JsonProperty("saldo")
    val balance: Balance? = null,

    @field:JsonProperty("ultimas_transacoes")
    val statementTransactions: List<StatementTransaction>? = null,
)

data class Balance(
    @field:JsonProperty("total")
    val description: Int? = null,

    @field:JsonProperty("data_extrato")
    val date: String? = null,

    @field:JsonProperty("limite")
    val value: Int? = null,
)

data class StatementTransaction(
    @field:JsonProperty("descricao")
    val description: String? = null,

    @field:JsonProperty("tipo")
    val type: String? = null,

    @field:JsonProperty("valor")
    val value: Int? = null,

    @field:JsonProperty("realizada_em")
    val performedIn: String? = null,
)