package com.example.gestao_eventos

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gestao_eventos.databinding.FragmentEventosDetalheBinding
import com.google.firebase.firestore.FirebaseFirestore

class EventosDetalhe : Fragment() {

    private lateinit var binding: FragmentEventosDetalheBinding
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "EventosDetalhe"
    private var eventoId: String? = null

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
        eventoId = arguments?.getString(ARG_EVENTO_ID)

        carregarDetalhesEvento()
        configurarBotaoApagarEvento()
    }

    private fun carregarDetalhesEvento() {
        if (eventoId == null) {
            binding.tvTituloEvento.text = "Evento não encontrado"
            return
        }

        db.collection("eventos").document(eventoId!!)
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

                // Convidados
                binding.containerConvidados.removeAllViews()
                val convidadosRaw = doc.get("convidados") as? List<Map<String, String>>
                convidadosRaw?.forEach {
                    val nome = CryptoUtils.decrypt(it["nome"] ?: "")
                    val email = CryptoUtils.decrypt(it["email"] ?: "")
                    binding.containerConvidados.addView(makeTextView("• $nome ($email)"))
                }

                // Fornecedores
                binding.containerFornecedores.removeAllViews()
                val fornecedoresRaw = doc.get("fornecedores") as? List<Map<String, String>>
                fornecedoresRaw?.forEach {
                    val nome = it["nome"] ?: "Nome desconhecido"
                    val localizacao = it["localizacao"] ?: "Localização desconhecida"
                    binding.containerFornecedores.addView(makeTextView("• $nome - $localizacao"))
                }
            }
            .addOnFailureListener { ex ->
                binding.tvTituloEvento.text = "Erro: ${ex.message}"
            }
    }

    private fun configurarBotaoApagarEvento() {
        binding.btnApagarEvento.setOnClickListener {
            if (eventoId == null) {
                Toast.makeText(requireContext(), "ID do evento não encontrado.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            AlertDialog.Builder(requireContext())
                .setTitle("Apagar Evento")
                .setMessage("Tens a certeza que queres apagar este evento? Esta ação é irreversível.")
                .setPositiveButton("Sim") { _, _ ->
                    apagarEvento()
                }
                .setNegativeButton("Não", null)
                .show()
        }
    }

    private fun apagarEvento() {
        db.collection("eventos").document(eventoId!!)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Evento apagado com sucesso.", Toast.LENGTH_SHORT).show()
                // Navegar para a Lista de Eventos Activity
                val intent = Intent(requireContext(), Eventos::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                requireActivity().finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Erro ao apagar o evento: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun makeTextView(text: String): TextView =
        TextView(requireContext()).apply {
            this.text = text
            setPadding(16, 8, 16, 8)
            setTextColor(Color.BLACK)
        }
}
