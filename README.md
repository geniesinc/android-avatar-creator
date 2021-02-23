# Genies Composer Library and Sample
This repository is a library and sample integration of the Genies 3D Composer in a native Android App.

## Library integration - gradle

1. [Apply for a Partner Account and get your `authToken`](https://geniesinc.github.io/#step-one-apply-for-a-partner-account)
2. Open gradle.properties file
   - Add
   ```
   authToken=YOUR_AUTH_TOKEN
   ```
2. Open build.gradle (project) file
   - Add the following to allprojects / repositories
```
  maven {
            url 'https://jitpack.io'
            credentials { username authToken }
        }
```
3. Open build.gradle (Module: app) file
   - Add to dependencies
   ```
   implementation 'com.github.geniesinc:android-composer-library:0.0.12'
   ```
4. The composer needs to be loaded in a separate activity with a dedicated process attached. The composer kills the current process when it unloads.
```
        <activity
            android:name="com.genies.sample.GeniesComposerActivity"
            android:label="@string/app_name"
            android:launchMode="singleTask"
            android:configChanges="mcc|mnc|locale|touchscreen|keyboard|keyboardHidden|navigation|orientation|screenLayout|uiMode|screenSize|smallestScreenSize|fontScale|layoutDirection|density" android:hardwareAccelerated="false"
            android:process=":genies_composer" // important
            android:screenOrientation="fullSensor"/>
```
5. Open strings.xml and add:
```
<string name="game_view_content_description">Genies Composer</string>
```
6. In the activity, to add the composer view:
```
 val composerCallback = object: GeniesComposerLoader.ComposerCallback {
      override fun onComposerLoaded() {
          Log.d(TAG, "Composer loaded")
          pb_loading.visibility = View.GONE
      }

      override fun onComposerUnloaded() {
          Log.d(TAG, "Composer unloaded")
      }
}

GeniesComposerLoader.loadGeniesComposerIntoView(
   parentView = composer_container,
   lifecycleOwner = this,
   credentialsJson = testCredentials,
   composerCallback = composerCallback
)
```
## Example
An example of the Genies composer integration can be found in [GeniesComposerActivity](https://github.com/geniesinc/android-composer-library/blob/master/sample/src/main/java/com/genies/sample/GeniesComposerActivity.kt)
