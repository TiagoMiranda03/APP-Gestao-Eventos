package com.example.gestao_eventos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestao_eventos.databinding.ActivityEventosBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Eventos : AppCompatActivity() {

    private lateinit var binding: ActivityEventosBinding
    private lateinit var listView: ListView
    private lateinit var db: FirebaseFirestore
    private val eventosList = mutableListOf<Pair<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        carregarEventosUsuarioLogado()

        binding.eventList.setOnItemClickListener { _, _, position, _ ->
            val eventoSelecionado = eventosList[position]
            val idEvento = eventoSelecionado.first

            binding.container.visibility = View.VISIBLE

           val fragment = EventosDetalhe.newInstance(idEvento)
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit()
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.event

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
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

    private fun carregarEventosUsuarioLogado() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            db.collection("eventos")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    eventosList.clear()
                    for (document in documents) {
                        val id = document.id
                        val titulo = document.getString("titulo") ?: "Sem título"
                        eventosList.add(Pair(id, titulo))
                    }

                    if (eventosList.isNotEmpty()) {
                        val titulos = eventosList.map { it.second }
                        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, titulos)
                        binding.eventList.adapter = arrayAdapter

                        arrayAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this, "Nenhum evento encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Erro ao carregar eventos: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
        }
    }


}
