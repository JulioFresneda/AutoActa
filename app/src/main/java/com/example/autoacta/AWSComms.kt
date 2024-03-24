import android.util.Log
import com.amplifyframework.core.Amplify
import com.google.gson.Gson
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

object SummaryJobMetadata {
    fun uploadToS3(jobName: String, jobDescription: String, actors: String) {
        // Serialization to JSON can happen here if needed
        val summaryData = mapOf(
            "jobName" to jobName,
            "jobDescription" to jobDescription,
            "actors" to actors
        )

        val jsonData = Gson().toJson(summaryData)
        val inputStream = ByteArrayInputStream(jsonData.toByteArray(StandardCharsets.UTF_8))

        // Amplify Storage does not require manually setting content length or metadata
        val key = "user1/$jobName/config.json"

        Amplify.Storage.uploadInputStream(key, inputStream,

            { Log.i("MyAmplifyApp", "Successfully uploaded: ${it.key}") },

            { Log.e("MyAmplifyApp", "Upload failed", it) }

        )
    }
}
