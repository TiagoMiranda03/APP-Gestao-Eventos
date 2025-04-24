package com.example.gestao_eventos

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gestao_eventos.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        CarregarNome()

        binding.IvNewEvent.setOnClickListener{
            val intent = Intent(this, CriarEvento::class.java)
            startActivity(intent)
        }
        binding.IvFonte.setOnClickListener{
            val intent = Intent(this, Fontes_Inspiracao::class.java)
            startActivity(intent)
        }
        binding.IvEvents.setOnClickListener{
            val intent = Intent(this, Eventos::class.java)
            startActivity(intent)
        }


        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.home

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.event -> {

                    startActivity(Intent(this, Eventos::class.java))
                    overridePendingTransition(0, 0)
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

    private fun CarregarNome()
    {
        val user = auth.currentUser
        if (user != null)
        {
            val userId= user.uid

            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                if(document.exists())
                {
                    val nomeEncriptado = document.getString("nome") ?: "Utilizador"
                    val nomeDesencriptado = CryptoUtils.decrypt(nomeEncriptado)

                    binding.tvWelcome.text = "Olá," + nomeDesencriptado + "\uD83D\uDC4B"
                }
            }.addOnFailureListener {
                binding.tvWelcome.text = "Erro ao carregar"
            }
        }
        else{
            binding.tvWelcome.text = "Não logado"
        }
    }
}