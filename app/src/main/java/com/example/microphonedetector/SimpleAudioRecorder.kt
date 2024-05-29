import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.io.File
import java.io.FileOutputStream

class SimpleAudioRecorder(private val sampleRateInHz: Int = 44100,
                          private val channelConfig: Int = AudioFormat.CHANNEL_IN_STEREO,
                          private val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT) {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)

    fun startRecording(fileName: String) {
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes)
        audioRecord?.startRecording()
        isRecording = true
        Thread {
            writeAudioDataToFile(fileName)
        }.start()
    }

    private fun writeAudioDataToFile(fileName: String) {
        val audioData = ShortArray(bufferSizeInBytes)
        val file = File(fileName)
        FileOutputStream(file).use { os ->
            while (isRecording) {
                val readSize = audioRecord?.read(audioData, 0, bufferSizeInBytes) ?: 0
                if (readSize > 0) {
                    val buffer = ByteArray(readSize * 2)
                    audioData.forEachIndexed { index, value ->
                        val bytes = value.toInt().toByte() // Simple conversion; for real use cases consider proper conversion
                        buffer[index * 2] = bytes
                        buffer[index * 2 + 1] = (value.toInt() shr 8).toByte()
                    }
                    os.write(buffer)
                }
            }
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        }
    }

    fun stopRecording() {
        isRecording = false
    }
}
