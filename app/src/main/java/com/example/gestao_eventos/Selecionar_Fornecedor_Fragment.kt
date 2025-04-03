package com.example.gestao_eventos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.gestao_eventos.databinding.FragmentSelecionarFornecedorBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class Selecionar_Fornecedor_Fragment : Fragment() {

    private var _binding: FragmentSelecionarFornecedorBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var eventoId: String

    companion object {
        fun newInstance(): Selecionar_Fornecedor_Fragment {
            return Selecionar_Fornecedor_Fragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSelecionarFornecedorBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()

        eventoId = arguments?.getString("eventoId") ?: ""

        carregarFornecedores()

        binding.btFinalizar.setOnClickListener {
            adicionarFornecedoresNoEvento(binding.containerFornecedor)
            mostrarDialogConfirmacao()

        }

        return binding.root
    }

    private fun carregarFornecedores() {
        db.collection("Fornecedores")
            .get()
            .addOnSuccessListener { result ->
                val listaFornecedores = mutableListOf<Pair<String, String>>()
                for (doc in result) {
                    val nome = doc.getString("Nome") ?: ""
                    val localizacao = doc.getString("Localização") ?: ""
                    listaFornecedores.add(Pair(nome, localizacao))
                }

                if (listaFornecedores.isEmpty()) {
                    Toast.makeText(context, "Nenhum fornecedor encontrado!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Fornecedores encontrados!", Toast.LENGTH_SHORT).show()

                    adicionarFornecedores(binding.containerFornecedor, listaFornecedores)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Erro ao buscar fornecedores: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun adicionarFornecedores(container: LinearLayout, fornecedores: List<Pair<String,String>>){
        container.removeAllViews() // Limpa os fornecedores anteriores

        for ((nome, localizacao) in fornecedores) {
            val fornecedorView = layoutInflater.inflate(R.layout.item_fornecedor, container, false)

            val txtNome = fornecedorView.findViewById<TextView>(R.id.txtNomeFornecedor)
            val txtLocalizacao = fornecedorView.findViewById<TextView>(R.id.txtLocalizacaoFornecedor)
            val checkBoxSelecionado = fornecedorView.findViewById<CheckBox>(R.id.checkBoxSelecionado)

            txtNome.text = nome
            txtLocalizacao.text = localizacao

            container.addView(fornecedorView)
        }
    }

    private fun adicionarFornecedoresNoEvento(container: LinearLayout){
        val fornecedoresSelecionados = mutableListOf<HashMap<String,String>>()

        for (i in 0 until container.childCount) {
            val fornecedorView = container.getChildAt(i)
            val checkBox = fornecedorView.findViewById<CheckBox>(R.id.checkBoxSelecionado)

            if(checkBox.isChecked){
                val nome = fornecedorView.findViewById<TextView>(R.id.txtNomeFornecedor)
                val localizacao = fornecedorView.findViewById<TextView>(R.id.txtLocalizacaoFornecedor)

                val fornecedor = hashMapOf(
                    "nome" to nome.text.toString(),
                    "localizacao" to localizacao.text.toString()
                )
                fornecedoresSelecionados.add(fornecedor)
            }
        }

        if (fornecedoresSelecionados.isNotEmpty()){
            db.collection("eventos").document(eventoId)
                .update("fornecedores", fornecedoresSelecionados)
                .addOnSuccessListener {
                    Toast.makeText(context, "Convidados adicionados ao evento!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Erro ao adicionar convidados: ${e.message}", Toast.LENGTH_SHORT).show()
                }

        }
        else {
            Toast.makeText(context, "Selecione ao menos um Fornecedor", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogConfirmacao()
    {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Enviar dados por Email")
        builder.setMessage("Deseja enviar os dados por email?")

        builder.setPositiveButton("Sim") { _, _ ->
            enviarEmail()
        }

        builder.setNegativeButton("Não") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private  fun enviarEmail()
    {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email

        if (userEmail == null) {
            Toast.makeText(context, "Erro: Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("eventos").document(eventoId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists())
                {
                    val tituloEvento = document.getString("titulo") ?:"Sem nome"
                    val tipoEvento = document.getString("tipoEvento") ?:"Sem tipo"
                    val localizacaoEvento = document.getString("localizacao") ?:"Sem localizacao"
                    val horaInicioEvento = document.getString("horaInicio") ?:"Sem horaInicio"
                    val horaFimEvento = document.getString("horaFim") ?:"Sem horaFim"
                    val descricao = document.getString("descricao") ?:"Sem descricao"
                    val dataInicio = document.getString("dataInicio") ?:"Sem dataInicio"
                    val dataFim = document.getString("dataFim") ?:"Sem dataFim"

                    val fornecedores = document.get("fornecedores") as? List<Map<String, String>> ?: emptyList()
                    val convidados = document.get("convidados") as? List<Map<String, String>> ?: emptyList()

                    val fornecedoresHtml = fornecedores.joinToString("") {
                        "<li>${it["nome"]} - ${it["localizacao"]}</li>"
                    }

                    val convidadosHtml = convidados.joinToString("") {
                        "<li>${it["nome"]} - ${it["email"]}</li>"
                    }

                    val corpoEmail = """
                    <html>
                        <body>
                            <h2>Detalhes do Evento</h2>
                            <p><strong>Titulo:</strong> $tituloEvento</p>
                            <p><strong>Tipo de Evento:</strong> $tipoEvento</p>
                            <p><strong>Descrição:</strong> $descricao</p>
                            <p><strong>Local:</strong> $localizacaoEvento</p>
                            <p><strong>Hora de Inicio:</strong> $horaInicioEvento</p>
                            <p><strong>Hora de Fim:</strong> $horaFimEvento</p>
                            <p><strong>Data de Inicio:</strong> $dataInicio</p>
                            <p><strong>Data de Fim:</strong> $dataFim</p>

                            <h3>Fornecedores Selecionados:</h3>
                            <ul>$fornecedoresHtml</ul>

                            <h3>Convidados:</h3>
                            <ul>$convidadosHtml</ul>

                            <br>
                            <p>Atenciosamente,</p>
                            <p><strong>Equipe Gestão Eventos</strong></p>
                        </body>
                    </html>
                    """.trimIndent()
                    enviarEmailComConteudo(corpoEmail, userEmail)

                }else {
                    Toast.makeText(context, "Evento não encontrado", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Erro ao buscar evento: ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun enviarEmailComConteudo(corpoEmail: String, destinatario: String) {
        val client = OkHttpClient()

        val json = JSONObject().apply {
            put("personalizations", JSONArray().put(JSONObject().apply {
                put("to", JSONArray().put(JSONObject().put("email", destinatario)))
            }))
            put("from", JSONObject().put("email", "eventosgestao56@gmail.com"))
            put("subject", "Detalhes do Evento")
            put("content", JSONArray().put(JSONObject().apply {
                put("type", "text/html")
                put("value", corpoEmail)
            }))
        }

        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url("https://api.sendgrid.com/v3/mail/send")
            .addHeader("Authorization", "Bearer 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")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("SendGrid", "Erro ao enviar email: ${e.message}")
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Erro ao enviar email", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                requireActivity().runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Email enviado com sucesso para $destinatario!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Erro ao enviar email: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
