package com.example.bicicletin.perfil

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bicicletin.R
import com.google.firebase.auth.FirebaseAuth

/**
 * Tela de perfil (aparece depois do login).
 * Tudo do perfil fica na pasta "perfil" para não misturar com o código do login.
 */
class PerfilActivity : AppCompatActivity() {

    private lateinit var uid: String
    private lateinit var imgFoto: ImageView

    // Abre a galeria para escolher a foto (não precisa de permissão)
    private val escolherFoto =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            if (PerfilStorage.salvarFoto(this, uid, uri)) {
                mostrarFoto()
            } else {
                Toast.makeText(this, "Não foi possível usar essa imagem.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)
        supportActionBar?.hide()

        // Sem usuário logado não há perfil para mostrar
        val usuario = FirebaseAuth.getInstance().currentUser
        if (usuario == null) {
            Toast.makeText(this, "Faça login novamente.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        uid = usuario.uid

        findViewById<View>(R.id.headerPerfil).aplicarInsetTopo()
        findViewById<View>(R.id.conteudoPerfil).aplicarInsetBase()
        imgFoto = findViewById(R.id.imgFoto)

        // ---- Cabeçalho
        findViewById<TextView>(R.id.txtNome).text = usuario.nomeExibicao()
        // TODO: mostrar o CPF real quando o cadastro salvar esse dado
        // findViewById<TextView>(R.id.txtCpf).text = "CPF: $cpf"

        mostrarFoto()
        findViewById<View>(R.id.containerFoto).setOnClickListener {
            escolherFoto.launch("image/*")
        }

        // ---- Menu
        findViewById<View>(R.id.itemDadosPessoais).setOnClickListener {
            startActivity(Intent(this, DadosPessoaisActivity::class.java))
        }
        findViewById<View>(R.id.itemPagamento).setOnClickListener {
            startActivity(Intent(this, PagamentoActivity::class.java))
        }
        findViewById<View>(R.id.itemHistoricoAlugueis).setOnClickListener {
            // TODO: outro integrante está fazendo o histórico de aluguéis.
            // Quando estiver pronto: startActivity(Intent(this, HistoricoActivity::class.java))
            Toast.makeText(this, "Em breve", Toast.LENGTH_SHORT).show()
        }
        // itemAjuda: ainda não existe a tela, então ficou sem ação de propósito.

        findViewById<View>(R.id.txtVerRotas).setOnClickListener {
            Toast.makeText(this, "Mapa indisponível no momento", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Atualiza os números sempre que a tela volta a aparecer
        atualizarAtividade(PerfilStorage.lerAtividade(this, uid))
    }

    private fun mostrarFoto() {
        val foto = PerfilStorage.lerFoto(this, uid)
        if (foto != null) imgFoto.setImageBitmap(foto)
        else imgFoto.setImageResource(R.drawable.perfil_avatar_padrao)
    }

    /** Preenche os 3 cartões de "Minha Atividade". Os números vêm de PerfilStorage.lerAtividade. */
    private fun atualizarAtividade(atividade: Atividade) {
        findViewById<TextView>(R.id.txtKm).text = String.format("%.1f km", atividade.km)
        findViewById<TextView>(R.id.txtBikes).text = atividade.bikesAlugadas.toString()
        findViewById<TextView>(R.id.txtTempo).text =
            "${atividade.minutosAlugados / 60} h ${atividade.minutosAlugados % 60} min"
    }
}
