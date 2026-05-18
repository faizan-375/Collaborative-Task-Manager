package com.mycompany.projectscd;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
// NEW IMPORT REQUIRED
import com.google.firebase.cloud.FirestoreClient;

import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseManager {

    // Store the Firestore instance
    private static Firestore db;

    // Name of the JSON key file you downloaded
    private static final String SERVICE_ACCOUNT_FILE = "key.json";

    public static void initialize() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                // Load the credentials file
                java.io.File keyFile = new java.io.File(SERVICE_ACCOUNT_FILE);
                if (!keyFile.exists()) {
                    System.err.println("Firebase credentials file (key.json) not found. " +
                            "Please place your service account key in the project root.");
                    return;
                }
                FileInputStream serviceAccount = new FileInputStream(SERVICE_ACCOUNT_FILE);

                // Configure Firebase options
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        // Replace 'your-project-id' with your ACTUAL project ID
                        .setDatabaseUrl("https://projectscd-desktop.firebaseio.com")
                        .build();

                // Initialize the app
                FirebaseApp.initializeApp(options);

                // --- FIX IS HERE ---
                // Use FirestoreClient instead of Firestore.getInstance()
                db = FirestoreClient.getFirestore();

                System.out.println("Firebase initialized successfully.");

            } catch (IOException e) {
                System.err.println("Error initializing Firebase: " + e.getMessage());
            }
        }
    }

    /**
     * @return The initialized Firestore database instance.
     */
    public static Firestore getFirestore() {
        if (db == null) {
            initialize();
        }
        return db;
    }
}