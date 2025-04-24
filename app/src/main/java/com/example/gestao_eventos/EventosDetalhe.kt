package com.example.gestao_eventos

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.gestao_eventos.databinding.FragmentEventosDetalheBinding
import com.google.firebase.firestore.FirebaseFirestore

class EventosDetalhe : Fragment() {

    private lateinit var binding: FragmentEventosDetalheBinding
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "EventosDetalhe"

    companion object {
        private const val ARG_EVENTO_ID = "evento_id"
        fun newInstance(eventoId: String) = EventosDetalhe().apply {
            arguments = Bundle().apply { putString(ARG_EVENTO_ID, eventoId) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventosDetalheBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val eventoId = arguments?.getString(ARG_EVENTO_ID) ?: return

        db.collection("eventos").document(eventoId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    binding.tvTituloEvento.text = "Evento não encontrado"
                    return@addOnSuccessListener
                }

                // Desencriptação dos campos principais
                binding.tvTituloEvento.text = CryptoUtils.decrypt(doc.getString("titulo") ?: "")
                binding.tvTipoEvento.apply {
                    text = "Tipo: ${CryptoUtils.decrypt(doc.getString("tipoEvento") ?: "")}"
                    setTextColor(Color.BLACK)
                }
                binding.tvDescricao.apply {
                    text = "Descrição: ${CryptoUtils.decrypt(doc.getString("descricao") ?: "")}"
                    setTextColor(Color.BLACK)
                }
                binding.tvLocal.apply {
                    text = "Local: ${CryptoUtils.decrypt(doc.getString("localizacao") ?: "")}"
                    setTextColor(Color.BLACK)
                }
                val dt = CryptoUtils.decrypt(doc.getString("dataInicio") ?: "")
                val hi = CryptoUtils.decrypt(doc.getString("horaInicio") ?: "")
                val hf = CryptoUtils.decrypt(doc.getString("horaFim") ?: "")
                binding.tvDataHora.apply {
                    text = "Data: $dt | Hora: $hi - $hf"
                    setTextColor(Color.BLACK)
                }

                // ————— Convidados —————
                binding.containerConvidados.removeAllViews()
                val convidadosRaw = doc.get("convidados")
                Log.d(TAG, "convidadosRaw = $convidadosRaw")
                (convidadosRaw as? List<*>)?.forEach { item ->
                    when (item) {
                        is String -> {
                            val nome = CryptoUtils.decrypt(item)
                            binding.containerConvidados.addView(makeTextView("• $nome"))
                        }
                        is Map<*, *> -> {
                            val nomeEnc  = item["nome"]  as? String ?: ""
                            val emailEnc = item["email"] as? String ?: ""
                            val nome  = CryptoUtils.decrypt(nomeEnc)
                            val email = CryptoUtils.decrypt(emailEnc)
                            binding.containerConvidados
                                .addView(makeTextView("• $nome ($email)"))
                        }
                        else -> {
                            Log.w(TAG, "Convidado de tipo inesperado: ${item?.javaClass}")
                        }
                    }
                }

                // ————— Fornecedores —————
                binding.containerFornecedores.removeAllViews()
                val fornecedoresRaw = doc.get("fornecedores")
                Log.d(TAG, "fornecedoresRaw = $fornecedoresRaw")
                when (fornecedoresRaw) {
                    is List<*> -> fornecedoresRaw.forEach { item ->
                        when (item) {
                            is Map<*, *> -> {
                                val nome = item["nome"] as? String ?: "Nome desconhecido"
                                val localizacao = item["localizacao"] as? String ?: "Localização desconhecida"
                                val contacto = item["Contacto"] as? String ?: "Contacto desconhecido"
                                val preco = item["Preço"] as? String ?: "Preço desconhecido"
                                val tipoServico = item["Tipo_Serviço"] as? String ?: "Tipo de serviço desconhecido"

                                binding.containerFornecedores.addView(makeTextView("• $nome"))
                                binding.containerFornecedores.addView(makeTextView("  Localização: $localizacao"))
                                binding.containerFornecedores.addView(makeTextView("  Contacto: $contacto"))
                                binding.containerFornecedores.addView(makeTextView("  Preço: $preco"))
                                binding.containerFornecedores.addView(makeTextView("  Tipo de Serviço: $tipoServico"))
                            }
                            else -> Log.w(TAG, "Fornecedor de tipo inesperado: ${item?.javaClass}")
                        }
                    }
                    null -> Log.i(TAG, "Campo 'fornecedores' não existe ou é nulo")
                    else -> Log.w(TAG, "'fornecedores' não é uma lista: ${fornecedoresRaw.javaClass}")
                }
            }
            .addOnFailureListener { ex ->
                binding.tvTituloEvento.text = "Erro: ${ex.message}"
            }
    }

    private fun makeTextView(text: String): TextView =
        TextView(requireContext()).apply {
            this.text = text
            setPadding(16, 8, 16, 8)
            setTextColor(Color.BLACK)
        }
}
