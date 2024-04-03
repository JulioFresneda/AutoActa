import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StorageException
import com.amplifyframework.storage.options.StoragePagedListOptions
import com.google.gson.Gson
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets

import com.amplifyframework.storage.options.StorageListOptions
import com.amplifyframework.storage.result.StorageListResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object S3Comms {

    data class JobConfig(
        val jobName: String,
        val description: String,
        val actors: String,
        val language: String,
        val audioFilename: String
    )



    suspend fun listSummaries(context: Context): List<JobConfig> = suspendCancellableCoroutine { continuation ->
        val options = StorageListOptions.builder()
            .build()

        Amplify.Storage.list("user1", options,
            { result: StorageListResult ->
                val jobConfigs = mutableListOf<JobConfig>()
                result.items.forEach { item ->
                    if ("config.json" in item.key) {
                        try {
                            val key = item.key
                            val config = loadConfigFromS3(context, key) // Ensure this is handled asynchronously
                            config?.let { jobConfigs.add(it) }
                        } catch (e: Exception) {
                            Log.e("ListSummaries", "Error loading config for ${item.key}", e)
                        }
                    }
                }
                continuation.resume(jobConfigs)
            },
            { error: StorageException ->
                Log.e("ListSummaries", "List failure", error)
                continuation.resumeWithException(error)
            }
        )
    }

    fun createTempFileCustom(context: Context): File {
        // Ensure the cache directory exists
        val cacheDir = context.cacheDir
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            Log.e("MyApp", "Failed to create cache directory")
            throw IOException("Could not create cache directory")
        }

        // Create a File instance for the temporary file
        val tempFile = File(cacheDir, "tempConfig.json")

        // Check if the file exists, and if not, create it
        if (!tempFile.exists()) {
            try {
                if (!tempFile.createNewFile()) {
                    Log.e("MyApp", "Failed to create temp file")
                    throw IOException("Could not create temp file")
                }
            } catch (e: IOException) {
                Log.e("MyApp", "Error creating temp file", e)
                throw e  // Re-throw the exception or handle it as needed
            }
        }

        return tempFile
    }



    fun loadConfigFromS3(context: Context, s3Key: String): JobConfig? {
        val file = File("${context.filesDir}/download.txt")
        var jobConfig: JobConfig? = null
        var isError = false

        val latch = CountDownLatch(1)


        Amplify.Storage.downloadFile(s3Key, file,
            { result ->
                Log.i("DownloadFile", "Successfully downloaded: ${result.file.absolutePath}")
                latch.countDown()
            },
            { error ->
                Log.e("DownloadFile", "Download Failure", error)
                latch.countDown()
            }
        )

        try {
            latch.await()  // Wait for the download to complete
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            return null
        }

        val jsonString = file.readText()
        jobConfig = Gson().fromJson(jsonString, JobConfig::class.java)


        file.delete()



        return jobConfig
    }




    fun uploadToS3(audioFile: File, jobName: String, jobDescription: String, actors: String) {
        val language = "es"  // Set the language or make it dynamic as needed
        val audioFilename = "$jobName.wav"

        // Create the JobConfig object
        val jobConfig = JobConfig(
            jobName = jobName,
            description = jobDescription,
            actors = actors,
            language = language,
            audioFilename = audioFilename
        )

        val keyConfig = "user1/${jobConfig.jobName}/config.json"
        val keyAudio = "user1/${jobConfig.jobName}/${jobConfig.audioFilename}"

        val jsonData = Gson().toJson(jobConfig)
        val inputStream = ByteArrayInputStream(jsonData.toByteArray(StandardCharsets.UTF_8))

        // Upload JSON configuration
        Amplify.Storage.uploadInputStream(keyConfig, inputStream,
            { Log.i("MyAmplifyApp", "Successfully uploaded config: ${it.key}") },
            { Log.e("MyAmplifyApp", "Config upload failed", it) }
        )

        // Upload audio file
        Amplify.Storage.uploadFile(keyAudio, audioFile,
            { Log.i("MyAmplifyApp", "Successfully uploaded audio file: ${it.key}") },
            { Log.e("MyAmplifyApp", "Audio file upload failed", it) }
        )
    }




}





