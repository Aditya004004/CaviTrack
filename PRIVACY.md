# Privacy Policy for CaviTrack

**Last Updated:** August 24, 2026

Welcome to CaviTrack. This Privacy Policy explains how we collect, use, and protect your information when you use our mobile application.

## 1. Information We Collect
CaviTrack provides inventory and mold management services. To function correctly, we collect:
- **Account Information:** Email address and display name (via Firebase Authentication).
- **User-Generated Content:** Inventory data, customer details, and mold specifications that you explicitly enter.
- **Photos:** Images you capture or upload to attach to inventory components.

## 2. How We Use Your Information
- To authenticate your account securely.
- To sync your data across devices using Firebase Cloud Firestore.
- To store photo attachments securely via Firebase Cloud Storage.

## 3. Data Storage and Security
CaviTrack is built as an offline-first application. Your data is stored locally on your device (using Room SQLite) and securely synchronized with Google Firebase infrastructure in the cloud. Access to your cloud data is strictly limited by Firebase Security Rules ensuring only your authenticated account can access your data.

## 4. Account Deletion
You may delete your account at any time via the "Settings" screen within the app. Deleting your account will immediately and permanently erase your user data from our databases (Firestore, Room, and Storage).

## 5. Contact Us
If you have any questions or concerns about this Privacy Policy, please open an issue in the CaviTrack GitHub repository.
