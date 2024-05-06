package com.logicallynx.autoacta

import S3Comms
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.logicallynx.autoacta.ui.theme.AutoActaTheme
import java.io.File
import java.io.IOException

import android.Manifest
import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth


import androidx.compose.ui.Alignment

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material.Text
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.s3.AWSS3StoragePlugin

import com.amplifyframework.ui.authenticator.ui.Authenticator
import com.logicallynx.autoacta.AuthenticatorScreen.SetUpLoginWithGoogle
import kotlinx.coroutines.launch
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.delay










sealed class Screens(val route : String) {
    object Home : Screens("home_route")
    object Summaries : Screens("summaries_route")
    object Premium : Screens("premium_route")
    object Settings : Screens("settings_route")
}

data class BottomNavigationItem(
    val label : String = "",
    val icon : ImageVector = Icons.Filled.Home,
    val route : String = ""
) {

    //function to get the list of bottomNavigationItems
    @Composable
    fun bottomNavigationItems() : List<BottomNavigationItem> {
        return listOf(
            BottomNavigationItem(
                label = LocalContext.current.getString(R.string.home),
                icon = Icons.Filled.Home,
                route = Screens.Home.route
            ),
            BottomNavigationItem(
                label = LocalContext.current.getString(R.string.summaries),
                icon = Icons.Filled.Call,
                route = Screens.Summaries.route
            ),
            BottomNavigationItem(
                label = LocalContext.current.getString(R.string.account),
                icon = Icons.Filled.AccountCircle,
                route = Screens.Settings.route
            )
        )
    }
}







