package com.harmoniplay.data.user

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.harmoniplay.R
import com.harmoniplay.domain.user.UserManager
import com.harmoniplay.utils.IS_LOGGED_IN
import com.harmoniplay.utils.NAME
import com.harmoniplay.utils.PLAY_BY

class UserManagerImpl(
    private val context: Context,
    private val sp: SharedPreferences,
): UserManager {

    override suspend fun saveUser(name: String) {
        sp.edit {
            putString(NAME, name)
            putBoolean(IS_LOGGED_IN, true)
        }
    }

    override fun getName(): String = sp.getString(NAME, "")!!
    override fun getGreetings(): String =
        "${context.getString(R.string.hello)} ${getName().split(" ")[0]}!"

    override fun playMusicBy(playBy: String) {
        sp.edit {
            putString(PLAY_BY, playBy)
        }
    }

    override fun getPlayMusicBy(): String = sp.getString(PLAY_BY, "ALL") ?: "ALL"

    override fun isLoggedIn(): Boolean = sp.getBoolean(IS_LOGGED_IN, false)

    override fun logOut() {
        sp.edit {
            clear()
        }
    }

}
