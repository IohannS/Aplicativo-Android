package com.iohann.mercadinhoestoque

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "estoque.db"
        private const val DATABASE_VERSION = 3  // Incrementado para 3
        private const val TABLE_PRODUTOS = "produtos"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NOME = "nome"
        private const val COLUMN_QUANTIDADE = "quantidade"
        private const val COLUMN_PRECO = "preco"
        private const val COLUMN_CATEGORIA = "categoria"
        private const val TABLE_HISTORICO = "historico_mensal"
        private const val COLUMN_HISTORICO_ID = "id"
        private const val COLUMN_MES_ANO = "mes_ano"
        private const val COLUMN_VALOR_TOTAL = "valor_total"
        private const val COLUMN_DATA_REGISTRO = "data_registro"

        // Nova tabela para produtos do histórico
        private const val TABLE_HISTORICO_PRODUTOS = "historico_produtos"
        private const val COLUMN_HIST_PROD_ID = "id"
        private const val COLUMN_HIST_MES_ANO = "mes_ano"
        private const val COLUMN_HIST_NOME = "nome"
        private const val COLUMN_HIST_QUANTIDADE = "quantidade"
        private const val COLUMN_HIST_PRECO = "preco"
        private const val COLUMN_HIST_CATEGORIA = "categoria"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Criar tabela de produtos
        val createTableProdutos = """
            CREATE TABLE $TABLE_PRODUTOS(
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOME TEXT,
                $COLUMN_QUANTIDADE INTEGER,
                $COLUMN_PRECO REAL,
                $COLUMN_CATEGORIA TEXT
            )
        """.trimIndent()
        db.execSQL(createTableProdutos)

        // Criar tabela de histórico
        val createTableHistorico = """
            CREATE TABLE $TABLE_HISTORICO(
                $COLUMN_HISTORICO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_MES_ANO TEXT UNIQUE,
                $COLUMN_VALOR_TOTAL REAL,
                $COLUMN_DATA_REGISTRO DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()
        db.execSQL(createTableHistorico)

        // Criar tabela de produtos do histórico
        val createTableHistoricoProdutos = """
            CREATE TABLE $TABLE_HISTORICO_PRODUTOS(
                $COLUMN_HIST_PROD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_HIST_MES_ANO TEXT,
                $COLUMN_HIST_NOME TEXT,
                $COLUMN_HIST_QUANTIDADE INTEGER,
                $COLUMN_HIST_PRECO REAL,
                $COLUMN_HIST_CATEGORIA TEXT
            )
        """.trimIndent()
        db.execSQL(createTableHistoricoProdutos)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            // Criar a nova tabela de produtos do histórico
            val createTableHistoricoProdutos = """
                CREATE TABLE $TABLE_HISTORICO_PRODUTOS(
                    $COLUMN_HIST_PROD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_HIST_MES_ANO TEXT,
                    $COLUMN_HIST_NOME TEXT,
                    $COLUMN_HIST_QUANTIDADE INTEGER,
                    $COLUMN_HIST_PRECO REAL,
                    $COLUMN_HIST_CATEGORIA TEXT
                )
            """.trimIndent()
            db.execSQL(createTableHistoricoProdutos)
        }
    }

    // Adicionar novo produto
    fun adicionarProduto(produto: Produto): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOME, produto.nome)
            put(COLUMN_QUANTIDADE, produto.quantidade)
            put(COLUMN_PRECO, produto.precoUnitario)
            put(COLUMN_CATEGORIA, produto.categoria)
        }
        val id = db.insert(TABLE_PRODUTOS, null, values)
        db.close()
        return id
    }

    // Buscar todos os produtos
    fun getAllProdutos(): List<Produto> {
        val produtoList = mutableListOf<Produto>()
        val selectQuery = "SELECT * FROM $TABLE_PRODUTOS ORDER BY $COLUMN_NOME"

        val db = this.readableDatabase
        val cursor: Cursor? = db.rawQuery(selectQuery, null)

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val produto = Produto(
                        id = it.getInt(it.getColumnIndexOrThrow(COLUMN_ID)),
                        nome = it.getString(it.getColumnIndexOrThrow(COLUMN_NOME)),
                        quantidade = it.getInt(it.getColumnIndexOrThrow(COLUMN_QUANTIDADE)),
                        precoUnitario = it.getDouble(it.getColumnIndexOrThrow(COLUMN_PRECO)),
                        categoria = it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORIA))
                    )
                    produtoList.add(produto)
                } while (it.moveToNext())
            }
        }
        db.close()
        return produtoList
    }

    // Atualizar quantidade
    fun atualizarQuantidade(id: Int, novaQuantidade: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_QUANTIDADE, novaQuantidade)
        }
        db.update(TABLE_PRODUTOS, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    // Deletar produto
    fun deletarProduto(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_PRODUTOS, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    // Salvar/atualizar valor total do mês
    fun salvarValorMensal(mesAno: String, valorTotal: Double): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_MES_ANO, mesAno)
            put(COLUMN_VALOR_TOTAL, valorTotal)
        }

        // Usar INSERT OR REPLACE para atualizar se já existir
        val id = db.replace(TABLE_HISTORICO, null, values)
        db.close()
        return id
    }

    // Buscar todos os históricos
    fun getHistoricoMensal(): List<HistoricoMensal> {
        val historicoList = mutableListOf<HistoricoMensal>()
        val selectQuery = "SELECT * FROM $TABLE_HISTORICO ORDER BY $COLUMN_DATA_REGISTRO DESC"

        val db = this.readableDatabase
        val cursor: Cursor? = db.rawQuery(selectQuery, null)

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val historico = HistoricoMensal(
                        id = it.getInt(it.getColumnIndexOrThrow(COLUMN_HISTORICO_ID)),
                        mesAno = it.getString(it.getColumnIndexOrThrow(COLUMN_MES_ANO)),
                        valorTotal = it.getDouble(it.getColumnIndexOrThrow(COLUMN_VALOR_TOTAL)),
                        dataRegistro = it.getString(it.getColumnIndexOrThrow(COLUMN_DATA_REGISTRO))
                    )
                    historicoList.add(historico)
                } while (it.moveToNext())
            }
        }
        db.close()
        return historicoList
    }

    // Buscar histórico de um mês específico
    fun getHistoricoPorMes(mesAno: String): HistoricoMensal? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_HISTORICO,
            null,
            "$COLUMN_MES_ANO = ?",
            arrayOf(mesAno),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                HistoricoMensal(
                    id = it.getInt(it.getColumnIndexOrThrow(COLUMN_HISTORICO_ID)),
                    mesAno = it.getString(it.getColumnIndexOrThrow(COLUMN_MES_ANO)),
                    valorTotal = it.getDouble(it.getColumnIndexOrThrow(COLUMN_VALOR_TOTAL)),
                    dataRegistro = it.getString(it.getColumnIndexOrThrow(COLUMN_DATA_REGISTRO))
                )
            } else {
                null
            }
        }
    }

    // NOVO: Salvar produtos no histórico
    fun salvarProdutosNoHistorico(mesAno: String, produtos: List<Produto>): Boolean {
        val db = this.writableDatabase
        var success = true

        try {
            // Salvar cada produto no histórico
            for (produto in produtos) {
                val values = ContentValues().apply {
                    put(COLUMN_HIST_MES_ANO, mesAno)
                    put(COLUMN_HIST_NOME, produto.nome)
                    put(COLUMN_HIST_QUANTIDADE, produto.quantidade)
                    put(COLUMN_HIST_PRECO, produto.precoUnitario)
                    put(COLUMN_HIST_CATEGORIA, produto.categoria)
                }
                val result = db.insert(TABLE_HISTORICO_PRODUTOS, null, values)
                if (result == -1L) {
                    success = false
                }
            }
        } catch (e: Exception) {
            success = false
        } finally {
            db.close()
        }
        return success
    }

    // NOVO: Buscar produtos do histórico por mês
    fun getProdutosDoHistorico(mesAno: String): List<Produto> {
        val produtoList = mutableListOf<Produto>()
        val selectQuery = "SELECT * FROM $TABLE_HISTORICO_PRODUTOS WHERE $COLUMN_HIST_MES_ANO = ? ORDER BY $COLUMN_HIST_NOME"

        val db = this.readableDatabase
        val cursor: Cursor? = db.rawQuery(selectQuery, arrayOf(mesAno))

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val produto = Produto(
                        id = it.getInt(it.getColumnIndexOrThrow(COLUMN_HIST_PROD_ID)),
                        nome = it.getString(it.getColumnIndexOrThrow(COLUMN_HIST_NOME)),
                        quantidade = it.getInt(it.getColumnIndexOrThrow(COLUMN_HIST_QUANTIDADE)),
                        precoUnitario = it.getDouble(it.getColumnIndexOrThrow(COLUMN_HIST_PRECO)),
                        categoria = it.getString(it.getColumnIndexOrThrow(COLUMN_HIST_CATEGORIA))
                    )
                    produtoList.add(produto)
                } while (it.moveToNext())
            }
        }
        db.close()
        return produtoList
    }

    // NOVO: Deletar produtos do histórico por mês (para evitar duplicação)
    fun deletarProdutosDoHistorico(mesAno: String) {
        val db = this.writableDatabase
        db.delete(TABLE_HISTORICO_PRODUTOS, "$COLUMN_HIST_MES_ANO = ?", arrayOf(mesAno))
        db.close()
    }
}