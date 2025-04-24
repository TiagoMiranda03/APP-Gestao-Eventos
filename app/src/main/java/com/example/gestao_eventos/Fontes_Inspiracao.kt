package com.example.gestao_eventos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestao_eventos.databinding.ActivityFontesInspiracaoBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class Fontes_Inspiracao : AppCompatActivity() {

    private lateinit var binding: ActivityFontesInspiracaoBinding
    private lateinit var listView: ListView
    private lateinit var db: FirebaseFirestore
    private val fontesList = mutableListOf<Pair<String,String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFontesInspiracaoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        carregarFontesInspiracao()

        binding.eventList.setOnItemClickListener { _, _, position, _ ->
            val fonteSelecionada = fontesList[position]
            val idFonte = fonteSelecionada.first

            binding.container.visibility = View.VISIBLE

            val fragment = DetalheFonteFragment.newInstance(idFonte)
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit()
        }


        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.Inspiration

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

    private fun carregarFontesInspiracao(){

        db.collection("Fonte_Inspiracao").get().addOnSuccessListener { documents ->

            fontesList.clear()
            for (document in documents)
            {
                val id = document.id
                val titulo = document.getString("Titulo") ?: "Sem título"
                fontesList.add(Pair(id,titulo))
            }
            val titulos = fontesList.map{ it.second }
            val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, titulos)
            binding.eventList.adapter = arrayAdapter
        }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao carregar fontes: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

}