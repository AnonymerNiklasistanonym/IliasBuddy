# IliasBuddy
Aims to use the information from your private Ilias RSS feed and sends notifications if something new is on

## Edit/Run
Open the directory with Android Studio 3.1 with an installed Android SDK bigger than v21 (Android 5.1+).
You can then build it with gradle on an external device via USB-Debugging or run it on an internal Android Virtual Device (which you need to install to).

## How does it work
It basically does nothing else then scraping your personal private RSS feed.

## You just want my credentials, how can i trust you
You simply don't. Clone the repository, view the code and find where your credentials are used/saved/gotten and you will quickly realize that they do not leave this application in any form besides the Http requests to Ilias which I cannot change.
After that build the program yourselve and you can be sure that nobody can track your personal private RSS feed + you can always change the password in Ilias.

## How can I get the needed credentials for this application
You need three things:
- Feed URL
- Feed user name
- Feed password

1. Open Ilias [feed page](https://ilias3.uni-stuttgart.de/ilias.php?view=0&col_side=right&block_type=pdnews&cmd=showFeedUrl&cmdClass=ilpdnewsblockgui&cmdNode=sh:6b:rv&baseClass=ilPersonalDesktopGUI#il_mhead_t_focus)
2. Copy/Save the private feed URL
3. Extract your user name from the URL (It is your name without spaces at the beginning: 'MaxMueller')
4. Open Ilias [private feed settings](https://ilias3.uni-stuttgart.de/ilias.php?view=0&col_side=right&block_type=pdnews&cmd=editSettings&cmdClass=ilpdnewsblockgui&cmdNode=sh:6b:rv&baseClass=ilPersonalDesktopGUI#il_mhead_t_focus)
5. Set a password for your private feed
6. Save the password
7. Replace in the before copied private feed URL 'password' with your just set password
8. Insert in the app the new URL, user name and password and click the check icon at the bottom right.
(If you cannot find the input of these three things click the three points at the right top and click then in the menu 'Open Setup' to get there)
