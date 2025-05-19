package com.example.gestao_eventos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gestao_eventos.databinding.FragmentSelecionarConvidadosBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Selecionar_Convidados_Fragment : Fragment() {

    private var _binding: FragmentSelecionarConvidadosBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private var eventoId: String? = null

    companion object {
        fun newInstance(): Selecionar_Convidados_Fragment {
            return Selecionar_Convidados_Fragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSelecionarConvidadosBinding.inflate(inflater, container, false)
        db = FirebaseFirestore.getInstance()

        eventoId = arguments?.getString("eventoId")

        if (eventoId.isNullOrEmpty()) {
            Toast.makeText(context, "Erro: ID do evento não encontrado.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack() // Volta ao fragmento anterior se o ID for inválido
            return binding.root
        }

        carregarConvidados()

        binding.btnAdicionarContato.setOnClickListener{
            val fragment = ConvidadoNovo_Fragment.newInstance()
            val bundle = Bundle()
            bundle.putString("eventoId", eventoId)
            fragment.arguments = bundle

            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }


        binding.btProximoPasso.setOnClickListener {
            adicionarConvidadosNoEvento(binding.containerConvidados)
        }

        return binding.root
    }

    private fun carregarConvidados() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        if (userId == null) {
            Toast.makeText(context, "Erro: Utilizador não autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("convidados")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val listaConvidados = mutableListOf<Pair<String, String>>()
                for (doc in result) {
                    val nomeEncriptado = doc.getString("NomeConvidado") ?: ""
                    val emailEncriptado = doc.getString("EmailConvidado") ?: ""

                    val nome = CryptoUtils.decrypt(nomeEncriptado)
                    val email = CryptoUtils.decrypt(emailEncriptado)
                    listaConvidados.add(Pair(nome, email))
                }
                adicionarConvidados(binding.containerConvidados, listaConvidados)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Erro ao carregar convidados: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun adicionarConvidados(container: LinearLayout, convidados: List<Pair<String, String>>) {
        container.removeAllViews()
        for ((nome, email) in convidados) {
            val convidadoView = layoutInflater.inflate(R.layout.item_convidado, container, false)

            val txtNome = convidadoView.findViewById<TextView>(R.id.txtNome)
            val txtEmail = convidadoView.findViewById<TextView>(R.id.txtEmail)
            val checkBox = convidadoView.findViewById<CheckBox>(R.id.checkBoxSelecionado)

            txtNome.text = nome
            txtEmail.text = email

            container.addView(convidadoView)
        }
    }

    private fun adicionarConvidadosNoEvento(container: LinearLayout) {
        if (eventoId.isNullOrEmpty()) {
            Toast.makeText(context, "Erro: ID do evento não encontrado.", Toast.LENGTH_SHORT).show()
            return
        }

        val convidadosSelecionados = mutableListOf<HashMap<String, String>>()

        for (i in 0 until container.childCount) {
            val convidadoView = container.getChildAt(i)
            val checkBox = convidadoView.findViewById<CheckBox>(R.id.checkBoxSelecionado)

            if (checkBox.isChecked) {
                val nome = convidadoView.findViewById<TextView>(R.id.txtNome).text.toString()
                val email = convidadoView.findViewById<TextView>(R.id.txtEmail).text.toString()

                val convidado = hashMapOf(
                    "nome" to CryptoUtils.encrypt(nome),
                    "email" to CryptoUtils.encrypt(email)
                )
                convidadosSelecionados.add(convidado)
            }
        }

        if (convidadosSelecionados.isNotEmpty()) {
            db.collection("eventos").document(eventoId!!)
                .update("convidados", convidadosSelecionados)
                .addOnSuccessListener {
                    Toast.makeText(context, "Convidados adicionados ao evento!", Toast.LENGTH_SHORT).show()
                    navegarParaSelecionarFornecedores()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Erro ao adicionar convidados: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Selecione ao menos um convidado.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navegarParaSelecionarFornecedores() {
        val fragment = Selecionar_Fornecedor_Fragment.newInstance()
        val bundle = Bundle()
        bundle.putString("eventoId", eventoId)
        fragment.arguments = bundle

        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
