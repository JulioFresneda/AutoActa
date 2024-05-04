// Authenticator.kt

package com.logicallynx.autoacta

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.AuthProvider
import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult
import com.amplifyframework.core.Amplify
import com.amplifyframework.ui.authenticator.SignInState
import com.amplifyframework.ui.authenticator.forms.FieldKey
import com.amplifyframework.ui.authenticator.ui.SignInFooter
import com.logicallynx.autoacta.Brushes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.logicallynx.myapp.R

// Import other necessary dependencies

// If Authenticator needs any parameters, add them here
@Composable
fun SignInForm(state: SignInState, scope: CoroutineScope, SetUpLoginWithGoogle: () -> Unit) {
    Column {
        Column (modifier = Modifier
            .background(brush = Brushes.gradient4(
                start = Offset.Zero,
                end = Offset.Infinite,
                alpha = 1f
            ) ).fillMaxWidth().padding(vertical = 80.dp).wrapContentHeight(),
            verticalArrangement = Arrangement.Center, // Center items vertically
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            val typewriterFontFamily = FontFamily(
                Font(R.font.texgyrecursor_regular),
                Font(R.font.texgyrecursor_bold)// Use the resource identifier for your font
            )

            val typewriterFontStyle = TextStyle(
                fontFamily = typewriterFontFamily,
                fontSize = 30.sp,
                brush = Brushes.gradient5(
                    start = Offset.Zero,
                    end = Offset.Infinite,
                    alpha = 0.9f
                ),


                letterSpacing = 0.5.sp,  // Adjust letter spacing
                lineHeight = 50.sp  // Adjust line height
            )



            Text("Welcome to AutoActa!", style = typewriterFontStyle)
        }
        Column(
            modifier = Modifier
                .fillMaxSize() // Fill the max size of the parent
                .padding(16.dp), // Add padding around the Column
            verticalArrangement = Arrangement.Center, // Center items vertically
            horizontalAlignment = Alignment.CenterHorizontally // Center items horizontally
        ) {
            val email = state.form.fields[FieldKey.Email]!!.state.content
            val password = state.form.fields[FieldKey.Password]!!.state.content




            Spacer(modifier = Modifier.height(40.dp))
            Text("Sign in or create an account", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(16.dp))

            // Use a nicer looking TextField, Material Design by default
            OutlinedTextField(
                value = email,
                onValueChange = { newValue ->
                    // Ensure the new value is updated properly in your state management
                    state.form.fields[FieldKey.Email]!!.state.content = newValue
                },
                label = { Text("Email") },
                singleLine = true, // Makes the TextField a single line input
                modifier = Modifier.fillMaxWidth(0.8f) // Use 80% of the width
            )

            Spacer(modifier = Modifier.height(16.dp)) // Add space between the text fields

            OutlinedTextField(
                value = password,
                onValueChange = { newValue ->
                    // Ensure the new value is updated properly in your state management
                    state.form.fields[FieldKey.Password]!!.state.content = newValue
                },
                label = { Text("Password") },
                singleLine = true, // Makes the TextField a single line input
                visualTransformation = PasswordVisualTransformation(), // Hides the password input
                modifier = Modifier.fillMaxWidth(0.8f) // Use 80% of the width
            )

            Spacer(modifier = Modifier.height(24.dp)) // Add space between the TextField and Button

            Button(
                onClick = { scope.launch { state.signIn() } },
                modifier = Modifier.align(Alignment.CenterHorizontally) // Center the button
            ) {
                Text("Sign In")
            }

            // If you have a footer, you can place it here,
            // or consider customizing it further as needed.
            SignInFooter(state)

            Spacer(modifier = Modifier.height(8.dp))




            IconButton(
                onClick = { SetUpLoginWithGoogle()},
                modifier = Modifier.align(Alignment.CenterHorizontally).size(width = 183.dp, height = 40.dp))
                {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.android_light_rd_si),
                        contentDescription = "Sign in with google",
                        tint = Color.Unspecified
                    )
                }


        }
    }

}



object AuthenticatorScreen {
    fun SetUpLoginWithGoogle(activity: Activity, isAuthenticated: MutableState<Boolean> ) {
        Amplify.Auth.fetchAuthSession(
            { result ->
                if (!result.isSignedIn) {
                    // No user is signed in, proceed with the sign-in process
                    try {
                        Amplify.Auth.signInWithSocialWebUI(
                            AuthProvider.google(),
                            activity,
                            {
                                isAuthenticated.value = true
                            },
                            {
                                // Handle sign-in failure
                                Log.e("AuthQuickstart", "Sign in failed", it)
                                Amplify.Auth.signOut { signOutResult ->
                                    when(signOutResult) {
                                        is AWSCognitoAuthSignOutResult.CompleteSignOut -> {
                                            // Sign Out completed fully and without errors.
                                            Log.i("AuthQuickStart", "Signed out successfully")

                                        }
                                        is AWSCognitoAuthSignOutResult.PartialSignOut -> {
                                            // Sign Out completed with some errors. User is signed out of the device.
                                            signOutResult.hostedUIError?.let {
                                                Log.e("AuthQuickStart", "HostedUI Error", it.exception)
                                                // Optional: Re-launch it.url in a Custom tab to clear Cognito web session.

                                            }
                                            signOutResult.globalSignOutError?.let {
                                                Log.e("AuthQuickStart", "GlobalSignOut Error", it.exception)
                                                // Optional: Use escape hatch to retry revocation of it.accessToken.
                                            }
                                            signOutResult.revokeTokenError?.let {
                                                Log.e("AuthQuickStart", "RevokeToken Error", it.exception)
                                                // Optional: Use escape hatch to retry revocation of it.refreshToken.
                                            }
                                        }
                                        is AWSCognitoAuthSignOutResult.FailedSignOut -> {
                                            // Sign Out failed with an exception, leaving the user signed in.
                                            Log.e("AuthQuickStart", "Sign out Failed", signOutResult.exception)
                                        }
                                    }
                                }


                            }
                        )
                    } catch (error: AmplifyException) {
                        // Log initialization failure
                        Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
                    }
                } else {
                    // User is already signed in, handle accordingly
                    Log.i("MyAmplifyApp", "User is already signed in.")
                    // Here, you can redirect the user to the main activity or refresh the current activity
                }
            },
            { error ->
                // Handle error in fetching the auth session
                Log.e("MyAmplifyApp", "Error fetching auth session", error)
            }
        )
    }

    fun isUserSignedIn(callback: (Boolean) -> Unit) {
        Amplify.Auth.fetchAuthSession(
            { result ->
                callback(result.isSignedIn)
            },
            { error ->
                // Handle error, for simplicity we return false here
                // but you should handle errors appropriately in your real application
                callback(false)
            }
        )
    }


}


