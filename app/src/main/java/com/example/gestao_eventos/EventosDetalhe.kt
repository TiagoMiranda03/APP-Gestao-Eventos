package com.example.gestao_eventos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gestao_eventos.databinding.FragmentEventosDetalheBinding
import com.google.firebase.firestore.FirebaseFirestore

class EventosDetalhe : Fragment() {

    private lateinit var binding: FragmentEventosDetalheBinding
    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val ARG_EVENTO_ID = "evento_id"

        fun newInstance(eventoId: String): EventosDetalhe {
            val fragment = EventosDetalhe()
            val args = Bundle()
            args.putString(ARG_EVENTO_ID, eventoId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventosDetalheBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pegar o ID do evento passado pelo Activity
        val eventoId = arguments?.getString(ARG_EVENTO_ID)

        if (eventoId != null) {
            // Buscar os detalhes do evento no Firestore
            db.collection("eventos")
                .document(eventoId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val tituloEvento = document.getString("titulo") ?:"Sem nome"
                        val tipoEvento = document.getString("tipoEvento") ?:"Sem tipo"
                        val localizacaoEvento = document.getString("localizacao") ?:"Sem localizacao"
                        val horaInicioEvento = document.getString("horaInicio") ?:"Sem horaInicio"
                        val horaFimEvento = document.getString("horaFim") ?:"Sem horaFim"
                        val descricao = document.getString("descricao") ?:"Sem descricao"
                        val dataInicio = document.getString("dataInicio") ?:"Sem dataInicio"
                        val dataFim = document.getString("dataFim") ?:"Sem dataFim"

                        binding.tvTituloEvento.text = tituloEvento
                        binding.etTipoEvento.setText(tipoEvento)
                        binding.etDescricao.setText(descricao)
                        binding.etlocal.setText(localizacaoEvento)
                        binding.etHInicio.setText(horaInicioEvento)
                        binding.etHFim.setText(horaFimEvento)
                        binding.etDInicio.setText(dataInicio)
                        binding.etDFim.setText(dataFim)
                    } else {
                        binding.tvTituloEvento.text = "Evento não encontrado"
                    }
                }
                .addOnFailureListener { exception ->
                    binding.tvTituloEvento.text = "Erro ao carregar evento: ${exception.message}"
                }
        }
    }
}
