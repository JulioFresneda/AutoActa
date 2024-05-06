package com.logicallynx.autoacta

import S3Comms.downloadPdf
import S3Comms.listSummaries
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import com.logicallynx.autoacta.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun SearchableSummaryList(
    summaries: List<S3Comms.JobConfig>,
    onSelectSummary: (S3Comms.JobConfig?) -> Unit,
    onShowDialog: (Boolean) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var selectedSummary by remember { mutableStateOf<S3Comms.JobConfig?>(null) }

    BackHandler {
        // Activate the button when the "Go Back" gesture is triggered
        showDialog = false
        selectedSummary = null
    }



    // Example: Trigger callback when selectedSummary changes
    LaunchedEffect(selectedSummary) {
        onSelectSummary(selectedSummary)
    }
    LaunchedEffect(selectedSummary) {
        onShowDialog(showDialog)
    }

    // Filtered list based on search text
    val filteredSummaries = summaries.filter {
        it.jobName.contains(searchText, ignoreCase = true) ||
        // Check if any of the tags contain the searchText
        it.tags.any { tag ->
            tag.contains(searchText, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text(LocalContext.current.getString(R.string.search)) },
            singleLine = true,
            
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                // Optionally handle done action, like hiding the keyboard
            })
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredSummaries) { summary ->
                SummaryElevatedCard(
                    item = summary,
                    onClickSummary = {
                        item -> selectedSummary = item
                        onShowDialog(true)
                        showDialog = true
                        Log.i("SummaryClicked", "Summary clicked: " + item.jobName + " " + showDialog.toString())
                    }
                )
            }
        }
    }
}


@Composable
fun summariesPage(){
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val summaries = remember { mutableStateListOf<S3Comms.JobConfig>() }
    var isLoading by remember { mutableStateOf(true) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedSummary by remember { mutableStateOf<S3Comms.JobConfig?>(null) }






    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                Log.i("Summaries", "Loading summaries")
                isLoading = true
                val fetchedSummaries = listSummaries(context)
                Log.i("Summaries", "Got summaries")
                summaries.clear()
                summaries.addAll(fetchedSummaries)
                isLoading = false
            } catch (e: Exception) {
                // Handle any errors appropriately
                isLoading = false
            }


        }


    }



    Box(modifier = Modifier.fillMaxSize()) {
        SearchableSummaryList(
            summaries = summaries,

            onSelectSummary = { summary ->
                selectedSummary = summary

            },
            onShowDialog = { sd -> showDialog = sd}

        )

        Log.i("SummaryClicked", "Clicked from Searchable " + selectedSummary?.jobName + " " + showDialog.toString())


        if (isLoading) {
            var progress by remember { mutableFloatStateOf(0.1F) }

            LaunchedEffect(key1 = Unit) {
                for (i in 1..100) {
                    progress = i.toFloat()/100F
                    delay(50)
                }
            }

            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
                strokeWidth = 10.dp,
                strokeCap = StrokeCap.Round
            )
        }
    }

    if (showDialog && selectedSummary != null) {
        Log.i("SummaryClicked", "Showing dialog")
        SummaryDetails(showDialog = {showDialog = it}, selectedSummary = selectedSummary!!)

    }
    else{
        Log.i("SummaryClicked", showDialog.toString() + " " + selectedSummary?.jobName)
    }


}



@Composable
fun SummaryCategory(title: String, content: String){

    /**
    val subcatbrush = Brushes.gradient3white(
        start = Offset(0f, 0f),
        end = Offset.Infinite,
        centerColor = MaterialTheme.colorScheme.primary
    )**/

    Spacer(modifier = Modifier.height(16.dp))
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth(0.89f)


    ){
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary // Ensure text color contrasts well with the background
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    if(content != ""){
        Column(
            modifier = Modifier
                .background(color = Color.White, shape = RoundedCornerShape(10.dp))
                .fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.Start

        ){
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(10.dp)

            )
        }
    }



}



