package com.example.jukebox

import android.os.Looper
import android.util.Log
import com.example.jukebox.spotify.task.SpotifySongControlTask
import com.example.jukebox.util.SongTimer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.runBlocking

class CurrentSong {

	companion object {
		private var duration: MutableStateFlow<Int> = MutableStateFlow(-1)
		val currentTime: MutableStateFlow<Int> = MutableStateFlow(-1)
		val roomManager = RoomManager()
		var roomCode: String = ""
		var uTokens: MutableStateFlow<MutableList<String>> = MutableStateFlow(ArrayList())
		var pausedTime = 0
		private var currentSong: MutableStateFlow<String> = MutableStateFlow("")

		private var timer : SongTimer? = null

		fun setDuration(duration: Int, currentTime: Int = -1, songUri: String = "") {
			this.duration.value = duration
			this.currentTime.value = currentTime
			this.currentSong.value = songUri
		}

		fun setCurrentSong(songUri: String) {
			this.currentSong.value = songUri
		}

		fun setInitialVars(roomCode: String, uTokens: MutableStateFlow<MutableList<String>>) {
			if (Looper.myLooper() == null) {
				Looper.prepare()
			}
			this.roomCode = roomCode
			this.uTokens = uTokens
		}

		suspend fun onTimerFinishes() {
			currentTime.collectLatest {
				if (it == 0) {
					Log.d("song ended: ", it.toString())
					roomManager.advanceSong(roomCode)
					val currentSong = runBlocking { roomManager.getCurrentSong(roomCode) }
					if (currentSong != null) {
						SpotifySongControlTask.playSong(
							currentSong.context_uri,
							0,
							uTokens.value
						)
						resetTimer()
						setDuration(duration = currentSong.duration, songUri = currentSong.context_uri)
						roomManager.setNewDuration(roomCode, currentSong.duration)
					}
				}
			}
		}

		suspend fun onSongChanged() {
			currentSong.collectLatest {
				roomManager.setNewSong(roomCode, it)
				Log.d("duration changed", (duration.value/1000).toString())
				if (Looper.myLooper() == null) {
					Looper.prepare()
				}

				if (timer != null) {
					timer?.cancel()
				}
				timer = SongTimer(duration.value.toLong(), currentTime)
				timer?.start()
			}
		}

		suspend fun onDurationChanged() {
			duration.collectLatest {
				Log.d("duration changed", (it/1000).toString())
				if (Looper.myLooper() == null) {
					Looper.prepare()
				}

				if (timer != null) {
					timer?.cancel()
				}
				timer = SongTimer(it.toLong(), currentTime)
				timer?.start()
			}
		}

		fun startTimer(){
			pausedTime = duration.value
			timer = SongTimer(duration.value.toLong(), currentTime)
			timer?.start()
		}

		fun pauseTimer(){
			pausedTime = currentTime.value * 1000
			timer?.cancel()
		}

		fun resumeTimer() {
			timer = SongTimer(pausedTime.toLong(), currentTime)
			timer?.start()
		}

		fun resetTimer() {
			timer?.cancel()
			timer = SongTimer(duration.value.toLong(), currentTime)
			timer?.start()
		}

		fun stopTimer(){
			currentTime.value = duration.value
			timer?.cancel()
		}
	}
}