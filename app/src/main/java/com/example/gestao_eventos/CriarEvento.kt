package com.example.gestao_eventos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestao_eventos.databinding.ActivityCriarEventoBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CriarEvento : AppCompatActivity() {
    private lateinit var binding: ActivityCriarEventoBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityCriarEventoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btProximoPasso.setOnClickListener{


            findViewById<ScrollView>(R.id.scCriarEvento).visibility = View.GONE
            findViewById<FrameLayout>(R.id.fragmentContainer).visibility = View.VISIBLE


            val titulo = binding.etTitulo.text.toString()
            val descricao = binding.etDescricao.text.toString()
            val tipoEvento = binding.etTipoEvento.text.toString()
            val dataInicio = binding.etDataInicio.text.toString()
            val dataFim = binding.etDataFim.text.toString()
            val horaInicio = binding.etHoraInicio.text.toString()
            val horaFim = binding.etHoraFim.text.toString()
            val localizacao = binding.etLocalizacao.text.toString()

            if (titulo.isEmpty() || descricao.isEmpty() || tipoEvento.isEmpty() || dataInicio.isEmpty() || dataFim.isEmpty() || horaInicio.isEmpty() || horaFim.isEmpty() || localizacao.isEmpty())
            {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val userId = auth.currentUser?.uid
            if (userId == null){
                Toast.makeText(this,"Utilizador não autenticado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val evento = hashMapOf(
                "titulo" to titulo,
                "descricao" to descricao,
                "tipoEvento" to tipoEvento,
                "dataInicio" to dataInicio,
                "dataFim" to dataFim,
                "horaInicio" to horaInicio,
                "horaFim" to horaFim,
                "localizacao" to localizacao,
                "userId" to userId
            )

            db.collection("eventos")
                .add(evento)
                .addOnSuccessListener { documentReference ->
                    val eventoId = documentReference.id
                    Toast.makeText(this, "Evento criado com sucesso!", Toast.LENGTH_SHORT).show()


                    val fragment = Selecionar_Convidados_Fragment.newInstance()
                    val bundle = Bundle()
                    bundle.putString("eventoId", eventoId)
                    fragment.arguments = bundle

                    val transaction = supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragmentContainer, fragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao criar evento: ${e.message}", Toast.LENGTH_SHORT).show()
                }


        }



        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.newevent

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    // Navegar para o HomeFragment ou outra Activity
                    //startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.event -> {
<<<<<<< HEAD
                    startActivity(Intent(this, Eventos::class.java))
                    overridePendingTransition(0, 0)
=======
>>>>>>> 7833391176b85b6e62bc3e66eb3ecce23dfe1ad9
                    true
                }
                R.id.newevent -> {
                    startActivity(Intent(this, CriarEvento::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.Inspiration -> {
                    startActivity(Intent(this, Fontes_Inspiracao::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.profile -> {
                    startActivity(Intent(this, Perfil::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }


    }
}