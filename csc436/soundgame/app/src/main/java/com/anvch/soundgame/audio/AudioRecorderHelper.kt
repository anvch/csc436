package com.anvch.soundgame.audio

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import kotlin.concurrent.thread
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.log10
import kotlin.math.sqrt

class AudioRecorderHelper {

    private var audioRecord: AudioRecord? = null
    private val isRecording = AtomicBoolean(false)

    private val sampleRate = 44100
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun start(onAmplitude: (Float) -> Unit) {
        if (isRecording.get()) return

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        audioRecord?.startRecording()
        isRecording.set(true)

        thread {
            val buffer = ShortArray(bufferSize)
            while (isRecording.get()) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0

                if (read > 0) {
                    var sum = 0L
                    for (i in 0 until read) {
                        val s = buffer[i].toInt()
                        sum += s * s
                    }
                    val rms = sqrt(sum.toDouble() / read).toFloat()
                    val db = 20 * log10((rms / 32768f).coerceAtLeast(1e-8f))
                    onAmplitude(db)
                }

                Thread.sleep(30)
            }
        }
    }

    fun stop() {
        isRecording.set(false)
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
}
