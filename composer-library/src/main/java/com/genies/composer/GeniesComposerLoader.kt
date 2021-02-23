package com.genies.composer

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.unity3d.player.IUnityPlayerLifecycleEvents
import com.unity3d.player.UnityPlayer

object GeniesComposerLoader : IUnityPlayerLifecycleEvents, LifecycleObserver {
    private var TAG = "GeniesComposerLoader"

    private var unityPlayer: UnityPlayer? = null
    private var parentView: ViewGroup? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private val composerMessages: ArrayList<GeniesComposerMessage> = arrayListOf()
    private var composerCallback: ComposerCallback? = null
    private var loadingOverlayView: View? = null

    /**
     * Create a [UnityPlayer] and add it to the parent view
     *
     * @param parentView The parent container for the [UnityPlayer]
     * @param lifecycleOwner The [LifecycleOwner]
     * @param credentialsJson a json object with 3 string params (userId, refreshToken, apiKey)
     */
    fun loadGeniesComposerIntoView(
        parentView: ViewGroup,
        lifecycleOwner: LifecycleOwner,
        credentialsJson: String? = null,
        composerCallback: ComposerCallback? = null
    ) {
        if (isInitialized()) {
            showComposer()
            Log.d(TAG, "Unity player already added.")
            return
        }
        Log.d(TAG, "Loading unity player")
        GeniesComposerLoader.lifecycleOwner = lifecycleOwner
        GeniesComposerLoader.composerCallback = composerCallback
        GeniesComposerLoader.parentView = parentView

        GeniesComposerLoader.lifecycleOwner?.lifecycle?.addObserver(this)

        // Init unity player
        unityPlayer = UnityPlayer(parentView.context, this)

        // Add player to parent view
        parentView.addView(unityPlayer?.view, parentView.layoutParams)

        // Add the loading view on top
        loadingOverlayView = createLoadingView(parentView.context)
        parentView.addView(loadingOverlayView, parentView.layoutParams)

        unityPlayer?.requestFocus()
        unityPlayer?.windowFocusChanged(true)

        if (credentialsJson != null) {
            sendComposerMessage(
                GeniesComposerMessage(
                objectName = "Client",
                methodName = "LoadApiSdkPartnerLogin",
                messageBody = credentialsJson
            ))
        }

        sendAllComposerMessages()

        // Hide the composer view under the loadingOverlayView until [composerFinishedLoading] is called
        hideComposer()
    }

    private fun createLoadingView(context: Context): View {
        return View(context).apply {
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
        }
    }

    /**
     * Get the [UnityPlayer] instance
     *
     * @return
     */
    fun getUnityPlayer(): UnityPlayer {
        checkInitialized()
        return unityPlayer!!
    }

    /**
     *  Set the Unity view [View.VISIBLE]
     */
    fun showComposer() {
        checkInitialized()
        // hide the loading overlay view
        loadingOverlayView?.visibility = View.GONE
        sendAllComposerMessages()
    }

    /**
     *  Set the Unity view [View.GONE]
     */
    fun hideComposer() {
        checkInitialized()
        // show the loading overlay view
        loadingOverlayView?.visibility = View.VISIBLE
    }

    /**
     *  Hide the unity view and disposes of the [UnityPlayer]
     */
    @JvmStatic
    fun unload() {
        // Do the unload on the UI thread
        Handler(Looper.getMainLooper()).post {
            Log.d(TAG, "unload")
            // Hide the view
            hideComposer()

            // Call the view unloaded listener
            composerCallback?.onComposerUnloaded()

            // Quit unity player
            unityPlayer?.quit()

            // Remove lifecycle observer
            lifecycleOwner?.lifecycle?.removeObserver(this)

            // Remove the view listener
            composerCallback = null
        }
    }

    /**
     * Called by [UnityPlayer] when composer finishes loading
     */
    @JvmStatic
    fun composerFinishedLoading() {
        // Do the unload on the UI thread
        Log.d(TAG, "composer finished loading")
        Handler(Looper.getMainLooper()).post {
            showComposer()
            composerCallback?.onComposerLoaded()
        }
    }

    /**
     * Send a message to [UnityPlayer]
     */
    fun sendComposerMessage(message: GeniesComposerMessage) {
        if (isInitialized())
            UnityPlayer.UnitySendMessage(
                message.objectName,
                message.methodName,
                message.messageBody
            )
        else
            composerMessages += message
    }

    /**
     * Send all the cached messages
     */
    private fun sendAllComposerMessages() {
        if (isInitialized() && composerMessages.isNotEmpty()) {
            composerMessages.forEach { message ->
                UnityPlayer.UnitySendMessage(
                    message.objectName,
                    message.methodName,
                    message.messageBody
                )
            }
        }
    }

    /**
     *  Check if the [UnityPlayer] is initialised
     */
    fun isInitialized(): Boolean {
        return unityPlayer != null
    }

    /**
     * Check if the [UnityPlayer] is initialised and throws an Exception otherwise
     */
    private fun checkInitialized() {
        if (!isInitialized())
            throw Exception("Unity player not initialised. loadUnityPlayerIntoView needs to be called first")
    }


    override fun onUnityPlayerUnloaded() {

    }

    // When Unity player quited kill process
    override fun onUnityPlayerQuitted() {
//        Process.killProcess(Process.myPid())
    }

    /**
     *  Resume the [UnityPlayer] and request focus
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resumeUnity() {
        unityPlayer?.resume()
        unityPlayer?.requestFocus()
        unityPlayer?.windowFocusChanged(true)
    }


    /**
     *  Pause the [UnityPlayer]
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun pauseUnity() {
        unityPlayer?.pause()
    }

    /**
     *  When parent lifecycle is destroyed, unload [UnityPlayer]
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroyUnity() {
        unload()
    }

    interface ComposerCallback {
        // Called when the Composer view is first loaded and shown on screen
        fun onComposerLoaded()
        // Called when the Composer view is unloaded
        fun onComposerUnloaded()
    }

}