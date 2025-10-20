package com.iohann.mercadinhoestoque

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale
import android.content.Intent
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProdutoAdapter
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var tvValorTotalGeral: TextView
    private val produtoList = mutableListOf<Produto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar o banco de dados
        databaseHelper = DatabaseHelper(this)

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewProdutos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializar adapter
        adapter = ProdutoAdapter(produtoList, databaseHelper)
        recyclerView.adapter = adapter

        // Configurar o listener para atualizar valor total
        adapter.setOnValorTotalChangedListener(object : ProdutoAdapter.OnValorTotalChangedListener {
            override fun onValorTotalChanged() {
                atualizarValorTotalGeral()
            }
        })

        // Inicializar TextView do valor total geral
        tvValorTotalGeral = findViewById(R.id.tvValorTotalGeral)

        // Botão adicionar produto
        val btnAdicionar: Button = findViewById(R.id.btnAdicionar)
        btnAdicionar.setOnClickListener {
            mostrarDialogAdicionarProduto()
        }

        // NOVO: Botão salvar mês atual
        val btnSalvarMes: Button = findViewById(R.id.btnSalvarMes)
        btnSalvarMes.setOnClickListener {
            salvarMesAtual()
        }

        // Botão histórico
        val btnHistorico: Button = findViewById(R.id.btnHistorico)
        btnHistorico.setOnClickListener {
            val intent = Intent(this, HistoricoActivity::class.java)
            startActivity(intent)
        }

        // Carregar produtos do banco
        carregarProdutos()
    }

    private fun carregarProdutos() {
        produtoList.clear()
        produtoList.addAll(databaseHelper.getAllProdutos())
        adapter.notifyDataSetChanged()
        atualizarValorTotalGeral()
    }

    fun atualizarValorTotalGeral() {
        try {
            var valorTotal = 0.0
            for (produto in produtoList) {
                valorTotal += produto.getValorTotal()
            }

            val formatadorMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            tvValorTotalGeral.text = formatadorMoeda.format(valorTotal)

        } catch (e: Exception) {
            // Ignora erros
        }
    }

    // NOVO: Método para salvar o mês atual
    private fun salvarMesAtual() {
        if (produtoList.isEmpty()) {
            Toast.makeText(this, "Não há produtos para salvar", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val calendar = Calendar.getInstance()
            val mesAno = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
            val mesFormatado = SimpleDateFormat("MMMM 'de' yyyy", Locale("pt", "BR")).format(calendar.time)

            // Calcular valor total
            var valorTotal = 0.0
            for (produto in produtoList) {
                valorTotal += produto.getValorTotal()
            }

            // Primeiro deletar produtos antigos do histórico para evitar duplicação
            databaseHelper.deletarProdutosDoHistorico(mesAno)

            // Salvar valor total no histórico
            val idHistorico = databaseHelper.salvarValorMensal(mesAno, valorTotal)

            // Salvar produtos no histórico
            val produtosSalvos = databaseHelper.salvarProdutosNoHistorico(mesAno, produtoList)

            if (idHistorico != -1L && produtosSalvos) {
                val formatadorMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                val mensagem = """
                    Mês salvo com sucesso!
                    
                    Período: $mesFormatado
                    Valor total: ${formatadorMoeda.format(valorTotal)}
                    Produtos salvos: ${produtoList.size}
                    
                    Os produtos foram salvos no histórico mensal.
                """.trimIndent()

                AlertDialog.Builder(this)
                    .setTitle("Sucesso!")
                    .setMessage(mensagem)
                    .setPositiveButton("OK", null)
                    .show()
            } else {
                Toast.makeText(this, "Erro ao salvar o mês", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao salvar: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("MainActivity", "Erro ao salvar mês: ${e.message}")
        }
    }

    // Método para salvar no histórico mensal (automático)
    private fun salvarNoHistoricoMensal(valorTotal: Double) {
        try {
            val calendar = Calendar.getInstance()
            val mesAno = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
            databaseHelper.salvarValorMensal(mesAno, valorTotal)
        } catch (e: Exception) {
            Log.e("MainActivity", "Erro ao salvar histórico: ${e.message}")
        }
    }

    private fun mostrarDialogAdicionarProduto() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_produto)
        dialog.setCancelable(true)

        val etNome: EditText = dialog.findViewById(R.id.etNome)
        val etQuantidade: EditText = dialog.findViewById(R.id.etQuantidade)
        val etPrecoUnitario: EditText = dialog.findViewById(R.id.etPrecoUnitario)
        val tvValorTotal: TextView = dialog.findViewById(R.id.tvValorTotal)
        val etCategoria: EditText = dialog.findViewById(R.id.etCategoria)
        val btnSalvar: Button = dialog.findViewById(R.id.btnSalvar)

        // Função para calcular valor total automaticamente
        val calcularValorTotal = {
            try {
                val quantidade = etQuantidade.text.toString().toIntOrNull() ?: 0
                val precoUnitario = etPrecoUnitario.text.toString().toDoubleOrNull() ?: 0.0
                val valorTotal = quantidade * precoUnitario
                val formatadorMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                tvValorTotal.text = "Valor total: ${formatadorMoeda.format(valorTotal)}"
            } catch (e: Exception) {
                tvValorTotal.text = "Valor total: R$ 0,00"
            }
        }

        // Listeners para calcular valor total em tempo real
        etQuantidade.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { calcularValorTotal() }
        })

        etPrecoUnitario.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { calcularValorTotal() }
        })

        btnSalvar.setOnClickListener {
            try {
                val nome = etNome.text.toString().trim()
                val quantidadeStr = etQuantidade.text.toString().trim()
                val precoUnitarioStr = etPrecoUnitario.text.toString().trim()
                val categoria = etCategoria.text.toString().trim()

                if (nome.isEmpty()) {
                    Toast.makeText(this, "Digite o nome do produto", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (quantidadeStr.isEmpty()) {
                    Toast.makeText(this, "Digite a quantidade", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (precoUnitarioStr.isEmpty()) {
                    Toast.makeText(this, "Digite o preço unitário", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val quantidade = quantidadeStr.toInt()
                val precoUnitario = precoUnitarioStr.toDouble()

                // Criar e salvar produto
                val produto = Produto(
                    nome = nome,
                    quantidade = quantidade,
                    precoUnitario = precoUnitario,
                    categoria = categoria
                )
                databaseHelper.adicionarProduto(produto)

                // Atualizar lista
                carregarProdutos()

                // Fechar dialog
                dialog.dismiss()

                Toast.makeText(this, "Produto adicionado!", Toast.LENGTH_SHORT).show()

            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Digite números válidos", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Erro ao salvar produto", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}