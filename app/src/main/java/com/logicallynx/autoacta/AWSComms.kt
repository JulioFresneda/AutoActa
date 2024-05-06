import android.content.Context
import android.util.Log
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.result.AuthSessionResult
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.StorageException
import com.amplifyframework.storage.options.StorageDownloadFileOptions
import com.google.gson.Gson
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets

import com.amplifyframework.storage.options.StorageListOptions
import com.amplifyframework.storage.options.StoragePagedListOptions
import com.amplifyframework.storage.options.StorageUploadFileOptions
import com.amplifyframework.storage.options.StorageUploadInputStreamOptions
import com.amplifyframework.storage.result.StorageListResult
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.amplifyframework.auth.AuthUserAttribute
import com.amplifyframework.auth.AuthUserAttributeKey
import com.logicallynx.autoacta.LanguageSelected
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object S3Comms {

    data class JobConfig(
        val jobName: String,
        val description: String,
        val audioFilename: String,
        val email: String,
        val tags: List<String>,
        val date: String,
        val language: String
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
                Log.i("ListSummaries", "Received identity ID: $identityId")

                val options = StoragePagedListOptions.builder()
                    .setPageSize(10000)
                    .accessLevel(StorageAccessLevel.PRIVATE)
                    .targetIdentityId(identityId)
                    .build()

                Amplify.Storage.list("", options,
                    { result: StorageListResult ->
                        val jobConfigs = mutableListOf<JobConfig>()
                        var sortedJobConfigs = mutableListOf<JobConfig>()
                        result.items.forEach { item ->

                            if ("config.json" in item.key) {
                                try {
                                    val key = item.key
                                    Log.i("ListSummaries", item.key)
                                    val config = loadConfigFromS3(context,  key) // Ensure this is handled asynchronously
                                    config?.let { jobConfigs.add(it)

                                    }
                                } catch (e: Exception) {
                                    Log.e("ListSummaries", "Error loading config for ${item.key}", e)
                                }
                            }
                        }
                        sortedJobConfigs = jobConfigs.sortedByDescending { LocalDateTime.parse(it.date) }.toMutableList()
                        continuation.resume(sortedJobConfigs)
                    },
                    { error: StorageException ->
                        Log.e("ListSummaries", "List failure", error)
                        continuation.resumeWithException(error)
                    }
                )

                Log.i("Summaries", "Everything ok")
            } else {
                Log.i("Summaries", "Identity ID was null")
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




    fun uploadToS3(
        audioFile: File,
        jobName: String,
        jobDescription: String,
        email: String,
        tags: List<String>
    ) {

        val audioFilename = "$jobName.wav"

        // Create the JobConfig object
        val lang = LanguageSelected.language

        val jobConfig = JobConfig(
            jobName = jobName,
            description = jobDescription,
            audioFilename = audioFilename,
            email = email,
            tags = tags,
            date = LocalDateTime.now().toString(),
            language = lang.toString()
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

    fun downloadPdf(key: String, localFilePath: String, onSuccess: () -> Unit, onError: (StorageException) -> Unit) {

        val options = StorageDownloadFileOptions.builder()
            .accessLevel(StorageAccessLevel.PRIVATE)
            .build()

        Amplify.Storage.downloadFile(
            key,
            File(localFilePath),
            options,
            { progress -> Log.i("DownloadPDF", "Progress: ${(progress.fractionCompleted * 100).toInt()}%") },
            { result ->
                Log.i("DownloadPDF", "Successfully downloaded: ${result.file.name}")
                onSuccess()
            },
            { error ->
                Log.e("DownloadPDF", "Download Failure", error)
                onError(error)
            }
        )
    }

    interface FetchMinutesCallback {
        fun onSuccess(minutes: Int)
        fun onError(e: Exception)
    }

    interface FetchPlanCallback {
        fun onSuccess(plan: String)
        fun onError(e: Exception)
    }

    fun fetchRemainingMinutesWithCallbacks(callback: FetchMinutesCallback) {
        Amplify.Auth.fetchUserAttributes(
            { attributes ->
                val remainingMinutesAttr = attributes.find { it.key.keyString == "custom:remaining_minutes" }
                if (remainingMinutesAttr != null) {
                    try {
                        val minutes = remainingMinutesAttr.value.toInt()
                        callback.onSuccess(minutes)
                    } catch (e: NumberFormatException) {
                        callback.onError(e)
                    }
                } else {
                    callback.onError(Exception("Attribute custom:remaining_minutes not found"))
                }
            },
            { error ->
                callback.onError(error)
            }
        )
    }

    fun fetchPlanWithCallbacks(callback: FetchPlanCallback) {
        Amplify.Auth.fetchUserAttributes(
            { attributes ->
                val plan = attributes.find { it.key.keyString == "custom:plan" }
                if (plan != null) {
                    try {
                        var planstr = plan.value.toString()
                        callback.onSuccess(planstr)
                    } catch (e: NumberFormatException) {
                        callback.onError(e)
                    }
                } else {
                    callback.onError(Exception("Attribute custom:plan not found"))
                }
            },
            { error ->
                callback.onError(error)
            }
        )
    }




    fun updateRemainingMinutes(value: Int) {


        val attributes = listOf(
            AuthUserAttribute(AuthUserAttributeKey.custom("custom:remaining_minutes"),
                value.toString()
            )
        )

        Amplify.Auth.updateUserAttributes(
            attributes,
            { result ->
                Log.i("UpdatedAttribute", "Attributes update successful: $result")
                // Handle success, perhaps update UI or notify user
            },
            { error ->
                Log.e("UpdatedAttribute", "Failed to update attributes", error)
                // Handle error, update UI or notify user accordingly
            }
        )
    }






}








