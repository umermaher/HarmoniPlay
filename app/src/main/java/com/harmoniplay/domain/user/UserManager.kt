package com.harmoniplay.domain.user

interface UserManager {
    suspend fun saveUser(name: String)
    fun getName(): String
    fun getGreetings(): String
    fun isLoggedIn(): Boolean
    fun logOut()
    fun playMusicBy(playBy: String)
    fun getPlayMusicBy(): String
}
