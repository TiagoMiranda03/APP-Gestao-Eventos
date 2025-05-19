package com.example.gestao_eventos

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestao_eventos.databinding.ActivityCriarEventoBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class CriarEvento : AppCompatActivity() {
    private lateinit var binding: ActivityCriarEventoBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriarEventoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Configurar o DatePicker para a Data de Início e Fim
        binding.etDataInicio.setOnClickListener { showDatePicker(binding.etDataInicio) }
        binding.etDataFim.setOnClickListener { showDatePicker(binding.etDataFim) }

        // Configurar o TimePicker para a Hora de Início e Fim
        binding.etHoraInicio.setOnClickListener { showTimePicker(binding.etHoraInicio) }
        binding.etHoraFim.setOnClickListener { showTimePicker(binding.etHoraFim) }

        binding.btProximoPasso.setOnClickListener {
            criarEvento()
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


    // Função para exibir o DatePicker
    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val data = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
            editText.setText(data)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        datePickerDialog.show()
    }

    // Função para exibir o TimePicker
    private fun showTimePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
            val hora = String.format("%02d:%02d", hourOfDay, minute)
            editText.setText(hora)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

        timePickerDialog.show()
    }

    private fun criarEvento() {
        val titulo = binding.etTitulo.text.toString()
        val descricao = binding.etDescricao.text.toString()
        val tipoEvento = binding.etTipoEvento.text.toString()
        val dataInicio = binding.etDataInicio.text.toString()
        val dataFim = binding.etDataFim.text.toString()
        val horaInicio = binding.etHoraInicio.text.toString()
        val horaFim = binding.etHoraFim.text.toString()
        val localizacao = binding.etLocalizacao.text.toString()

        if (titulo.isEmpty() || descricao.isEmpty() || tipoEvento.isEmpty() ||
            dataInicio.isEmpty() || dataFim.isEmpty() ||
            horaInicio.isEmpty() || horaFim.isEmpty() || localizacao.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this,"Utilizador não autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val evento = hashMapOf(
            "titulo" to CryptoUtils.encrypt(titulo),
            "descricao" to CryptoUtils.encrypt(descricao),
            "tipoEvento" to CryptoUtils.encrypt(tipoEvento),
            "dataInicio" to CryptoUtils.encrypt(dataInicio),
            "dataFim" to CryptoUtils.encrypt(dataFim),
            "horaInicio" to CryptoUtils.encrypt(horaInicio),
            "horaFim" to CryptoUtils.encrypt(horaFim),
            "localizacao" to CryptoUtils.encrypt(localizacao),
            "userId" to userId
        )

        db.collection("eventos")
            .add(evento)
            .addOnSuccessListener { documentReference ->
                val eventoId = documentReference.id
                Toast.makeText(this, "Evento criado com sucesso!", Toast.LENGTH_SHORT).show()
                abrirSelecionarConvidados(eventoId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao criar evento: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun abrirSelecionarConvidados(eventoId: String) {
        findViewById<ScrollView>(R.id.scCriarEvento).visibility = View.GONE
        findViewById<FrameLayout>(R.id.fragmentContainer).visibility = View.VISIBLE

        val fragment = Selecionar_Convidados_Fragment.newInstance()
        val bundle = Bundle()
        bundle.putString("eventoId", eventoId)
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}