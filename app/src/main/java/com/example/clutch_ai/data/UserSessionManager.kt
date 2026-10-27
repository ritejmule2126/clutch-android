package com.example.clutch_ai.data

import android.content.Context
import android.content.SharedPreferences

class UserSessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "clutch_ai_session_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_full_name"
        private const val KEY_FIRST_VISIT_DONE = "first_visit_completed"
    }

    fun hasCompletedFirstVisit(): Boolean = prefs.getBoolean(KEY_FIRST_VISIT_DONE, false)

    fun markFirstVisitCompleted() {
        prefs.edit().putBoolean(KEY_FIRST_VISIT_DONE, true).apply()
    }

    fun saveSession(accessToken: String, userId: String, email: String, fullName: String?) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, fullName ?: "")
            .putBoolean(KEY_FIRST_VISIT_DONE, true)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun getUserFullName(): String? = prefs.getString(KEY_USER_NAME, null)

    fun isLoggedIn(): Boolean = !getAccessToken().isNullOrBlank()

    fun clearSession() {
        // Keep KEY_FIRST_VISIT_DONE intact so user is not repeatedly sent to signup page
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .apply()
    }
}
