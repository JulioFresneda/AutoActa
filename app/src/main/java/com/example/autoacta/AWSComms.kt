import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import aws.smithy.kotlin.runtime.http.auth.AnonymousIdentity.attributes
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.result.AuthSessionResult
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.StorageException
import com.amplifyframework.storage.options.StorageDownloadFileOptions
import com.amplifyframework.storage.options.StoragePagedListOptions
import com.google.gson.Gson
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets

import com.amplifyframework.storage.options.StorageListOptions
import com.amplifyframework.storage.options.StorageUploadFileOptions
import com.amplifyframework.storage.options.StorageUploadInputStreamOptions
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
        val audioFilename: String,
        val email: String
    )


    fun fetchIdentityId(onResult: (String?) -> Unit) {
        Amplify.Auth.fetchAuthSession(
            { session ->
                val cognitoSession = session as AWSCognitoAuthSession
                when (cognitoSession.identityIdResult.type) {
                    AuthSessionResult.Type.SUCCESS -> {
                        val identityId = cognitoSession.identityIdResult.value
                        Log.i("AuthQuickStart", "IdentityId = $identityId")
                        onResult(identityId) // Pass the identity ID to the callback
                    }
                    AuthSessionResult.Type.FAILURE -> {
                        Log.w("AuthQuickStart", "IdentityId not found", cognitoSession.identityIdResult.error)
                        onResult(null)
                    }
                }
            },
            { error ->
                Log.e("AuthQuickStart", "Failed to fetch session", error)
                onResult(null)
            }
        )
    }

    fun fetchEmail(onResult: (String?) -> Unit) {
        Amplify.Auth.fetchUserAttributes(
            { attributes ->
                val emailAttribute = attributes.find { it.key.keyString == "email" }
                val fetchedEmail = emailAttribute?.value
                onResult(fetchedEmail)
            },
            { error ->
                Log.e("AuthQuickStart", "Failed to fetch user attributes", error)
                onResult(null)
            }
        )
    }

    suspend fun listSummaries(context: Context): List<JobConfig> = suspendCancellableCoroutine { continuation ->





        fetchIdentityId { identityId ->
            if (identityId != null) {
                Log.i("AuthQuickStart", "Received identity ID: $identityId")

                val options = StorageListOptions.builder()
                    .accessLevel(StorageAccessLevel.PRIVATE)
                    .targetIdentityId(identityId)
                    .build()

                Amplify.Storage.list("", options,
                    { result: StorageListResult ->
                        val jobConfigs = mutableListOf<JobConfig>()
                        result.items.forEach { item ->

                            if ("config.json" in item.key) {
                                try {
                                    val key = item.key
                                    Log.i("Sumario", item.key)
                                    val config = loadConfigFromS3(context,  key) // Ensure this is handled asynchronously
                                    config?.let { jobConfigs.add(it)

                                    }
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
            } else {
                Log.i("AuthQuickStart", "Identity ID was null")
            }
        }

    }





    fun loadConfigFromS3(context: Context, s3Key: String): JobConfig? {
        val file = File("${context.filesDir}/download.txt")
        var jobConfig: JobConfig? = null
        var isError = false

        val latch = CountDownLatch(1)

        val options = StorageDownloadFileOptions.builder()
            .accessLevel(StorageAccessLevel.PRIVATE)
            .build()

        Amplify.Storage.downloadFile(s3Key, file, options,
            { result ->
                Log.i("DownloadFile", "Successfully downloaded: ${result.file.absolutePath}")
                latch.countDown()
            },
            { error ->
                Log.i("DownloadFile", s3Key)
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




    fun uploadToS3(audioFile: File, jobName: String, jobDescription: String, actors: String, email: String) {
        val language = "es"  // Set the language or make it dynamic as needed
        val audioFilename = "$jobName.wav"

        // Create the JobConfig object
        val jobConfig = JobConfig(
            jobName = jobName,
            description = jobDescription,
            actors = actors,
            language = language,
            audioFilename = audioFilename,
            email = email
        )

        val keyConfig = "${jobConfig.jobName}/config.json"
        val keyAudio = "${jobConfig.jobName}/${jobConfig.audioFilename}"

        val jsonData = Gson().toJson(jobConfig)
        val inputStream = ByteArrayInputStream(jsonData.toByteArray(StandardCharsets.UTF_8))


        val fileOptions = StorageUploadFileOptions.builder()
            .accessLevel(StorageAccessLevel.PRIVATE)
            .build()

        val isOptions = StorageUploadInputStreamOptions.builder()
            .accessLevel(StorageAccessLevel.PRIVATE)
            .build()


        // Upload audio file
        Amplify.Storage.uploadFile(keyAudio, audioFile, fileOptions,
            { Log.i("MyAmplifyApp", "Successfully uploaded audio file: ${it.key}") },
            { Log.e("MyAmplifyApp", "Audio file upload failed", it) }
        )

        // Upload JSON configuration
        Amplify.Storage.uploadInputStream(keyConfig, inputStream, isOptions,
            { Log.i("MyAmplifyApp", "Successfully uploaded config: ${it.key}") },
            { Log.e("MyAmplifyApp", "Config upload failed", it) }
        )


    }




}








