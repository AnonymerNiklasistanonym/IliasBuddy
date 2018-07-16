# IliasBuddy
Uses the private Ilias RSS feed to notify you if there are new posts/entries on Ilias

## Development

### Help/Ideas
I 100% appreciate it if you would take some of your time and make issues on how to make this application better if you have any ideas or problems :)

### Edit & Build directly on a device
Open the directory with Android Studio v3.1 with an installed Android SDK bigger than v23 (Android 6+).
You can then build it with gradle on an external device via USB-Debugging or run it on an internal Android Virtual Device (which you need to install to).

### Build/Create signed `.apk` for distribution (using Android Studio v3.1)
1. Select `Build` in the menu bar
2. Select in the new menu `Generate Signed APK`
   1. If you not already created a key select `Create new...`
      1. Choose a path and file name by clicking `...`
      2. Enter a password for the key store path
      3. Enter key alias specifically for this app
      4. Enter another key password specifically for this app 
      5. Enter your a name
      6. Select `OK`
   2. Otherwise select the `.jks` file vie `Choose existing...`
3. Then click `Next` and select `release` as Build Type
4. Select the checkbox `V1 (Jar Signature)` (and `V2` - was not necessary when I tried it)
5. Select `Finish` and find your generated signed APK in the `\IliasBuddy\app` directory

### Important hint
If you want to install the app via a created `.apk` on a device where you already built the app via USB-debugging this won't work.
Because the sign has changed an *update* is not possible which means you need to uninstall the app before installing it again via the `.apk` file. The same problem probably also occurs in the opposite direction.
This exist to be sure that nobody can take this code add malicious things and upload the `.apk` file to update the existing app with his malicious code. As long as the app installs via the release `.apk` you can be sure that I built it and nobody else.

## FAQ

### You just want my Ilias credentials, how can I trust you
There are the following options:
1. The password and user name would only help me in reading your private RSS feed in which nothing like emails, feedbacks or results are contained and knowing your name - I would still not be able to access anything else than the feed on your Ilias account plus you could always change your feed URL through which I would loose any access
1. Read the code and understand that none of your credentials ever leave your device
1. You still think that there is a possibility that the created installation file (`.apk`) has malicious code in it which abuses your credentials - Good on you for thinking about this and here is my solution: Don't trust me and build the `.apk` yourself which would mean you can read the source code and be after the build 100% sure that the app is in no way malicious or abuses your credentials.

### How can I get the needed credentials for this app to access my private Ilias RSS feed
You app need three things:
- Private Ilias feed URL
- Private Ilias feed user name
- Private Ilias feed password

1. Open Ilias [feed page](https://ilias3.uni-stuttgart.de/ilias.php?view=0&col_side=right&block_type=pdnews&cmd=showFeedUrl&cmdClass=ilpdnewsblockgui&cmdNode=sh:6b:rv&baseClass=ilPersonalDesktopGUI#il_mhead_t_focus)
2. Copy/Save the private feed URL
3. Extract your user name from the URL (It is your name without spaces at the beginning: 'MaxMueller')
4. Open Ilias [private feed settings](https://ilias3.uni-stuttgart.de/ilias.php?view=0&col_side=right&block_type=pdnews&cmd=editSettings&cmdClass=ilpdnewsblockgui&cmdNode=sh:6b:rv&baseClass=ilPersonalDesktopGUI#il_mhead_t_focus)
5. Set a password for your private feed
6. Save the password
7. Replace in the before copied private feed URL 'password' with your just set password
8. Insert in the app the new URL, user name and password and click the check icon at the bottom right.
