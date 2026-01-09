package owd.foldersync.client.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.documentfile.provider.DocumentFile
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp

import owd.foldersync.client.android.ui.theme.FolderSyncTheme

class MainActivity : ComponentActivity() {

    private val pickFolderLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if(uri != null)
            {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                writeToSharedFolder(uri)
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            FolderSyncTheme {
//                PickFolderScreen {
//                    pickFolderLauncher.launch(null)
//                }
                ServerScreen()
            }
        }
    }

    @Composable
    fun ServerScreen() {
        var message by remember { mutableStateOf("Idle") }

        Column(modifier = Modifier.padding(16.dp)) {

            Button(onClick = {
                message = "Connecting..."
                connectToServer {
                    result ->
                    message = result
                }
            }) {
                Text("Check server")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = message)
        }
    }

    private fun connectToServer(onResult: (String) -> Unit) {

        Thread {
            try {
                val url = java.net.URL("https://example.com")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 3000
                connection.readTimeout = 3000
                connection.requestMethod = "GET"

                val code = connection.responseCode
                connection.disconnect()

                runOnUiThread {
                    onResult("Success! HTTP $code")
                }
            } catch (e: Exception) {
                onResult("ERROR: ${e.message}")
            }
        }.start()
    }

    @Composable
    fun PickFolderScreen(onPickFolder: () -> Unit) {
        Scaffold { padding ->
            androidx.compose.material3.Button(
                onClick = onPickFolder,
                modifier = Modifier.padding(padding)
            ) {
                Text("Select storage folder")
            }

        }

    }

    private fun writeToSharedFolder(treeUri: Uri) {

        val rootFolder = DocumentFile.fromTreeUri(this, treeUri) ?: return

        val folderSync = rootFolder.findFile("FolderSync")
            ?: rootFolder.createDirectory("FolderSync")
            ?: return

        val folder000 = folderSync.findFile("folder_000")
            ?: folderSync.createDirectory("folder_000")
            ?: return

        val testFile = folder000.createFile("text/plain",
            "test.txt") ?: return

        contentResolver.openOutputStream(testFile.uri)?.use { out ->
            out.write("Hello from Android SAF".toByteArray())
        }

        Toast.makeText(
            this,
            "File written to FolderSync/folder_000",
            Toast.LENGTH_LONG
        ).show()
    }
}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}