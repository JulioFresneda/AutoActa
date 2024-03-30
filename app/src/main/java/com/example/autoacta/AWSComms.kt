import android.util.Log
import com.amplifyframework.core.Amplify
import com.google.gson.Gson
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets

object SummaryJobMetadata {
    fun uploadToS3(audioFile: File, jobName: String, jobDescription: String, actors: String) {
        // Serialization to JSON can happen here if needed
        var language = "es"
        var audioFilename = "$jobName.wav"
        val key = "user1/$jobName/config.json"
        val path = "public/user1/$jobName"
        val keyAudio = "user1/$jobName/$jobName.wav"

        val summaryData = mapOf(
            "jobName" to jobName,
            "description" to jobDescription,
            "actors" to actors,
            "language" to language,
            "audioFilename" to audioFilename,
            "path" to path
        )

        val jsonData = Gson().toJson(summaryData)
        val inputStream = ByteArrayInputStream(jsonData.toByteArray(StandardCharsets.UTF_8))

        // Amplify Storage does not require manually setting content length or metadata


        Amplify.Storage.uploadInputStream(key, inputStream,

            { Log.i("MyAmplifyApp", "Successfully uploaded: ${it.key}") },

            { Log.e("MyAmplifyApp", "Upload failed", it) }

        )



        Amplify.Storage.uploadFile(keyAudio, audioFile,

            { Log.i("MyAmplifyApp", "Successfully file uploaded: ${it.key}") },

            { Log.e("MyAmplifyApp", "Upload failed", it) }

        )


    }
}





