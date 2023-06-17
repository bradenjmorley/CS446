package com.example.jukebox.spotify

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {

	private const val accountUrl = "https://accounts.spotify.com"

	fun getAccountUrlInstance(): Retrofit {
		return Retrofit.Builder().baseUrl(accountUrl)
			.addConverterFactory(GsonConverterFactory.create())
			.build()
	}
}