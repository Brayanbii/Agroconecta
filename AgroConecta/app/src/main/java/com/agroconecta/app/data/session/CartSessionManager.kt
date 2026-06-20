package com.agroconecta.app.data.session

import android.content.Context
import android.content.SharedPreferences

class CartSessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("AgroConectaCartSession", Context.MODE_PRIVATE)

    private fun keyForUser(userId: Long): String = "KEY_CART_COUNT_$userId"

    fun saveCartCount(userId: Long, count: Int) {
        prefs.edit().putInt(keyForUser(userId), count).apply()
    }

    fun getCartCount(userId: Long): Int {
        return prefs.getInt(keyForUser(userId), 0)
    }

    fun clearCartCount(userId: Long) {
        prefs.edit().remove(keyForUser(userId)).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
