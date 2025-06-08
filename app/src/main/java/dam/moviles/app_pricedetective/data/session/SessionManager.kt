package dam.moviles.app_pricedetective.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import dam.moviles.app_pricedetective.data.model.Usuario

class SessionManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "PriceDetectiveSession"
        private const val KEY_USER = "user"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    }

    fun saveUser(usuario: Usuario) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_USER, gson.toJson(usuario))
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    fun getUser(): Usuario? {
        val userJson = sharedPreferences.getString(KEY_USER, null)
        return if (userJson != null) {
            gson.fromJson(userJson, Usuario::class.java)
        } else null
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearSession() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
} 