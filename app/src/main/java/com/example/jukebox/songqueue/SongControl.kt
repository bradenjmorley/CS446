package com.example.jukebox.songqueue

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.jukebox.R
import com.example.jukebox.RoomManager
import com.example.jukebox.spotify.task.SpotifySongControlTask.playPreviousSong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

private fun getHostToken(roomCode: String, hostToken: MutableStateFlow<String>) {
    val roomManager = RoomManager()
    roomManager.getHostToken(roomCode) { name ->
        hostToken.value = name
    }
}

private fun getUserTokens(roomCode: String, userTokens: MutableStateFlow<List<String>>) {
    val roomManager = RoomManager()
    roomManager.getUserTokens(roomCode) { name ->
        userTokens.value = name
    }
}

@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun SongControl(roomCode: String, roomManager: RoomManager?) {
    val scope = rememberCoroutineScope()
    var hostToken = MutableStateFlow("")
    getHostToken(roomCode, hostToken)
    var userTokens = MutableStateFlow<List<String>>(emptyList())
    getUserTokens(roomCode, userTokens)
    val combinedTokens = hostToken.combine(userTokens) { hostTokenValue, userTokensValue ->
        userTokensValue + listOf(hostTokenValue)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = {
                    scope.launch {
                        Log.d("spotify control: ", "play prev button")
                        playPreviousSong(combinedTokens as List<String>)
                    }
            }
        ) {
            Image(painter = painterResource(id = R.drawable.previous_track), contentDescription = null)
        }
        Button(
            onClick = {
                //TODO: https://api.spotify.com/v1/me/player/play
            }
        ) {
            Image(painter = painterResource(id = R.drawable.pause_track), contentDescription = null)
        }
        Button(
            onClick = {
                //TODO: https://api.spotify.com/v1/me/player/next
            }
        ) {
            Image(painter = painterResource(id = R.drawable.next_track), contentDescription = null)
        }
    }
}
