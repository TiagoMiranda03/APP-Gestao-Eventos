package com.example.gestao_eventos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gestao_eventos.databinding.FragmentConvidadoNovoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ConvidadoNovo_Fragment : Fragment() {

    private var _binding: FragmentConvidadoNovoBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    companion object{
        fun newInstance(): ConvidadoNovo_Fragment{
            return ConvidadoNovo_Fragment()
        }
    }


    private var eventoId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConvidadoNovoBinding.inflate(inflater, container, false)
        eventoId = arguments?.getString("eventoId")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth= FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnGuardarConvidado.setOnClickListener{
            novoConvidado()
        }
        binding.btnCancelarConvidado.setOnClickListener{
            val fragment = Selecionar_Convidados_Fragment.newInstance()

            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    private fun novoConvidado(){
        val nome = binding.etNomeConvidado.text.toString()
        val email = binding.etEmailConvidado.text.toString()

        val user = auth.currentUser
        if (user != null){
            val userId = user.uid

            val nomeEncripted = CryptoUtils.encrypt(nome)
            val emailEncripted = CryptoUtils.encrypt(email)

            val convidado = hashMapOf(
                "userId" to userId,
                "NomeConvidado" to nomeEncripted,
                "EmailConvidado" to emailEncripted
            )

            db.collection("convidados")
                .add(convidado)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Convidado adicionado!!", Toast.LENGTH_SHORT).show()

                    val fragment = Selecionar_Convidados_Fragment.newInstance()
                    val bundle = Bundle()
                    bundle.putString("eventoId", eventoId)
                    fragment.arguments = bundle

                    val transaction = parentFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragmentContainer, fragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Erro ao adicionar convidado!!", Toast.LENGTH_SHORT).show()
                }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}