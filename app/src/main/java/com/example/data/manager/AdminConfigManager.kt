package com.example.data.manager

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor centralizado de Administradores del Sistema Actitud Fuerte.
 * - Administrador Maestro Inmutable: Alex Gómez (alexgcuicas@gmail.com)
 * - Permite a Alex Gómez agregar y revocar administradores adicionales por correo electrónico.
 */
object AdminConfigManager {
    const val SUPER_ADMIN_EMAIL = "alexgcuicas@gmail.com"
    const val SUPER_ADMIN_NAME = "Alex Gómez"

    private const val PREFS_NAME = "actitud_fuerte_admins"
    private const val KEY_ADMIN_EMAILS = "additional_admin_emails"
    private const val KEY_SUPER_ADMIN_PIN = "super_admin_access_pin"
    const val DEFAULT_SUPER_ADMIN_PIN = "admin"

    private val _adminEmailsFlow = MutableStateFlow<Set<String>>(emptySet())
    val adminEmailsFlow: StateFlow<Set<String>> = _adminEmailsFlow.asStateFlow()

    private val _adminPinFlow = MutableStateFlow<String>(DEFAULT_SUPER_ADMIN_PIN)
    val adminPinFlow: StateFlow<String> = _adminPinFlow.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = (prefs.getStringSet(KEY_ADMIN_EMAILS, emptySet()) ?: emptySet()).toMutableSet()
        // Purgar inmediatamente cualquier residuo no autorizado
        if (stored.any { it.contains("carlen", ignoreCase = true) }) {
            stored.removeAll { it.contains("carlen", ignoreCase = true) }
            prefs.edit().putStringSet(KEY_ADMIN_EMAILS, stored).apply()
        }
        _adminEmailsFlow.value = stored.toSet()
        _adminPinFlow.value = prefs.getString(KEY_SUPER_ADMIN_PIN, DEFAULT_SUPER_ADMIN_PIN) ?: DEFAULT_SUPER_ADMIN_PIN
    }

    /**
     * Obtiene la clave de acceso actual del Super Administrador Alex Gómez.
     */
    fun getSuperAdminPin(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SUPER_ADMIN_PIN, DEFAULT_SUPER_ADMIN_PIN) ?: DEFAULT_SUPER_ADMIN_PIN
    }

    /**
     * Actualiza la clave de acceso del Super Administrador Alex Gómez.
     */
    fun updateSuperAdminPin(context: Context, newPin: String) {
        val clean = newPin.trim()
        if (clean.isBlank()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SUPER_ADMIN_PIN, clean).apply()
        _adminPinFlow.value = clean
    }

    /**
     * Retorna la lista de todos los correos administradores (incluyendo al Super Admin).
     */
    fun getAllAdmins(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getStringSet(KEY_ADMIN_EMAILS, emptySet()) ?: emptySet()
        val list = mutableListOf(SUPER_ADMIN_EMAIL)
        stored.forEach { email ->
            val clean = email.trim().lowercase()
            if (clean != SUPER_ADMIN_EMAIL.lowercase() && !clean.contains("carlen") && !list.contains(clean)) {
                list.add(clean)
            }
        }
        return list
    }

    /**
     * Valida si un correo electrónico posee privilegios administrativos plenos.
     */
    fun isAdmin(context: Context, email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        val clean = email.trim().lowercase()
        // Restricción explícita de seguridad: Carlen Chirinos es usuaria/atleta estándar
        if (clean.contains("carlenchirinos")) return false
        if (clean == SUPER_ADMIN_EMAIL.lowercase()) return true
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getStringSet(KEY_ADMIN_EMAILS, emptySet()) ?: emptySet()
        return stored.any { it.trim().equals(clean, ignoreCase = true) }
    }

    /**
     * Agrega un nuevo administrador mediante su correo electrónico.
     */
    fun addAdmin(context: Context, email: String): Boolean {
        val clean = email.trim().lowercase()
        if (clean.isBlank() || !clean.contains("@") || clean == SUPER_ADMIN_EMAIL.lowercase() || clean.contains("carlenchirinos")) {
            return false
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = (prefs.getStringSet(KEY_ADMIN_EMAILS, emptySet()) ?: emptySet()).toMutableSet()
        current.add(clean)
        prefs.edit().putStringSet(KEY_ADMIN_EMAILS, current).apply()
        _adminEmailsFlow.value = current.toSet()
        return true
    }

    /**
     * Revoca los privilegios de un administrador adicional.
     * El Super Administrador Alex Gómez es inmutable y no puede ser removido.
     */
    fun removeAdmin(context: Context, email: String): Boolean {
        val clean = email.trim().lowercase()
        if (clean == SUPER_ADMIN_EMAIL.lowercase()) return false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = (prefs.getStringSet(KEY_ADMIN_EMAILS, emptySet()) ?: emptySet()).toMutableSet()
        val removed = current.remove(clean)
        if (removed) {
            prefs.edit().putStringSet(KEY_ADMIN_EMAILS, current).apply()
            _adminEmailsFlow.value = current.toSet()
        }
        return removed
    }
}
