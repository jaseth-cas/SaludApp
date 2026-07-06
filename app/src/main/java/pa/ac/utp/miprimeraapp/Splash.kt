package pa.ac.utp.miprimeraapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Splash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val mainView = findViewById<android.view.View>(R.id.ScrollView)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            checkRegistration()
        }, 2000)
    }

    private fun checkRegistration() {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val isRegistered = sharedPref.getBoolean("isRegistered", false)
        val biometricEnabled = sharedPref.getBoolean("biometricEnabled", false)

        if (isRegistered) {
            if (biometricEnabled) {
                showBiometricPrompt()
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        } else {
            startActivity(Intent(this, RegistroUsuario::class.java))
            finish()
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Error: $errString", Toast.LENGTH_SHORT).show()
                    // Si falla o se cancela, cerramos la app por seguridad
                    finish()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    startActivity(Intent(this@Splash, MainActivity::class.java))
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Autenticación fallida", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceso Seguro - SaludApp")
            .setSubtitle("Use su huella o rostro para entrar")
            .setNegativeButtonText("Salir")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}