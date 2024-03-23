import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.options.StorageUploadDataOptions
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

        try {

            // Using Amplify to upload the ByteArrayInputStream directly
            Amplify.Storage.uploadInputStream(
                key,
                inputStream,
                StorageUploadDataOptions.defaultInstance(),
                { progress -> println("Upload progress: ${progress.fractionCompleted}") },
                { result -> println("Successfully uploaded data to S3 with key: ${result.key}") },
                { error -> println("Failed to upload data to S3: $error") }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            println("Encountered an error during the upload process")
        }
    }
}
