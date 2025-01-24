package com.example.moviesdiscovery.core.ui

import com.example.moviesdiscovery.core.ui.model.SnackbarVisualsData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class SnackbarEventBus {

    private val snackbarChannel = Channel<SnackbarVisualsData>(capacity = Channel.BUFFERED)
    val snackbarEvent = snackbarChannel.receiveAsFlow()

    suspend fun send(snackbarData: SnackbarVisualsData) {
        snackbarChannel.send(snackbarData)
    }

}
