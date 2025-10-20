package com.iohann.mercadinhoestoque

data class Produto(
    var id: Int = 0,
    var nome: String = "",
    var quantidade: Int = 0,
    var precoUnitario: Double = 0.0,  // Este campo deve existir
    var categoria: String = ""
) {
    // Método para calcular valor total do produto
    fun getValorTotal(): Double {
        return quantidade * precoUnitario
    }
}