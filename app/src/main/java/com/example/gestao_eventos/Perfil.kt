package com.example.gestao_eventos

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.gestao_eventos.databinding.ActivityPerfilBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.seuapp.ui.fragments.FAQFragment

class Perfil : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        carregarNomePerfil()

        binding.ivLogout.setOnClickListener{
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        binding.ivEditProfile.setOnClickListener{

            binding.llProfileOptions.visibility = View.GONE
            binding.container.visibility = View.VISIBLE

            val fragment = EditarPerfil.newInstance()
            supportFragmentManager.beginTransaction().replace(R.id.container, fragment).addToBackStack(null).commit()
        }

        binding.ivFAQ.setOnClickListener{

            binding.llProfileOptions.visibility = View.GONE
            binding.container.visibility = View.VISIBLE

            val fragment = FAQFragment.newInstance()
            supportFragmentManager.beginTransaction().replace(R.id.container, fragment).addToBackStack(null).commit()
        }



        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.profile

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    // Navegar para o HomeFragment ou outra Activity
                    //startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.event -> {
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

    private fun carregarNomePerfil(){
        val user = auth.currentUser
        if (user!= null){
            val userId = user.uid

            db.collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document.exists())
                {
                    val nome = document.getString("nome") ?:"Utilizador"
                    binding.tvProfileName.text = nome
                }
            }
                .addOnFailureListener{
                    binding.tvProfileName.text = "Erro ao Carregar"
                }
        }
        else{
            binding.tvProfileName.text="Não logado"
        }
    }
}