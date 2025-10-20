package com.iohann.mercadinhoestoque

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HistoricoMensal(
    var id: Int = 0,
    var mesAno: String = "", // Formato: "2024-01"
    var valorTotal: Double = 0.0,
    var dataRegistro: String = ""
) {
    // Formatar para exibição
    fun getMesAnoFormatado(): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM/yyyy", Locale("pt", "BR"))
            val date = inputFormat.parse(mesAno)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            mesAno
        }
    }

    fun getValorFormatado(): String {
        val formatador = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("pt", "BR"))
        return formatador.format(valorTotal)
    }
}