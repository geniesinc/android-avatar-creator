package com.genies.sample

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.genies.composer.ComposerCredentials
import com.genies.composer.GeniesComposerLoader
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_composer.*

const val TAG = "GeniesComposerActivity"
class GeniesComposerActivity : AppCompatActivity(R.layout.activity_composer) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val testCredentials = Gson().toJson(provideTestComposerCredentials())

        val composerCallback = object: GeniesComposerLoader.ComposerCallback {
            override fun onComposerLoaded() {
                Log.d(TAG, "Composer loaded")
                pb_loading.visibility = View.GONE
            }

            override fun onComposerUnloaded() {
                Log.d(TAG, "Composer unloaded")
                // When the unity view is unloaded, close the activity
                finish()
            }

        }

        GeniesComposerLoader.loadGeniesComposerIntoView(
            parentView = composer_container,
            lifecycleOwner = this,
            credentialsJson = testCredentials,
            composerCallback = composerCallback
        )

    }

    /**
     * For more information about Composer Credentials see https://geniesinc.github.io/avatar_creator/#integration
     */
    private fun provideTestComposerCredentials() = ComposerCredentials(
        userId = "YOUR_USER_ID",
        refreshToken = "YOUR_REFRESH_TOKEN",
        apiKey = "YOUR_API_KEY"
    )

}
