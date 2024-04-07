package com.example.autoacta

import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.autoacta.ui.theme.AutoActaTheme
import java.io.File
import java.io.IOException

import android.Manifest
import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.s3.AWSS3StoragePlugin

import com.amplifyframework.ui.authenticator.ui.Authenticator
import com.yourapp.ui.com.example.autoacta.AuthenticatorScreen.SetUpLoginWithGoogle
import com.yourapp.ui.com.example.autoacta.AuthenticatorScreen.isUserSignedIn
import com.yourapp.ui.com.example.autoacta.SignInForm

// TODO - Formatear codigo
// Docx
// TODO - Cuadrar informes
// Backend
// TODO - Mejorar prompt chatgpt
// TODO - Añadir etapa translate
// TODO - Mejorar sign in
// Frontend
// TODO - Añadir opciones exportacion





data class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var audioFile: File

    private var showMakeSummaryDialog by mutableStateOf(false)

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.configure(applicationContext)
            Log.i("MyAmplifyApp", "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
        }





        setContent {
            val isAuthenticated = remember { mutableStateOf(false) }



            AutoActaTheme {
                if (isAuthenticated.value) {
                    MainUI(isAuthenticated)
                    if (showMakeSummaryDialog) {
                        // Show the full-screen composable when requested
                        SaveAudioActivityBack(
                            showContent = showMakeSummaryDialog,
                            onShowContentChange = { showMakeSummaryDialog = it },
                            audioFile = audioFile
                        )
                    }
                } else {
                    Authenticator(
                        signInContent = { state ->
                            val scope = rememberCoroutineScope()
                            SignInForm(
                                state,
                                scope,
                                SetUpLoginWithGoogle = { SetUpLoginWithGoogle(this, isAuthenticated) })
                        }
                    )
                    {
                        isAuthenticated.value = true
                    }
                }
            }
        }
    }


    // Sets up login using AWS Amplify with Google as the social sign-in provider.





    @Composable
    fun MainUI(isAuthenticated: MutableState<Boolean>){
        val navController = rememberNavController()
        val items = listOf(
            NavigationItem(
                title = "Home",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
            ),
            NavigationItem(
                title = "Summaries",
                selectedIcon = Icons.Filled.Call,
                unselectedIcon = Icons.Outlined.Call,
            ),
            NavigationItem(
                title = "Premium plan",
                selectedIcon = Icons.Filled.Star,
                unselectedIcon = Icons.Outlined.Star,
            ),
            NavigationItem(
                title = "Settings",
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
            ),
        )
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            var selectedItemIndex by rememberSaveable {
                mutableStateOf(0)
            }
            // Define your custom colors here
            val customColors = NavigationDrawerItemDefaults.colors(
                selectedContainerColor = MaterialTheme.colorScheme.primary, // Custom color when item is selected
                unselectedContainerColor = Color.Transparent, // Typically transparent for unselected
                selectedIconColor = MaterialTheme.colorScheme.tertiary, // Custom icon color when item is selected
                unselectedIconColor = MaterialTheme.colorScheme.secondary, // Custom icon color when item is unselected
                selectedTextColor = Color.White, // Custom text color when item is selected
                unselectedTextColor = Color.DarkGray, // Custom text color when item is unselected
                selectedBadgeColor = MaterialTheme.colorScheme.secondary, // Custom badge color when item is selected
                unselectedBadgeColor = MaterialTheme.colorScheme.secondary // Custom badge color when item is unselected
            )


            ModalNavigationDrawer(
                drawerContent = {

                    ModalDrawerSheet {

                        Spacer(modifier = Modifier.height(16.dp))
                        items.forEachIndexed { index, item ->
                            NavigationDrawerItem(
                                colors = customColors,
                                label = {
                                    Text(text = item.title)
                                },
                                selected = index == selectedItemIndex,
                                onClick = {
                                    navController.navigate(item.title)
                                    selectedItemIndex = index
                                    scope.launch {
                                        drawerState.close()
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (index == selectedItemIndex) {
                                            item.selectedIcon
                                        } else item.unselectedIcon,
                                        contentDescription = item.title,
                                        tint = if (index == selectedItemIndex) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary // Navy blue when selected
                                    )
                                },
                                badge = {
                                    item.badgeCount?.let {
                                        Text(text = item.badgeCount.toString())
                                    }
                                },
                                modifier = Modifier
                                    .padding(NavigationDrawerItemDefaults.ItemPadding)

                            )
                        }
                    }
                },
                drawerState = drawerState,
                scrimColor = MaterialTheme.colorScheme.secondary
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),

                            title = {
                                Text(text = "AutoActa")
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Menu"
                                    )
                                }
                            }

                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer

                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "Home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("Home") { homePage() }
                        composable("Summaries") { summariesPage() }
                        composable("Settings") { settingsPage(isAuthenticated) }
                    }
                }

            }

        }
    }





    @Composable
    fun homePage(){

        Box() {
            RecordButton() // Positioned correctly now within the Scaffold's content
        }
    }









    @Composable
    fun RecordButton() {
        var isRecording by remember { mutableStateOf(false) }
        val context = LocalContext.current
        val activity = (context as? Activity)

        Box(
            contentAlignment = Alignment.Center, // Align the button to the center of the Box
            modifier = Modifier.fillMaxSize() // Box occupies the entire screen
        ) {

            Button(onClick = {
                if (isRecording) {
                    // Stop recording
                    isRecording = false
                    stopRecording() // Assume this function correctly stops the recording and releases the MediaRecorder
                    // Potentially navigate to a new activity or handle post-recording logic here
                } else {
                    // Start recording after checking permissions
                    activity?.let {
                        if (checkPermissions(it)) {
                            startRecording()
                            isRecording = true
                        } else {
                            // Permissions not granted, handle accordingly
                            // For instance, showing a rationale or guiding the user to the settings
                        }
                    }
                }
            }, colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)) {
                Text(if (isRecording) "Stop Recording" else "Start Recording")
            }
        }
    }

    private fun checkPermissions(activity: Activity): Boolean {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
            return false
        }
        return true
    }



    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            // Assuming you're using external cache directory for simplicity
            // Consider using getExternalFilesDir(Environment.DIRECTORY_MUSIC) for production
            audioFile = File(externalCacheDir, "recorded_audio.wav")
            setOutputFile(audioFile.absolutePath)

            try {
                prepare()
                start()
                // Recording started
            } catch (e: IOException) {
                // Handle preparation or start failure
                Log.e("MainActivity", "startRecording() failed", e)
            }
        }
    }

    private fun stopRecording() {
        // Stop the recording
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null // Set mediaRecorder to null to indicate it's no longer in use

        // Here, you might want to handle the recorded audio file.
        // For example, you could start an activity to play the recording
        // or upload it to a server, depending on your app's functionality.
        // This is a placeholder for whatever next steps you need.
        handleRecordedAudio(audioFile)
    }

    private fun handleRecordedAudio(audioFile: File) {
        showMakeSummaryDialog = true
    }






}
