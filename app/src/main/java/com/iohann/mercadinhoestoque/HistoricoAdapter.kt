package com.iohann.mercadinhoestoque

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class HistoricoAdapter(
    private var historicoList: List<HistoricoMensal>,
    private val databaseHelper: DatabaseHelper
) : RecyclerView.Adapter<HistoricoAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMesAno: TextView = itemView.findViewById(R.id.tvMesAno)
        val tvDataRegistro: TextView = itemView.findViewById(R.id.tvDataRegistro)
        val tvValorTotal: TextView = itemView.findViewById(R.id.tvValorTotal)
        val tvQuantidadeProdutos: TextView = itemView.findViewById(R.id.tvQuantidadeProdutos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historico, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val historico = historicoList[position]
        val formatadorMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        // Formatar mês/ano
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMMM/yyyy", Locale("pt", "BR"))
            val date = inputFormat.parse(historico.mesAno)
            val mesAnoFormatado = outputFormat.format(date ?: System.currentTimeMillis())
            holder.tvMesAno.text = mesAnoFormatado
        } catch (e: Exception) {
            holder.tvMesAno.text = historico.mesAno
        }

        // Formatar data de registro
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
            val date = inputFormat.parse(historico.dataRegistro)
            val dataFormatada = outputFormat.format(date ?: System.currentTimeMillis())
            holder.tvDataRegistro.text = "Salvo em: $dataFormatada"
        } catch (e: Exception) {
            holder.tvDataRegistro.text = "Salvo em: ${historico.dataRegistro}"
        }

        // Valor total
        holder.tvValorTotal.text = formatadorMoeda.format(historico.valorTotal)

        // Quantidade de produtos
        val produtos = databaseHelper.getProdutosDoHistorico(historico.mesAno)
        holder.tvQuantidadeProdutos.text = "${produtos.size} produtos"
    }

    override fun getItemCount(): Int {
        return historicoList.size
    }

    fun atualizarLista(novaLista: List<HistoricoMensal>) {
        historicoList = novaLista
        notifyDataSetChanged()
    }
}