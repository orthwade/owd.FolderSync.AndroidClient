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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.documentfile.provider.DocumentFile
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
                PickFolderScreen {
                    pickFolderLauncher.launch(null)
                }
            }
        }
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

        val testFile = folder000.createFile("text/plain", "test.txt") ?: return

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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FolderSyncTheme {
        Greeting("Android")
    }
}