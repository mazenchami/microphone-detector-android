package com.example.microphonedetector

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder

class StereoAudioProcessor(private val sampleRate: Int = 44100) {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false

    // Assuming a stereo configuration
    private val channelConfig = AudioFormat.CHANNEL_IN_STEREO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    init {
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, bufferSize)
    }

    fun startRecording() {
        audioRecord?.startRecording()
        isRecording = true

        Thread {
            processAudioStream()
        }.start()
    }

    private fun processAudioStream() {
        val audioBuffer = ShortArray(bufferSize)
        while (isRecording) {
            val readResult = audioRecord?.read(audioBuffer, 0, bufferSize) ?: 0
            if (readResult > 0) {
                // Process the buffer
                analyzeStereoBuffer(audioBuffer, readResult)
            }
        }
    }

    private fun analyzeStereoBuffer(buffer: ShortArray, readSize: Int) {
        // Define a threshold for what you consider a "loud" sound.
        val threshold = 5000 // Example threshold, adjust based on your needs

        var leftChannelSum: Long = 0
        var rightChannelSum: Long = 0
        var loudSamplesLeft = 0
        var loudSamplesRight = 0

        for (i in 0 until readSize step 2) {
            val leftSample = Math.abs(buffer[i].toInt())
            val rightSample = Math.abs(buffer[i + 1].toInt())

            // Only consider samples louder than the threshold
            if (leftSample > threshold) {
                leftChannelSum += leftSample
                loudSamplesLeft++
            }
            if (rightSample > threshold) {
                rightChannelSum += rightSample
                loudSamplesRight++
            }
        }

        // Avoid division by zero by ensuring there's at least one loud sample per channel
        if (loudSamplesLeft > 0 || loudSamplesRight > 0) {
            val leftChannelAverage = if (loudSamplesLeft > 0) leftChannelSum / loudSamplesLeft else 0
            val rightChannelAverage = if (loudSamplesRight > 0) rightChannelSum / loudSamplesRight else 0

            // Use the averages to guess the direction of incoming loud sound
            val direction = if (leftChannelAverage > rightChannelAverage) "Left" else if (leftChannelAverage < rightChannelAverage) "Right" else "Equal or No loud sound"
            println("Dominant loud sound direction: $direction")
        }
    }

    fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
    }
}
