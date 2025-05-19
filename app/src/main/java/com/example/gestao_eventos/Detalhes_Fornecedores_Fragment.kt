package com.example.gestao_eventos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.firebase.firestore.FirebaseFirestore

class Detalhes_Fornecedores_Fragment : Fragment() {
    private var fornecedorId: String? = null
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            fornecedorId = it.getString("fornecedorId")
        }
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalhes__fornecedores_, container, false)

        view.findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            voltarParaLista()
        }

        carregarDetalhesFornecedor(view)
        return view
    }

    private fun voltarParaLista() {
        parentFragmentManager.popBackStack("DETALHES_FORNECEDOR", FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }


    private fun carregarDetalhesFornecedor(view: View) {
        if (fornecedorId.isNullOrEmpty()) {
            Toast.makeText(context, "Fornecedor não encontrado!", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("Fornecedores").document(fornecedorId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    view.findViewById<TextView>(R.id.tvNomeFornecedor).text = document.getString("Nome")
                    view.findViewById<TextView>(R.id.tvCategoriaFornecedor).text = "Categoria: " + document.getString("Tipo_Serviço")
                    view.findViewById<TextView>(R.id.tvLocalizacaoFornecedor).text = "Localização: " + document.getString("Localização")
                    view.findViewById<TextView>(R.id.tvContactoFornecedor).text = "Contacto: " + document.getString("Contacto")
                    view.findViewById<TextView>(R.id.tvPrecoFornecedor).text = "Preço: " + document.getString("Preço") + "€"
                } else {
                    Toast.makeText(context, "Fornecedor não encontrado!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao buscar detalhes do fornecedor.", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        fun newInstance(fornecedorId: String) =
            Detalhes_Fornecedores_Fragment().apply {
                arguments = Bundle().apply {
                    putString("fornecedorId", fornecedorId)
                }
            }
    }
}