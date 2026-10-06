package com.arc.training

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class AppStateStore(context: Context) {
    private val file = File(context.filesDir, "arc_state.bin")

    fun load(): AppState = try {
        if (!file.exists()) AppState()
        else ObjectInputStream(FileInputStream(file)).use { input -> input.readObject() as AppState }
    } catch (_: Exception) {
        AppState()
    }

    fun save(state: AppState) {
        try {
            FileOutputStream(file).use { out -> ObjectOutputStream(out).use { output -> output.writeObject(state) } }
        } catch (_: Exception) { }
    }
}
