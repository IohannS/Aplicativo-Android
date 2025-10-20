package com.iohann.mercadinhoestoque

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HistoricoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoricoAdapter
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var tvMesAtual: TextView
    private lateinit var tvValorMesAtual: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico)

        databaseHelper = DatabaseHelper(this)

        // Configurar botão voltar
        val btnVoltar: Button = findViewById(R.id.btnVoltar)
        btnVoltar.setOnClickListener {
            finish()
        }

        // Configurar textos do mês atual
        tvMesAtual = findViewById(R.id.tvMesAtual)
        tvValorMesAtual = findViewById(R.id.tvValorMesAtual)

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewHistorico)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = HistoricoAdapter(emptyList(), databaseHelper)
        recyclerView.adapter = adapter

        // Carregar dados
        carregarDados()
    }

    private fun carregarDados() {
        try {
            // Carregar mês atual
            val calendar = Calendar.getInstance()
            val mesAnoAtual = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.time)
            val mesAnoFormatado = SimpleDateFormat("MMMM/yyyy", Locale("pt", "BR")).format(calendar.time)

            tvMesAtual.text = mesAnoFormatado

            val historicoAtual = databaseHelper.getHistoricoPorMes(mesAnoAtual)
            val valorAtual = historicoAtual?.valorTotal ?: 0.0
            val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            tvValorMesAtual.text = formatador.format(valorAtual)

            // Carregar histórico completo (excluindo o mês atual da lista)
            val historicoCompleto = databaseHelper.getHistoricoMensal()
                .filter { it.mesAno != mesAnoAtual }

            adapter.atualizarLista(historicoCompleto)

        } catch (e: Exception) {
            // Log de erro se necessário
        }
    }
}