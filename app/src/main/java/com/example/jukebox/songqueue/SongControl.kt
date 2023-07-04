package com.example.jukebox.songqueue

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.jukebox.R
import com.example.jukebox.RoomManager
import com.example.jukebox.spotify.task.SpotifySongControlTask.getPlaybackState
import com.example.jukebox.spotify.task.SpotifySongControlTask.pauseSong
import com.example.jukebox.spotify.task.SpotifySongControlTask.playPreviousSong
import com.example.jukebox.spotify.task.SpotifySongControlTask.playSong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

private fun getHostToken(
    roomCode: String,
    hostToken: MutableStateFlow<String>,
    roomManager: RoomManager?
) {
    roomManager?.getHostToken(roomCode) { token ->
        hostToken.value = token
    }
}

private fun getUserTokens(
    roomCode: String,
    userTokens: MutableStateFlow<MutableList<String>>,
    roomManager: RoomManager?
) {
    roomManager?.getUsers(roomCode) { users ->
        val tokens = mutableListOf<String>()
        users.forEach { tokens.add(it.userToken) }
        userTokens.value = tokens
    }
}

@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun SongControl(roomCode: String, roomManager: RoomManager?) {
    val scope = rememberCoroutineScope()
    var hostToken = MutableStateFlow("")
    getHostToken(roomCode, hostToken, roomManager)
    var userTokens = MutableStateFlow<MutableList<String>>(ArrayList())
    getUserTokens(roomCode, userTokens, roomManager)
    var userTokenList = userTokens.collectAsState().value
    userTokenList.add(hostToken.collectAsState().value)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = {
                    Log.d("spotify control: token list", userTokenList.toString())
                    Log.d("spotify control: host token", hostToken.value)
                    scope.launch {
                       // playPreviousSong(userTokenList)
                        playSong("spotify:album:5ht7ItJgpBH7W6vJ5BqpPr", 0, userTokenList)
                    }
            }
        ) {
            Image(painter = painterResource(id = R.drawable.previous_track), contentDescription = null)
        }
        Button(
            onClick = {
                scope.launch {
                    pauseSong(userTokenList)
                }
            }
        ) {
            Image(painter = painterResource(id = R.drawable.pause_track), contentDescription = null)
        }
        Button(
            onClick = {
                //TODO: https://api.spotify.com/v1/me/player/next
                Log.d("spotify fetch state: host token", hostToken.value)
                scope.launch {
                   // playPreviousSong(userTokenList)
                    val playBackState = getPlaybackState(hostToken.value)
                    playBackState.first?.let { Log.d("spotify fetch state: context_uri", it) }
                    Log.d("spotify fetch state: offset", playBackState.second.toString())
                }
            }
        ) {
            Image(painter = painterResource(id = R.drawable.next_track), contentDescription = null)
        }
    }
}
