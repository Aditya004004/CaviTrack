# Privacy Policy for CaviTrack

**Last Updated:** September 5, 2026

Welcome to CaviTrack. This Privacy Policy explains how we collect, use, and protect your information when you use our mobile application.

## 1. Information We Collect
CaviTrack provides inventory, tooling, and mold management services. To function correctly, we collect:
- **Account Information:** Email address and display name (via Firebase Authentication).
- **User-Generated Content:** Inventory components, customer accounts, and mold configurations that you explicitly create.
- **Photos:** Images you capture via the camera or upload to attach to inventory components.
- **Device Identifiers:** Firebase Cloud Messaging (FCM) tokens for delivering inventory alert push notifications.

## 2. How We Use Your Information
- To authenticate your account securely.
- To sync your data across devices using Firebase Cloud Firestore.
- To store photo attachments securely via Firebase Cloud Storage.
- To send push notifications about inventory alerts using Firebase Cloud Messaging.

## 3. Data Storage and Security
Your data is stored securely in Google Firebase infrastructure (Cloud Firestore and Cloud Storage). Access to your cloud data is strictly limited by Firebase Security Rules ensuring only your authenticated account can access your data. App Check (via Google Play Integrity) is enforced to ensure requests originate exclusively from authentic app instances.

## 4. Account and Data Deletion (Google Play Data Safety Compliance)
Users have full rights to permanent data removal:
1. **In-App Account Deletion:** You may permanently delete your account and all data at any time via the **Settings -> Delete Account** screen within the app. Triggering this cascades an immediate and irreversible purge of:
   - All inventory components, customers, and molds from Cloud Firestore.
   - All audit history logs from Cloud Firestore.
   - All uploaded component photos from Cloud Storage.
   - All associated FCM device push tokens.
   - Local on-device cached metrics DataStore.
   - The Firebase Authentication user account.
2. **Web-Based Deletion Request:** If you cannot access the mobile application, you may request complete account and data wipeout by emailing **cavitrack.privacy@gmail.com** with the subject line *"Account Deletion Request"* and your registered email address. Web deletion requests are verified and processed within 30 days.

## 5. Contact Us
If you have any questions or concerns about this Privacy Policy, please contact us at:

**Email:** cavitrack.privacy@gmail.com
