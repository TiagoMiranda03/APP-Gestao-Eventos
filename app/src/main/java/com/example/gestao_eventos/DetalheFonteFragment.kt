package com.example.gestao_eventos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.gestao_eventos.databinding.FragmentDetalheFonteBinding
import com.google.firebase.firestore.FirebaseFirestore

class DetalheFonteFragment : Fragment() {

    private var _binding: FragmentDetalheFonteBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_ID = "id"

        fun newInstance(id: String): DetalheFonteFragment {
            val fragment = DetalheFonteFragment()
            val args = Bundle()
            args.putString(ARG_ID, id)
            fragment.arguments = args
            return fragment
        }
    }

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalheFonteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val idFonte = arguments?.getString(ARG_ID) ?: ""

        if (idFonte.isNotEmpty()) {
            db.collection("Fonte_Inspiracao").document(idFonte).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val titulo = document.getString("Titulo") ?: "Sem título"
                        binding.tvNomeEvento.text = titulo
                    } else {
                        binding.tvNomeEvento.text = "Fonte não encontrada"
                    }
                }
                .addOnFailureListener { exception ->
                    binding.tvNomeEvento.text = "Erro ao carregar fonte: ${exception.message}"
                }
        } else {
            binding.tvNomeEvento.text = "ID não encontrado"
        }

        if (idFonte.isNotEmpty())
        {
            db.collection("Fonte_Inspiracao").document(idFonte).get().addOnSuccessListener { document ->
                if (document.exists())
                {
                    val descricao = document.getString("Descricao") ?: "Sem descrição"
                    binding.etDescricao.setText(descricao)
                }else
                    binding.etDescricao.setText("Fonte não encontrada")
            }
                .addOnFailureListener { exception ->
                    binding.etDescricao.setText("Erro ao carregar fonte: ${exception.message}")
                }
        }

        if (idFonte.isNotEmpty())
        {
            db.collection("Fonte_Inspiracao").document(idFonte).get().addOnSuccessListener { document ->
                if (document.exists())
                {
                    val descricao = document.getString("Categoria") ?: "Sem Categoria"
                    binding.etCategoria.setText(descricao)
                }else
                    binding.etCategoria.setText("Fonte não encontrada")
            }
                .addOnFailureListener { exception ->
                    binding.etCategoria.setText("Erro ao carregar fonte: ${exception.message}")
                }
        }

        if (idFonte.isNotEmpty())
        {
            db.collection("Fonte_Inspiracao").document(idFonte).get().addOnSuccessListener { document ->
                if (document.exists())
                {
                    val descricao = document.getString("Link_Referencia") ?: "Sem descrição"
                    binding.etLinkReferencia.setText(descricao)
                }else
                    binding.etLinkReferencia.setText("Fonte não encontrada")
            }
                .addOnFailureListener { exception ->
                    binding.etLinkReferencia.setText("Erro ao carregar fonte: ${exception.message}")
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
