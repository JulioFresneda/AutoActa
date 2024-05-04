package com.logicallynx.autoacta

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Text
import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult
import com.amplifyframework.core.Amplify

@Composable
fun settingsPage(isAuthenticated: MutableState<Boolean>){
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        SignOutButton(isAuthenticated)
    }

}


@Composable
fun SignOutButton(isAuthenticated: MutableState<Boolean>) {
    Button(onClick = {
        Amplify.Auth.signOut { signOutResult ->
            when(signOutResult) {
                is AWSCognitoAuthSignOutResult.CompleteSignOut -> {
                    // Sign Out completed fully and without errors.
                    Log.i("AuthQuickStart", "Signed out successfully")
                    isAuthenticated.value = false
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
    }) {
        Text("Sign Out")
    }
}