@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var audioFile: File

    private var showMakeSummaryDialog by mutableStateOf(false)

    companion object {
        private const val REQUEST_CODE = 200
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
                    BottomNavigationBar(isAuthenticated, showMakeSummaryDialog)

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



    @Composable
    fun homePage() {

        val showDescription = remember { mutableStateOf(false) }

        val typewriterFontFamily = FontFamily(
            Font(R.font.texgyrecursor_regular),
            Font(R.font.texgyrecursor_bold)// Use the resource identifier for your font
        )

        val typewriterFontStyle = TextStyle(
            fontFamily = typewriterFontFamily,
            fontSize = 50.sp,
            color = MaterialTheme.colorScheme.primary,  // Specify the text color
            letterSpacing = 0.5.sp,  // Adjust letter spacing
            lineHeight = 22.sp  // Adjust line height
        )



        val toastText = getString(R.string.mm_aa_desc)



        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Box(

                modifier = Modifier
                    .background(
                        color = Color.White.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),


                        )
                    .border(
                        BorderStroke(
                            5.dp,
                            brush = Brushes.gradient3(
                                start = Offset(0f, 0f),
                                end = Offset(500f, 500f),
                                alpha = 0.5f
                            )
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ),


                ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .clickable() {
                            showDescription.value = true
                        },
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "AutoActa",
                        style = typewriterFontStyle
                    )
                    Text(
                        text = getString(R.string.mm_aa_text),
                        color = Color.Black,
                        style = MaterialTheme.typography.bodyMedium
                    )

                }

            }
        }


        Box(contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize() ) {
            RecordButton() // Positioned correctly now within the Scaffold's content
        }


        remaining_minutes_counter()



        if (this.showMakeSummaryDialog) {
            // Show the full-screen composable when requested
            SaveAudioActivityBack(
                showContent = this.showMakeSummaryDialog,
                onShowContentChange = { this.showMakeSummaryDialog = it },
                audioFile = audioFile
            )
        }

        if (showDescription.value) {
            AlertDialog(
                onDismissRequest = { showDescription.value = false },
                title = { androidx.compose.material3.Text(getString(R.string.mm_aa_desc_title)) },
                text = {
                    Text(text = toastText, color = Color.Black)
                },
                confirmButton = {
                    TextButton(onClick = { showDescription.value = false }) {
                        androidx.compose.material3.Text(getString(R.string.close), color = Color.Black)
                    }
                }
            )
        }
    }









    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun RecordButton() {
        var isRecording by remember { mutableStateOf(false) }
        var started by remember { mutableStateOf(false) }

        val context = LocalContext.current
        val activity = (context as? Activity)
        val scale = remember { Animatable(1f) }
        val coroutineScope = rememberCoroutineScope()

        var elapsedSeconds by remember { mutableStateOf(0L) }
        val hours = elapsedSeconds / 3600
        val minutes = (elapsedSeconds % 3600) / 60
        val seconds = elapsedSeconds % 60



        LaunchedEffect(isRecording) {
            while (isRecording) {
                delay(1000)
                elapsedSeconds++
            }
        }

        val defaultColor = MaterialTheme.colorScheme.tertiary
        val tertiaryColor = MaterialTheme.colorScheme.primary
        val colorAnimatable = remember { Animatable(defaultColor) }


        val animatedbrush = Brushes.gradient2animated(tertiaryColor = colorAnimatable.value, center = Offset.Zero, radius = 500f)
        var targetState = R.drawable.micro
        if(!isRecording && started){
            targetState = R.drawable.pause
        }


        Column (
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale.value)

                    .background(
                        brush = Brushes.gradient1(center = Offset.Zero, radius = 500f),
                        shape = MaterialTheme.shapes.large,

                        ),


                ) {
                IconButton(
                    onClick = {
                        Log.i("RecordButton", "Clicked")
                        if (isRecording) {
                            // Stop recording
                            isRecording = false

                            pauseRecording()
                            coroutineScope.launch {
                                colorAnimatable.animateTo(
                                    targetValue = tertiaryColor, // Return to gray
                                    animationSpec = tween(durationMillis = 1000)
                                )
                            }// Assume this function correctly stops the recording and releases the MediaRecorder
                            // Potentially navigate to a new activity or handle post-recording logic here
                        } else {
                            if(started == false){
                                // Start recording after checking permissions
                                activity?.let {
                                    if (checkPermissions(it)) {
                                        Log.i("RecordButton", "Permissions checked")
                                        started = true
                                        startRecording()
                                        isRecording = true
                                        coroutineScope.launch {
                                            colorAnimatable.animateTo(
                                                targetValue = tertiaryColor, // Pulse to red
                                                animationSpec = infiniteRepeatable(
                                                    animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                                                    repeatMode = RepeatMode.Reverse
                                                )
                                            )
                                        }
                                    } else {
                                        // Permissions not granted, handle accordingly
                                        // For instance, showing a rationale or guiding the user to the settings
                                    }
                                }
                            }
                            else{
                                isRecording = true
                                Log.i("RecordingStatus", "Resumed")
                                mediaRecorder?.resume()
                                coroutineScope.launch {
                                    colorAnimatable.animateTo(
                                        targetValue = tertiaryColor, // Pulse to red
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                                            repeatMode = RepeatMode.Reverse
                                        )
                                    )
                                }
                            }

                        }
                    },
                    modifier = Modifier
                        .size(100.dp)
                        .scale(scale.value)

                        .graphicsLayer {
                            // Example: Adjust the alpha based on some state or interaction
                            alpha = 0.5f
                        }// Apply animated scale
                        .background(
                            brush = animatedbrush,
                            shape = MaterialTheme.shapes.large,

                            ),
                ) {
                    AnimatedContent(
                        targetState = if (isRecording) R.drawable.pause else R.drawable.micro,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(1000)) togetherWith fadeOut(animationSpec = tween(1000))
                        }
                    ) { targetState ->
                        val icon = ImageVector.vectorResource(id = targetState)
                        Icon(
                            imageVector = icon,
                            contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                            tint = Color.White,

                            )
                    }

                }
            }



            Spacer(modifier = Modifier.height(8.dp))

            Box (
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(5.dp)
                    )
                    .padding(5.dp),

            ){

                Text(
                    "${hours.formatTime()}:${minutes.formatTime()}:${seconds.formatTime()}",
                    color = MaterialTheme.colorScheme.primaryContainer
                )
            }


            Spacer(modifier = Modifier.height(48.dp))



            Box (
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .graphicsLayer {
                        alpha = if (isRecording || (!isRecording && started)) 1f else 0f
                    },


                ){


                Button(onClick = {
                    isRecording = false
                    started = false
                    stopRecording()
                    elapsedSeconds = 0
                }, enabled = isRecording || (!isRecording && started)
                ) {
                    Text(getString(R.string.finish))
                }


            }





        }



    }

    @Composable
    fun Long.formatTime(): String {
        return if (this < 10) "0$this" else "$this"
    }


    private fun checkPermissions(activity: Activity): Boolean {
        Log.i("RecordButton", "Checking permissions")
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_CODE)
            return false
        }
        // Check if the permission is already available:
        /**
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // If not, request it from the user:
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
            return false
        }
        **/

        return true
    }



    private fun startRecording() {
        Log.i("RecordingStatus", "Started")
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

    private fun pauseRecording() {

        mediaRecorder?.pause() // Pauses the recording
        Log.i("RecordingStatus", "Paused")
    }

    private fun stopRecording() {
        Log.i("RecordingStatus", "Stopped")
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


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BottomNavigationBar(isAuthenticated: MutableState<Boolean>, showMakeSummaryDialog: Boolean) {
//initializing the default selected item
        var navigationSelectedItem by remember {
            mutableStateOf(0)
        }
        /**
         * by using the rememberNavController()
         * we can get the instance of the navController
         */
        val navController = rememberNavController()



//scaffold to hold our bottom navigation Bar
        Scaffold(

            

            modifier = Modifier
                .fillMaxSize()
                ,

            bottomBar = {

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.primary
                ) {


                    //getting the list of bottom navigation items for our data class
                    BottomNavigationItem().bottomNavigationItems().forEachIndexed {index,navigationItem ->

                        //iterating all items with their respective indexes
                        NavigationBarItem(
                            /*If our current index of the list of items
                             *is equal to navigationSelectedItem then simply
                             *The selected item is active in overView this
                             *is used to know the selected item
                             */
                            selected = index == navigationSelectedItem,

                            //Label is used to bottom navigation labels like Home, Search
                            label = {
                                Text(navigationItem.label, color = MaterialTheme.colorScheme.secondary)
                            },

                            // Icon is used to display the icons of the bottom Navigation Bar
                            icon = {
                                Icon(
                                    navigationItem.icon,
                                    contentDescription = navigationItem.label)
                            },
                            // used to handle click events of navigation items
                            onClick = {
                                navigationSelectedItem = index
                                navController.navigate(navigationItem.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer, // Custom color when item is selected
                                selectedIconColor = MaterialTheme.colorScheme.tertiary, // Custom icon color when item is selected
                                unselectedIconColor = MaterialTheme.colorScheme.secondary, // Custom icon color when item is unselected
                                selectedTextColor = Color.White, // Custom text color when item is selected
                                unselectedTextColor = Color.DarkGray, // Custom text color when item is unselected
                                disabledIconColor = MaterialTheme.colorScheme.secondary, // Custom badge color when item is selected
                                disabledTextColor = MaterialTheme.colorScheme.secondary // Custom badge color when item is unselected
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.background(Brushes.gradient1white(start = Offset(-500f, -500f), end = Offset(2000f, 2000f))))
            {

                NavHost(
                    navController = navController,
                    startDestination = Screens.Home.route,
                    modifier = Modifier.padding(paddingValues = paddingValues)) {
                    composable(Screens.Home.route) {
                        homePage()
                    }
                    composable(Screens.Summaries.route) {
                        summariesPage()
                    }
                    composable(Screens.Premium.route) {

                    }
                    composable(Screens.Settings.route) {
                        accountPage(isAuthenticated)
                    }
                }
            }


        }
    }






}

private suspend fun startGradientPulsingAnimation(brush: Animatable<Brush, AnimationVector4D>) {
    val gradientStart = Brush.radialGradient(
        colors = listOf(Color.Red, Color.Transparent)
    )
    val gradientEnd = Brush.radialGradient(
        colors = listOf(Color.Yellow, Color.Transparent)
    )

    brush.animateTo(
        targetValue = gradientEnd,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
}

private suspend fun stopGradientPulsingAnimation(brush: Animatable<Brush, AnimationVector4D>) {
    brush.snapTo(Brush.radialGradient(
        colors = listOf(Color.Gray, Color.Transparent)
    ))
}
private suspend fun startRecordingAnimation(scale: Animatable<Float, *>){
    scale.animateTo(
        targetValue = 1.1f,  // Scale up slightly
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                1.1f at 500
                1f at 1000
            }
        )
    )
}

// Animation for stopping the recording
private suspend fun stopRecordingAnimation(scale: Animatable<Float, *>){
    scale.snapTo(1f)  // Immediately return to normal scale
}


@Composable
fun remaining_minutes_counter(){
    
    var remainingMinutes by remember {
        mutableStateOf(0)
    }
    var myplan by remember {
        mutableStateOf("FREE")
    }
    S3Comms.fetchRemainingMinutesWithCallbacks(object :
        S3Comms.FetchMinutesCallback {
        override fun onSuccess(minutes: Int) {
            remainingMinutes = minutes
        }

        override fun onError(e: Exception) {
            // Handle error, possibly update UI to show error message
            Log.e("FetchError", "Error fetching minutes: ${e.message}")
        }
    })

    
    S3Comms.fetchPlanWithCallbacks(object :
        S3Comms.FetchPlanCallback {
        override fun onSuccess(plan: String) {
            myplan = plan
        }

        override fun onError(e: Exception) {
            // Handle error, possibly update UI to show error message
            Log.e("FetchError", "Error fetching minutes: ${e.message}")
        }
    })


    var totalMinutes = 200
    if(myplan == "PREMIUM") {
        totalMinutes = 1000
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 50.dp),
        contentAlignment = Alignment.BottomCenter
    ){
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val progress = remainingMinutes.toFloat()/totalMinutes.toFloat()
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(10.dp),
                strokeCap = StrokeCap.Round
                )
            Spacer(modifier = Modifier.height(10.dp))

            val annotatedString = buildAnnotatedString {
                append(LocalContext.current.getString(R.string.mm_min_remaining_1))

                var remcolor = MaterialTheme.colorScheme.onSecondary
                if(remainingMinutes < 60){
                    remcolor = MaterialTheme.colorScheme.tertiary
                }
                withStyle(style = SpanStyle(color = remcolor)) {
                    append(remainingMinutes.toString())
                }

                append(LocalContext.current.getString(R.string.mm_min_remaining_2))

                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSecondary)) {
                    append(totalMinutes.toString())
                }

                append(".")
            }

            Text(
                text = annotatedString,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(alignment = Alignment.CenterHorizontally)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(vertical = 2.dp, horizontal = 10.dp),

            )
        }
    }
}

