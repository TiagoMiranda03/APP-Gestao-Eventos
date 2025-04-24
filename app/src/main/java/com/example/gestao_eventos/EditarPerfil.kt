package com.example.gestao_eventos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gestao_eventos.databinding.FragmentEditarPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditarPerfil : Fragment() {

    private var _binding: FragmentEditarPerfilBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    companion object {
        fun newInstance(): EditarPerfil {
            return EditarPerfil()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditarPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        carregarDadosPerfil()

        binding.btnGuardar.setOnClickListener{
            guardarAlteracoes()
        }
    }

    private fun carregarDadosPerfil(){

        val user = auth.currentUser
        if (user != null)
        {
            val userId = user.uid

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists())
                    {
                        val nomeEncriptado = document.getString("nome") ?: "Utilizador"
                        val emailEncriptado = document.getString("email") ?: "email@dominio.com"
                        val dnEncriptado = document.getString("dataNascimento") ?: ""

                        val nomeDesencriptado = CryptoUtils.decrypt(nomeEncriptado)
                        val emailDesencriptado = CryptoUtils.decrypt(emailEncriptado)
                        val dnDesencriptado = CryptoUtils.decrypt(dnEncriptado)



                        binding.etNome.setText(nomeDesencriptado)
                        binding.etEmail.setText(emailDesencriptado)
                        binding.etDataNascimento.setText(dnDesencriptado)
                    }
                }
        }

    }

    private fun guardarAlteracoes()
    {
        val nome = binding.etNome.text.toString()
        val email = binding.etEmail.text.toString()
        val dn = binding.etDataNascimento.text.toString()

        val user = auth.currentUser
        if (user != null)
        {
            val userId = user.uid
            val NomeEncriptado = CryptoUtils.encrypt(nome)
            val EmailEncriptado = CryptoUtils.encrypt(email)
            val DNEncriptado = CryptoUtils.encrypt(dn)

            val userData = mapOf(
                "nome" to NomeEncriptado,
                "email" to EmailEncriptado,
                "dataNascimento" to DNEncriptado
            )

            db.collection("users").document(userId).update(userData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Dados alterados com sucesso", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Erro ao alterar os dados", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
