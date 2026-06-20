package com.agroconectago.app.data.session

import android.content.Context
import com.agroconectago.app.data.model.DeliveryUsuarioInfo

class DeliverySessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("AgroConectaGoSession", Context.MODE_PRIVATE)

    fun saveUser(user: DeliveryUsuarioInfo) {
        prefs.edit()
            .putLong("USER_ID", user.id)
            .putString("USER_NAME", user.userName)
            .putString("USER_EMAIL", user.email)
            .putString("USER_ROL", user.rol)
            .apply()
    }

    fun getUser(): DeliveryUsuarioInfo? {
        val id = prefs.getLong("USER_ID", -1)
        if (id == -1L) return null
        return DeliveryUsuarioInfo(
            id = id,
            userName = prefs.getString("USER_NAME", "") ?: "",
            email = prefs.getString("USER_EMAIL", "") ?: "",
            rol = prefs.getString("USER_ROL", "") ?: ""
        )
    }

    fun isLoggedIn(): Boolean = getUser() != null
    fun isRepartidor(): Boolean = getUser()?.rol == "REPARTIDOR"

    fun saveLastEmail(email: String) {
        prefs.edit().putString("LAST_EMAIL", email).apply()
    }

    fun getLastEmail(): String? = prefs.getString("LAST_EMAIL", null)

    fun saveLastPassword(password: String) {
        prefs.edit().putString("LAST_PASSWORD", password).apply()
    }

    fun getLastPassword(): String? = prefs.getString("LAST_PASSWORD", null)

    fun isRememberMe(): Boolean = prefs.getBoolean("REMEMBER_ME", false)

    fun saveRememberMe(remember: Boolean) {
        prefs.edit().putBoolean("REMEMBER_ME", remember).apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
