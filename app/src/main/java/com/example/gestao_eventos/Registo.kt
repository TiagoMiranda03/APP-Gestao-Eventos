package com.example.gestao_eventos

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gestao_eventos.databinding.ActivityRegistoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Registo : AppCompatActivity() {

    private lateinit var binding: ActivityRegistoBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.ivDataNascimentoIcon.setOnClickListener{
            abrirCalendario()
        }

        var isPasswordVisible = false

        binding.ivConfirmTogglePassword.setOnClickListener{
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible)
            {
                binding.etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }
            else
                binding.etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text.length)
        }

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

        binding.btnRegisto.setOnClickListener{

            val nome = binding.etNome.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirm_password = binding.etConfirmPassword.text.toString()
            val dataNascimento = binding.etDataNascimento.text.toString()

            if(nome.isEmpty() || email.isEmpty() || password.isEmpty() || confirm_password.isEmpty() || dataNascimento.isEmpty())
            {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm_password)
            {
                Toast.makeText(this, "Passwords não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener


            }

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { verifyTask ->
                            if (verifyTask.isSuccessful) {
                                val userId = user.uid

                                //Encriptação dos dados
                                val encryptedNome = CryptoUtils.encrypt(nome)
                                val encryptedEmail = CryptoUtils.encrypt(email)
                                val encryptedDob   = CryptoUtils.encrypt(dataNascimento)
                                val encryptedRole  = CryptoUtils.encrypt("utilizador")

                                val userData = hashMapOf(
                                    "userId" to userId,
                                    "nome" to encryptedNome,
                                    "email" to encryptedEmail,
                                    "dataNascimento" to encryptedDob,
                                    "role" to encryptedRole
                                )

                                db.collection("users").document(userId)
                                    .set(userData)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Verifique o seu email para ativar a conta.", Toast.LENGTH_LONG).show()
                                        auth.signOut() // Desconecta o utilizador até verificar o email
                                        val intent = Intent(this, Login::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Erro ao guardar utilizador", Toast.LENGTH_SHORT).show()
                                        Log.w("Firestore", "Erro ao guardar utilizador", e)
                                    }
                            } else {
                                Toast.makeText(this, "Erro ao enviar email de verificação", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Erro ao registar utilizador", Toast.LENGTH_SHORT).show()
                    Log.e("Auth", "Erro ao registar: ${task.exception?.message}")
                }
            }

        }
    }

    private fun abrirCalendario() {
        val ano = calendar.get(Calendar.YEAR)
        val mes = calendar.get(Calendar.MONTH)
        val dia = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, anoSelecionado, mesSelecionado, diaSelecionado ->

            calendar.set(Calendar.YEAR, anoSelecionado)
            calendar.set(Calendar.MONTH, mesSelecionado)
            calendar.set(Calendar.DAY_OF_MONTH, diaSelecionado)

            val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.etDataNascimento.setText(formatoData.format(calendar.time))
        }, ano, mes, dia)

        datePickerDialog.show()
    }
}