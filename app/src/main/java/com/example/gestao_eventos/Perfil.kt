package com.example.gestao_eventos

import android.content.Intent
import android.os.Bundle
<<<<<<< HEAD
import android.util.Log
import android.view.View
import android.widget.Toast
=======
import android.view.View
>>>>>>> 7833391176b85b6e62bc3e66eb3ecce23dfe1ad9
import androidx.appcompat.app.AppCompatActivity
import com.example.gestao_eventos.databinding.ActivityPerfilBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.seuapp.ui.fragments.FAQFragment
<<<<<<< HEAD
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
=======
>>>>>>> 7833391176b85b6e62bc3e66eb3ecce23dfe1ad9

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
<<<<<<< HEAD
        
        binding.ivteste.setOnClickListener{

            //API: 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
            //eventosgestao56@gmail.com

            enviarEmail()
        }
=======

>>>>>>> 7833391176b85b6e62bc3e66eb3ecce23dfe1ad9


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

<<<<<<< HEAD
    private fun enviarEmail() {
        val client = OkHttpClient()

        val json = JSONObject().apply {
            put("personalizations", JSONArray().put(JSONObject().apply {
                put("to", JSONArray().put(JSONObject().put("email", "gamesrd164@gmail.com")))
            }))
            put("from", JSONObject().put("email", "eventosgestao56@gmail.com"))
            put("subject", "Assunto do Email")
            put("content", JSONArray().put(JSONObject().apply {
                put("type", "text/html")
                put("value", """
            <html>
                <body>
                    <p>Olá,</p>
                    <p>Obrigado por se inscrever no nosso evento!</p>
                    <p>Se precisar de mais informações, entre em contato.</p>
                    <br>
                    <p>Atenciosamente,</p>
                    <p><strong>Equipe Gestão Eventos</strong></p>
                </body>
            </html>
        """.trimIndent())
            }))
        }

        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url("https://api.sendgrid.com/v3/mail/send")
            .addHeader("Authorization", "Bearer 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")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("SendGrid", "Erro ao enviar email: ${e.message}")
                runOnUiThread {
                    Toast.makeText(applicationContext, "Erro ao enviar email", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                Log.d("SendGrid", "Código de resposta: ${response.code}")
                Log.d("SendGrid", "Resposta: ${response.body?.string()}") // Mostra a resposta completa
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(applicationContext, "Email enviado com sucesso!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(applicationContext, "Erro: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }



=======
>>>>>>> 7833391176b85b6e62bc3e66eb3ecce23dfe1ad9
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