package com.logicallynx.autoacta

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.ContentAlpha
import androidx.wear.compose.material.Text
import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult
import com.amplifyframework.core.Amplify
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDropdown(onLanguageSelected: (LanguageOption) -> Unit) {
    // Sample language options
    val languages = Locale.getAvailableLocales().map { locale ->
        LanguageOption(locale.language,
            locale.getDisplayLanguage(Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
    }.distinctBy { it.code }.sortedBy { it.name }

    // Get the current device language
    val defaultLanguageCode = Locale.getDefault().language

    // Find the matching LanguageOption for the device's default language
    val defaultLanguageOption = languages.find { it.code == LanguageSelected.language }
        ?: LanguageOption("", "Select Language")

    var selectedLanguage by remember { mutableStateOf(defaultLanguageOption) }
    var expanded by remember { mutableStateOf(false) }

    var exposedColors = ExposedDropdownMenuDefaults.textFieldColors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        cursorColor = Color.Black,
        focusedLabelColor = Color.Black, // Color for the label when the TextField is focused
        unfocusedLabelColor = Color.Black.copy(alpha = ContentAlpha.disabled), // Color for the label when the TextField is unfocused

    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {  },

    ) {
        TextField(
            value = selectedLanguage.name,
            onValueChange = { },
            readOnly = true, // Makes the TextField non-editable
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ArrowDropDown else Icons.Filled.ArrowDropDown,
                        contentDescription = if (expanded) "Close menu" else "Open menu"
                    )
                }
            },
            colors = exposedColors,
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { language ->
                DropdownMenuItem(
                    modifier = Modifier.background(color = Color.White),
                    text = { Text(language.name, color = Color.Black)},
                    onClick = {
                        selectedLanguage = language
                        onLanguageSelected(language)
                        expanded = false
                    }
                )
            }
        }
    }
}
fun openEmailApp(context: Context, emailAddress: String, subject: String) {
    try {

        // Create an email intent
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$emailAddress")  // Set the data as a mailto: URI
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }

        // Verify there's an email app available to handle this intent
        if (intent.resolveActivity(context.packageManager) != null) {
            Log.i("MAIL", "Seems OK.")
            context.startActivity(intent)
        } else {
            Log.e("MAIL", "No email client is available to handle the intent.")
        }
    } catch (e: Exception) {
        Log.e("MAIL", "Error launching email intent: ${e.message}")
    }
}

object LanguageSelected {
    var language: String? = Locale.getDefault().language
}


@Composable
fun accountPage(isAuthenticated: MutableState<Boolean>){
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            UpgradePremiumBox()


            Spacer(modifier = Modifier.height(40.dp))

            SelectLanguageBox()
            Spacer(modifier = Modifier.height(40.dp))
            SignOutButton(isAuthenticated)
        }

    }

}

@Composable
fun SelectLanguageBox(){
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
        Column (horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(0.7f).padding(20.dp)){
            Text(
                text = LocalContext.current.getString(R.string.aa_lang),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            LanguageDropdown {
                LanguageSelected.language = it.code
            }
        }




    }
}

@Composable
fun UpgradePremiumBox(){
    val context = LocalContext.current
    Box(

        modifier = Modifier
            .background(
                color = Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),


                )
            .clickable {
                // Trigger an Intent to open the email app
                openEmailApp(context, "contact@autoacta.com", "I want the premium plan!")
            }
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
                .fillMaxWidth(0.7f),
            horizontalAlignment = Alignment.CenterHorizontally,

            )
        {
            val typewriterFontFamily = FontFamily(
                Font(R.font.texgyrecursor_regular),
                Font(R.font.texgyrecursor_bold)// Use the resource identifier for your font
            )
            val typewriterFontStyle = TextStyle(
                fontFamily = typewriterFontFamily,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary,  // Specify the text color
                letterSpacing = 0.5.sp,  // Adjust letter spacing
                lineHeight = 22.sp  // Adjust line height
            )
            Text(
                text = LocalContext.current.getString(R.string.acc_upgrade),
                style = typewriterFontStyle
            )
            Spacer(modifier = Modifier.height(10.dp))

            val annotatedString = buildAnnotatedString {
                append(LocalContext.current.getString(R.string.acc_upgrade_desc))

                withStyle(style = SpanStyle(color = Color.Blue)) {
                    append("contact@autoacta.com")
                }

                append(".")
            }
            Text(
                text = annotatedString,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

        }

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
        Text(LocalContext.current.getString(R.string.signout))
    }
}