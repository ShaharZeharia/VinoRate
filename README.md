﻿# VinoRate
Welcome to **VinoRate**, the ultimate app for wine enthusiasts. VinoRate offers personalized wine recommendations, detailed wine information, and user reviews, helping users discover the perfect wine to suit their tastes. The app integrates seamlessly with Firebase to provide real-time updates, ensuring users always have the most current and accurate information.


## Features

-   **Login and Authentication**: Users can sign in using their email, phone, or Google account. VinoRate uses Firebase Authentication to manage user sessions and preferences.
-   **Daily Wine Recommendations**: The "For You" screen provides users with a daily wine recommendation that updates every 24 hours.
-   **Wishlist**: Users can add wines to their Wishlist, allowing them to easily track and manage wines they wish to try in the future.
-   **Search Functionality**: Users can search for wines by name. The app displays detailed information, including user reviews and ratings, for each wine.
-   **Reviews**: Users can rate and review wines, sharing their experiences with the VinoRate community.
-   **User Profile**: The profile screen displays the user's personal information, reviews, and provides an option to log out.
-   **Real-time Updates**: VinoRate integrates with Firebase to provide real-time updates on wine ratings, reviews, and recommendations.


## Installation

 - Clone the repository:
git clone https://github.com/ShaharZeharia/VinoRate.git

 -  Open the project in your preferred IDE (e.g., Android Studio).

 -  Build and run the project on your device or emulator.

### Firebase Setup

To enable Firebase functionality in your VinoRate project, follow these steps:

1.  **Firebase Project Setup:**
    
    -   Go to the Firebase Console.
    -   Create a new Firebase project or select an existing one.
    -   Add an Android app to your Firebase project:
        -   Register the app with your app’s package name.
        -   Download the `google-services.json` file and place it in the `app/` directory of your Android project.
2.  **Enable Firebase Services:**
    
    -   In the Firebase Console, navigate to the following sections and enable the necessary services:
        -   **Authentication**: Set up Firebase Authentication to manage user sign-ins.
        -   **Realtime Database**: Set up and configure your Realtime Database.
        -   **Cloud Firestore** (if needed): Enable Firestore if your app uses it for data storage.
        -   **Storage** (if needed): Enable Firebase Storage if your app needs to store files.
3.  **Configure Firebase in Your App:**
    
    -   Ensure the `google-services.json` file is correctly placed in the `app/` directory.
    -   Update your app-level `build.gradle` file to include the Firebase dependencies:
        
        gradle
        
        Copy code
        
        `dependencies {
            implementation platform('com.google.firebase:firebase-bom:26.2.0')
            implementation 'com.google.firebase:firebase-auth'
            implementation 'com.google.firebase:firebase-database'
            // Add other Firebase dependencies as needed
        }` 
        
    -   Sync your project with Gradle files to apply the changes.
4.  **Firebase Configuration in the App:**
    
    -   Create a `.env` file in the root directory of your project (if using environment variables) and add your Firebase configuration values:
        
        bash
        
        Copy code
        
        `REACT_APP_FIREBASE_API_KEY=your-api-key
        REACT_APP_FIREBASE_AUTH_DOMAIN=your-auth-domain
        REACT_APP_FIREBASE_PROJECT_ID=your-project-id
        REACT_APP_FIREBASE_STORAGE_BUCKET=your-storage-bucket
        REACT_APP_FIREBASE_MESSAGING_SENDER_ID=your-sender-id
        REACT_APP_FIREBASE_APP_ID=your-app-id` 
        
5.  **Testing the Firebase Connection:**
    
    -   After completing the setup, run your app on a device or emulator.
    -   Test the authentication, database reads and writes, and any other Firebase-related functionality to ensure everything is working as expected.

# Contact

For any questions or support, please contact:
 **Shachar Zeharia** - shacharz2000@gmail.com

