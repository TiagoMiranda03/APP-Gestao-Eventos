package com.example.gestao_eventos

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.gestao_eventos.databinding.FragmentSelecionarFornecedorBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.math.pow

class Selecionar_Fornecedor_Fragment : Fragment() {

    private var _binding: FragmentSelecionarFornecedorBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLatitude: Double?= null
    private var userLongitude: Double?= null

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        verificarPermissaoLocalizacao()

        eventoId = arguments?.getString("eventoId") ?: ""
        carregarFornecedores()

        binding.btFinalizar.setOnClickListener {
            adicionarFornecedoresNoEvento(binding.containerFornecedor)
            mostrarDialogConfirmacao()
        }

        val opcoes = listOf("Todos","Ordenar por nome", "Ordenar por distância")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, opcoes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerOrdenar.adapter = adapter

        binding.spinnerOrdenar.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> carregarFornecedores()
                    1 -> ordenarPorNome()
                    2 -> ordenarPorDistancia()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        restaurarListaFornecedores()
    }


    fun restaurarListaFornecedores() {
        binding.containerPesquisaFiltro.visibility = View.VISIBLE
        binding.containerFornecedorScrollView.visibility = View.VISIBLE
        binding.btFinalizar.visibility = View.VISIBLE
        binding.fragmentContainerDetalhes.visibility = View.GONE
    }

    private fun adicionarFornecedoresNoEvento(container: LinearLayout) {
        Log.d("DEBUG", "Adicionar Fornecedores ao Evento")

        if (container == null) {
            Log.e("DEBUG", "Container é NULL")
            Toast.makeText(context, "Erro: Container de Fornecedores não encontrado!", Toast.LENGTH_SHORT).show()
            return
        }

        val fornecedoresSelecionados = mutableListOf<Map<String, String>>()

        Log.d("DEBUG", "Total de Views no Container: ${container.childCount}")

        for (i in 0 until container.childCount) {
            val fornecedorView = container.getChildAt(i)
            val checkBox = fornecedorView.findViewById<CheckBox>(R.id.checkBoxSelecionado)

            if (checkBox?.isChecked == true) {
                val nome = fornecedorView.findViewById<TextView>(R.id.txtNomeFornecedor)?.text.toString()
                val localizacao = fornecedorView.findViewById<TextView>(R.id.txtLocalizacaoFornecedor)?.text.toString()

                val fornecedor = mapOf(
                    "nome" to nome,
                    "localizacao" to localizacao
                )
                fornecedoresSelecionados.add(fornecedor)
            }
        }

        Log.d("DEBUG", "Total de Fornecedores Selecionados: ${fornecedoresSelecionados.size}")

        if (fornecedoresSelecionados.isNotEmpty()) {
            db.collection("eventos").document(eventoId)
                .update("fornecedores", fornecedoresSelecionados)
                .addOnSuccessListener {
                    Toast.makeText(context, "Fornecedores adicionados ao evento!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Erro ao adicionar fornecedores: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("DEBUG", "Erro ao adicionar fornecedores: ${e.message}")
                }
        } else {
            Toast.makeText(context, "Selecione ao menos um fornecedor", Toast.LENGTH_SHORT).show()
        }
    }


    private fun ordenarPorNome(){
        db.collection("Fornecedores")
            .get()
            .addOnSuccessListener { result ->
            val listaFornecedores = mutableListOf<Map<String, String>>()
            for (doc in result){
                val fornecedor = mapOf(
                    "nome" to (doc.getString("Nome") ?: ""),
                    "localizacao" to (doc.getString("Localização") ?: ""),
                    "contacto" to (doc.getString("Contacto") ?: ""),
                    "preco" to (doc.getString("Preço") ?: ""),
                    "tipoServico" to (doc.getString("Tipo_Serviço") ?: "")
                )
                listaFornecedores.add(fornecedor)
            }
            val fornecedoresOrdenados = listaFornecedores.sortedBy { it["nome"]?.lowercase() }
            adicionarFornecedores(binding.containerFornecedor, fornecedoresOrdenados)
        }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Erro ao ordenar por nome: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun ordenarPorDistancia() {
        if (userLatitude == null || userLongitude == null) {
            Toast.makeText(context, "Localização atual não disponível", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("Fornecedores")
            .get()
            .addOnSuccessListener { result ->
                val listaFornecedores = mutableListOf<Map<String, Any>>()

                for (doc in result) {
                    val latitude = doc.getString("Latitude")
                    val longitude = doc.getString("Longitude")

                    if (!latitude.isNullOrBlank() && !longitude.isNullOrBlank()) {
                        try {
                            val lat = latitude.toDouble()
                            val lon = longitude.toDouble()

                            val distancia = calcularDistancia(userLatitude!!, userLongitude!!, lat, lon)

                            val fornecedor = mapOf(
                                "nome" to (doc.getString("Nome") ?: ""),
                                "localizacao" to (doc.getString("Localização") ?: ""),
                                "contacto" to (doc.getString("Contacto") ?: ""),
                                "preco" to (doc.getString("Preço") ?: ""),
                                "tipoServico" to (doc.getString("Tipo_Serviço") ?: ""),
                                "distancia" to distancia
                            )

                            listaFornecedores.add(fornecedor)
                        } catch (e: Exception) {
                            Log.e("OrdenarDistancia", "Erro ao converter coordenadas: ${e.message}")
                        }
                    }
                }

                val listaOrdenada = listaFornecedores.sortedBy { it["distancia"] as Double }

                // Atualizar o UI
                val containerFornecedor = view?.findViewById<LinearLayout>(R.id.containerFornecedor)
                containerFornecedor?.removeAllViews()
                for (fornecedor in listaOrdenada) {
                    val view = criarCardFornecedor(fornecedor) // função que cria a View
                    containerFornecedor?.addView(view)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Erro ao buscar fornecedores: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2).pow(2.0) + Math.cos(Math.toRadians(lat1)) *
                Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2).pow(2.0)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    private fun criarCardFornecedor(fornecedor: Map<String, Any>): View {
        val fornecedorView = layoutInflater.inflate(R.layout.item_fornecedor, null, false)

        val txtNome = fornecedorView.findViewById<TextView>(R.id.txtNomeFornecedor)
        val txtCategoriaFornecedor = fornecedorView.findViewById<TextView>(R.id.txtCategoriaFornecedor)
        val txtContactoFornecedor = fornecedorView.findViewById<TextView>(R.id.txtContactoFornecedor)
        val txtLocalizacaoFornecedor = fornecedorView.findViewById<TextView>(R.id.txtLocalizacaoFornecedor)
        val txtPrecoFornecedor = fornecedorView.findViewById<TextView>(R.id.txtPrecoFornecedor)
        val btnVerMaisDetalhes = fornecedorView.findViewById<Button>(R.id.btnVerMaisDetalhes)

        Log.d("DEBUG", "Fornecedor Card Criado")
        Log.d("DEBUG", "Botão Ver Mais Detalhes: ${btnVerMaisDetalhes != null}")

        // Preencher os dados
        txtNome.text = fornecedor["nome"] as? String ?: ""
        txtCategoriaFornecedor.text = "Categoria: " + (fornecedor["tipoServico"] as? String ?: "")
        txtContactoFornecedor.text = "Contacto: " + (fornecedor["contacto"] as? String ?: "")
        txtLocalizacaoFornecedor.text = "Localização: " + (fornecedor["localizacao"] as? String ?: "")
        txtPrecoFornecedor.text = "Preço: " + (fornecedor["preco"] as? String ?: "") + "€"

        // Ao clicar no botão, abrir o fragmento de detalhes
        btnVerMaisDetalhes.setOnClickListener {
            val fornecedorId = fornecedor["id"] as? String
            Log.d("DEBUG", "Botão Ver Mais Detalhes Clicado, Fornecedor ID: $fornecedorId")

            if (!fornecedorId.isNullOrEmpty()) {
                abrirDetalhesFornecedor(fornecedorId)
            } else {
                Toast.makeText(requireContext(), "ID do fornecedor não encontrado.", Toast.LENGTH_SHORT).show()
            }
        }

        return fornecedorView
    }


    private fun abrirDetalhesFornecedor(fornecedorId: String) {
        val fragment = Detalhes_Fornecedores_Fragment.newInstance(fornecedorId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack("DETALHES_FORNECEDOR")
            .commit()
    }


    private fun verificarPermissaoLocalizacao(){
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            obterLocalizacaoAtual()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        }
    }

    @SuppressLint("MissingPermission")
    private fun obterLocalizacaoAtual() {
        val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates = 1
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    userLatitude = location.latitude
                    userLongitude = location.longitude
                    Log.d("Localizacao", "Latitude: $userLatitude, Longitude: $userLongitude")
                    Toast.makeText(requireContext(), "Lat: $userLatitude, Lon: $userLongitude", Toast.LENGTH_LONG).show()
                } else {
                    Log.d("Localizacao", "Localização ainda não disponível.")
                    Toast.makeText(requireContext(), "Localização ainda não disponível.", Toast.LENGTH_LONG).show()
                }
            }
        }, requireActivity().mainLooper)
    }

    private fun carregarFornecedores() {
        Log.d("DEBUG", "Iniciar carregamento de fornecedores")

        db.collection("Fornecedores")
            .get()
            .addOnSuccessListener { result ->
                val listaFornecedores = mutableListOf<Map<String, Any>>()
                for (doc in result) {
                    val fornecedor = mapOf(
                        "id" to doc.id, // Inclui o ID do documento
                        "nome" to (doc.getString("Nome") ?: ""),
                        "localizacao" to (doc.getString("Localização") ?: ""),
                        "contacto" to (doc.getString("Contacto") ?: ""),
                        "preco" to (doc.getString("Preço") ?: ""),
                        "tipoServico" to (doc.getString("Tipo_Serviço") ?: "")
                    )
                    listaFornecedores.add(fornecedor)
                }

                Log.d("DEBUG", "Fornecedores carregados: ${listaFornecedores.size}")

                if (listaFornecedores.isEmpty()) {
                    Toast.makeText(context, "Nenhum fornecedor encontrado!", Toast.LENGTH_SHORT).show()
                } else {
                    adicionarFornecedores(binding.containerFornecedor, listaFornecedores)
                }
            }
            .addOnFailureListener { e ->
                Log.e("DEBUG", "Erro ao carregar fornecedores: ${e.message}")
                Toast.makeText(context, "Erro ao buscar fornecedores: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun adicionarFornecedores(container: LinearLayout, fornecedores: List<Map<String, Any>>) {
        Log.d("DEBUG", "Adicionar Fornecedores: ${fornecedores.size}")
        container.removeAllViews() // Limpa os fornecedores anteriores

        for (fornecedor in fornecedores) {
            val fornecedorView = criarCardFornecedor(fornecedor)
            Log.d("DEBUG", "Fornecedor Adicionado ao Layout: ${fornecedor["nome"]}")
            container.addView(fornecedorView)
        }
    }

    private fun adicionarFornecedorCompletos(fornecedor: Map<String, String>) {
        val fornecedorCompletos = hashMapOf(
            "nome" to fornecedor["nome"],
            "localizacao" to fornecedor["localizacao"],
            "contacto" to fornecedor["contacto"],
            "preco" to fornecedor["preco"],
            "tipo_servico" to fornecedor["tipoServico"]
        )

        // Agora, adicionamos esses dados ao evento
        db.collection("eventos").document(eventoId)
            .update("fornecedores", FieldValue.arrayUnion(fornecedorCompletos))
            .addOnSuccessListener {
                Toast.makeText(context, "Fornecedor adicionado ao evento!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Erro ao adicionar fornecedor: ${e.message}", Toast.LENGTH_SHORT).show()
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

                    val tituloDesencriptado = CryptoUtils.decrypt(tituloEvento)
                    val tipoDesencriptado = CryptoUtils.decrypt(tipoEvento)
                    val HoraInicioDesencriptado = CryptoUtils.decrypt(horaInicioEvento)
                    val horaFimDesencriptado = CryptoUtils.decrypt(horaFimEvento)
                    val LocalizacaoDesencriptado = CryptoUtils.decrypt(localizacaoEvento)
                    val descricaoDesencriptado = CryptoUtils.decrypt(descricao)
                    val dataInicioDesencriptado = CryptoUtils.decrypt(dataInicio)
                    val dataFimDesencriptado = CryptoUtils.decrypt(dataFim)

                    val fornecedores = document.get("fornecedores") as? List<Map<String, String>> ?: emptyList()
                    val convidados = document.get("convidados") as? List<Map<String, String>> ?: emptyList()

                    val fornecedoresHtml = fornecedores.joinToString("") {
                        "<li>${it["nome"]} - ${it["localizacao"]}</li>"
                    }

                    val convidadosHtml = convidados.joinToString("") {
                        val nomeDesencriptado = CryptoUtils.decrypt(it["nome"] ?: "")
                        val emailDesencriptado = CryptoUtils.decrypt(it["email"] ?: "")
                        "<li>$nomeDesencriptado - $emailDesencriptado</li>"
                    }

                    val corpoEmail = """
                    <html>
                        <body>
                            <h2>Detalhes do Evento</h2>
                            <p><strong>Titulo:</strong> $tituloDesencriptado</p>
                            <p><strong>Tipo de Evento:</strong> $tipoDesencriptado</p>
                            <p><strong>Descrição:</strong> $descricaoDesencriptado</p>
                            <p><strong>Local:</strong> $LocalizacaoDesencriptado</p>
                            <p><strong>Hora de Inicio:</strong> $HoraInicioDesencriptado</p>
                            <p><strong>Hora de Fim:</strong> $horaFimDesencriptado</p>
                            <p><strong>Data de Inicio:</strong> $dataInicioDesencriptado</p>
                            <p><strong>Data de Fim:</strong> $dataFimDesencriptado</p>

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
