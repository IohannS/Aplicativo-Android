package com.iohann.mercadinhoestoque

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class ProdutoAdapter(
    private var produtoList: MutableList<Produto>,
    private val databaseHelper: DatabaseHelper
) : RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder>() {

    // Interface para comunicação com MainActivity
    interface OnValorTotalChangedListener {
        fun onValorTotalChanged()
    }

    private var listener: OnValorTotalChangedListener? = null

    fun setOnValorTotalChangedListener(listener: OnValorTotalChangedListener) {
        this.listener = listener
    }

    class ProdutoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNome: TextView = itemView.findViewById(R.id.tvNome)
        val tvQuantidade: TextView = itemView.findViewById(R.id.tvQuantidade)
        val tvPrecoUnitario: TextView = itemView.findViewById(R.id.tvPrecoUnitario)
        val tvValorTotalProduto: TextView = itemView.findViewById(R.id.tvValorTotalProduto)
        val tvCategoria: TextView = itemView.findViewById(R.id.tvCategoria)
        val btnAumentar: Button = itemView.findViewById(R.id.btnAumentar)
        val btnDiminuir: Button = itemView.findViewById(R.id.btnDiminuir)
        val btnDeletar: Button = itemView.findViewById(R.id.btnDeletar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produto, parent, false)
        return ProdutoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        val produto = produtoList[position]
        val formatadorMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        holder.tvNome.text = produto.nome
        holder.tvQuantidade.text = produto.quantidade.toString()
        holder.tvPrecoUnitario.text = formatadorMoeda.format(produto.precoUnitario)

        // Calcular e mostrar valor total do produto
        val valorTotalProduto = produto.getValorTotal()
        holder.tvValorTotalProduto.text = formatadorMoeda.format(valorTotalProduto)

        holder.tvCategoria.text = produto.categoria.ifEmpty { "-" }

        // Botão aumentar quantidade
        holder.btnAumentar.setOnClickListener {
            val novaQuantidade = produto.quantidade + 1
            produto.quantidade = novaQuantidade
            databaseHelper.atualizarQuantidade(produto.id, novaQuantidade)

            // Atualizar valor total do produto
            val novoValorTotal = produto.getValorTotal()
            holder.tvQuantidade.text = novaQuantidade.toString()
            holder.tvValorTotalProduto.text = formatadorMoeda.format(novoValorTotal)

            // Notificar mudança no valor total geral
            listener?.onValorTotalChanged()
        }

        // Botão diminuir quantidade
        holder.btnDiminuir.setOnClickListener {
            if (produto.quantidade > 0) {
                val novaQuantidade = produto.quantidade - 1
                produto.quantidade = novaQuantidade
                databaseHelper.atualizarQuantidade(produto.id, novaQuantidade)

                // Atualizar valor total do produto
                val novoValorTotal = produto.getValorTotal()
                holder.tvQuantidade.text = novaQuantidade.toString()
                holder.tvValorTotalProduto.text = formatadorMoeda.format(novoValorTotal)

                // Notificar mudança no valor total geral
                listener?.onValorTotalChanged()
            }
        }

        // Botão deletar
        holder.btnDeletar.setOnClickListener {
            databaseHelper.deletarProduto(produto.id)
            produtoList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, produtoList.size)

            // Notificar mudança no valor total geral
            listener?.onValorTotalChanged()
        }
    }

    override fun getItemCount(): Int {
        return produtoList.size
    }

    fun atualizarLista(novaLista: List<Produto>) {
        produtoList.clear()
        produtoList.addAll(novaLista)
        notifyDataSetChanged()

        // Notificar mudança no valor total geral
        listener?.onValorTotalChanged()
    }
}