@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SummaryDetails(showDialog: (Boolean) -> Unit, selectedSummary: S3Comms.JobConfig){

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .zIndex(1f)
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .padding(vertical = 30.dp),
        contentAlignment = Alignment.TopCenter,
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ){

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .fillMaxWidth(fraction = 0.9f)


                ){
                Text(
                    text = selectedSummary.jobName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.secondary // Ensure text color contrasts well with the background
                )
            }


            SummaryCategory(LocalContext.current.getString(R.string.s_desc), selectedSummary.description)
            SummaryCategory(LocalContext.current.getString(R.string.s_date), formatDateString(selectedSummary.date))
            SummaryCategory(LocalContext.current.getString(R.string.s_email), selectedSummary.email)
            SummaryCategory(LocalContext.current.getString(R.string.s_tags), selectedSummary.tags.joinToString(separator = ", "))

            SummaryCategory(LocalContext.current.getString(R.string.s_share), "")

            var found_pdf by remember {mutableStateOf(true) }
            var found_docx by remember {mutableStateOf(true) }


            Row(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .fillMaxWidth(0.9f)
                    .align(Alignment.CenterHorizontally),


            ){

                Box(modifier = Modifier.padding(horizontal = 4.dp).background(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(10.dp))){

                    IconButton(
                        onClick = {
                            SendButton(context = context, jobName = selectedSummary.jobName, type="pdf", found = {found_pdf = it})

                        }

                        ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.pdf_icon),
                            contentDescription = "Share PDF",
                            tint = MaterialTheme.colorScheme.primaryContainer,



                            )
                    }


                }

                Box(modifier = Modifier.padding(horizontal = 4.dp).background(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(10.dp))) {
                    IconButton(onClick = {
                        SendButton(
                            context = context,
                            jobName = selectedSummary.jobName,
                            type = "docx",
                            found = {found_docx = it}
                        )
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.docx_icon),
                            contentDescription = "Share docx",
                            tint = MaterialTheme.colorScheme.primaryContainer,

                            )
                    }

                }
                Box(modifier = Modifier.padding(horizontal = 4.dp).background(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(10.dp))) {
                    var found by remember {mutableStateOf(false) }
                    IconButton(onClick = {
                        SendButton(
                            context = context,
                            jobName = selectedSummary.jobName,
                            type = "wav",
                            found = { found = it }
                        )
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.wav_icon),
                            contentDescription = "Share wav",
                            tint = MaterialTheme.colorScheme.primaryContainer,

                            )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            if(!found_pdf){
                Toast.makeText(context, LocalContext.current.getString(R.string.toast_pdf_not_ready), Toast.LENGTH_SHORT).show()
            }
            if(!found_docx){
                Toast.makeText(context, LocalContext.current.getString(R.string.toast_docx_not_ready), Toast.LENGTH_SHORT).show()
            }



        }

        Box(
            modifier = Modifier
                .wrapContentHeight()
                .align(Alignment.BottomCenter)
                .padding(30.dp),
            contentAlignment = Alignment.BottomCenter,
        ){
            Row {







                TextButton(
                    onClick = {
                        showDialog(false)

                    },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                ) {

                    Text(LocalContext.current.getString(R.string.close))
                }
            }

        }

    }


}


fun SendButton(context: Context, jobName: String, type: String, found: (Boolean) -> Unit){

    val localPath = "${context.filesDir}/${jobName}.${type}"

    val file = File(context.filesDir, "${jobName}.${type}")
    val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

    if(type == "wav"){
        downloadPdf("${jobName}/${jobName}.${type}", localPath, {
            val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", File(localPath))
            sharePdf(context, fileUri)
            found(true)
        }, {
            found(false)
        })
    }
    else{
        downloadPdf("${jobName}/${jobName}_summary.${type}", localPath, {
            val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", File(localPath))
            sharePdf(context, fileUri)
            found(true)
        }, {
            found(false)
        })
    }


}


fun sharePdf(context: Context, fileUri: Uri) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, fileUri)
        type = "application/pdf"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share PDF via"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryElevatedCard(item: S3Comms.JobConfig, onClickSummary: (S3Comms.JobConfig) -> Unit) {
    var cardColors = CardDefaults.elevatedCardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.secondary

    )


    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,

        ),

        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .padding(10.dp)
            ,
        onClick = { onClickSummary(item)},
        colors = cardColors


    ) {
        Column(modifier = Modifier.padding(horizontal = 6.dp)) {

            CardTitle(text = item.jobName)
            
            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.padding(horizontal = 6.dp)){
                CardField(LocalContext.current.getString(R.string.s_date), formatDateString(item.date))

                Spacer(modifier = Modifier.height(5.dp))
                CardField(LocalContext.current.getString(R.string.s_email), item.email)
                Spacer(modifier = Modifier.height(5.dp))


                CardField(LocalContext.current.getString(R.string.s_tags), item.tags.joinToString(separator = ", "))
                Spacer(modifier = Modifier.height(12.dp))
            }



        }
        
        //SendPdfButton(pdfUri = "")
    }
}

fun formatDateString(dateStr: String): String {
    // Define the original formatter with milliseconds
    val originalFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    // Parse the original date string
    val dateTime = LocalDateTime.parse(dateStr, originalFormat)

    // Define the new formatter without milliseconds
    val newFormat = DateTimeFormatter.ofPattern("dd/mm/yyyy HH:mm")
    // Format the LocalDateTime object to the new format
    return dateTime.format(newFormat)
}

@Composable fun CardField(title: String, content: String){
    Row {
        Column(modifier = Modifier.weight(1f)){
            CardHeader(text = title)

        }
        Column(modifier = Modifier.weight(5f), horizontalAlignment = Alignment.Start) {


            CardContent(text = content)



        }
    }


}



@Composable
fun CardTitle(text: String){
    Spacer(modifier = Modifier.height(6.dp))
    Box (
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(10.dp)
            )
            .fillMaxWidth()
            .padding(5.dp),

        ){

            Text(
                text = text,
                Modifier.padding(horizontal = 10.dp),
                style = MaterialTheme.typography.titleMedium
            )
    }

}
@Composable
fun CardHeader(text: String){
    Text(
        text = text,
        Modifier.padding(horizontal = 5.dp),
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
fun CardContent(text: String){
    Box (
        modifier = Modifier
            .background(
                color = Color.White,
                shape = RoundedCornerShape(5.dp)
            )
            .padding(5.dp)

        ){

        Text(
            text = text,
            Modifier.padding(horizontal = 2.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }

}



