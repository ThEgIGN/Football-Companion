Football Companion android app made in Android Studio with the help of Java.

User authentication (Login and Register) is done using Firebase authentication.

The app consists of three main parts:

1. Matches where users can see all football matches for the current day. They are sorted into four categories:
    1. Live (currently being played)
    2. Ended (have finished)
    3. Upcoming (haven't been played or postponed)
    4. All the above

    Users can select each category and search for matches in each one of them.
    Users aren't limited to seeing matches only for the current date and can change dates freely.

    If users select a match in the category "Ended", a new screen will pop up showing match statistics for the selected match. Users can also press the button "Find stadium" which shows the coordinates of the stadium where the match was played in Google Maps.

    If users select a match in the category "Upcoming" and that match hasn't started yet, they can choose when to get a notification for that match (the default is 30 minutes before the match).
   
2. News where users can see all news related to football for the current day. News can also be searched through, and they can be opened inside the application.

3. Profile where users can set a profile picture (done using Firebase storage) which will be saved and shown every time a user logs in. It can also be changed.

    The main part of the Profile section are search terms, which users can add or remove by will. They serve as filters for the Matches section, where matches are shown only if they contain one of those terms.
    
    These terms are saved in the Firebase Realtime database and are applied every time a user logs in. They are saved to the database alongside the picture when the user presses the "Update profile" button.
    
    Users can also change the application language from English to Serbian and backwards. In order to see the effects, users have to either log out or reopen the application.

Below is the demo of the application showcasing all of the componenets listed above:


https://github.com/user-attachments/assets/fbc5053c-47a3-4398-9725-359b5f433726




https://github.com/user-attachments/assets/66dcbf6e-325c-4547-9df3-97fff2f99835


