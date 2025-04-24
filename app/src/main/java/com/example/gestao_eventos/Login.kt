package com.example.gestao_eventos

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestao_eventos.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()


        var isPasswordVisible = false
        binding.ivTogglePassword.setOnClickListener{
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible)
            {
                binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }
            else
                binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            binding.etPassword.setSelection(binding.etPassword.text.length)
        }

        binding.btnLogin.setOnClickListener{
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty())
                fazerLogin(email,password)
            else
                Toast.makeText(this,"Preencha todos os campos!", Toast.LENGTH_SHORT).show()
        }


        binding.tvRegistar.setOnClickListener{
            val intent = Intent(this, Registo::class.java)
            startActivity(intent)
        }
    }

    private fun fazerLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Verifique o seu email antes de fazer login.", Toast.LENGTH_LONG).show()
                        auth.signOut()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Erro ao dar login!", Toast.LENGTH_SHORT).show()
            }
    }
}