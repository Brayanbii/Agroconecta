package com.agroconecta.app.data.session

import android.content.Context
import android.content.SharedPreferences
import com.agroconecta.app.data.model.UsuarioInfo

class SessionManager(context: Context) {

    // Almacen de datos privado del celular
    private val prefs: SharedPreferences = context.getSharedPreferences("AgroConectaSession", Context.MODE_PRIVATE)

    // Persistir cookie de sesion (JSESSIONID)
    fun saveSessionCookie(cookie: String) {
        prefs.edit().putString("KEY_JSESSIONID", cookie).apply()
    }

    fun getSessionCookie(): String? = prefs.getString("KEY_JSESSIONID", null)

    // Guarda los datos básicos del usuario una vez que inicia sesión con éxito
    fun saveUser(user: UsuarioInfo) {
        prefs.edit().apply {
            putLong("KEY_ID", user.id)
            putString("KEY_USERNAME", user.userName)
            putString("KEY_EMAIL", user.email)
            putString("KEY_ROL", user.rol)
            apply()
        }
    }

    // Recupera la sesión si existe
    fun getUser(): UsuarioInfo? {
        val id = prefs.getLong("KEY_ID", -1L)
        val userName = prefs.getString("KEY_USERNAME", null)
        val email = prefs.getString("KEY_EMAIL", null)
        val rol = prefs.getString("KEY_ROL", null)

        // Si falta algún dato vital, no hay sesión activa
        if (id == -1L || userName == null || email == null || rol == null) {
            return null
        }
        return UsuarioInfo(id, userName, email, rol)
    }

    // Borra la sesión cuando el usuario presiona "Cerrar Sesión"
    fun clearSession() {
        prefs.edit().clear().apply()
    }

    // Guardar email para autocompletar en proximo login
    fun saveLastEmail(email: String) {
        prefs.edit().putString("KEY_LAST_EMAIL", email).apply()
    }

    fun getLastEmail(): String? = prefs.getString("KEY_LAST_EMAIL", null)

    // Guardar preferencia "Recordarme"
    fun saveRememberMe(remember: Boolean) {
        prefs.edit().putBoolean("KEY_REMEMBER_ME", remember).apply()
    }

    fun isRememberMe(): Boolean = prefs.getBoolean("KEY_REMEMBER_ME", false)

    // Guardar ultima contrasena (solo si recordarme esta activo)
    fun saveLastPassword(password: String) {
        prefs.edit().putString("KEY_LAST_PASSWORD", password).apply()
    }

    fun getLastPassword(): String? = prefs.getString("KEY_LAST_PASSWORD", null)
}