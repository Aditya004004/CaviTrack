import admin from 'firebase-admin';

// Placeholder initialization, normally you'd use a real serviceAccountKey.json
if (!admin.apps.length) {
  try {
    admin.initializeApp({
      credential: admin.credential.cert({
        projectId: 'placeholder-project-id',
        clientEmail: 'placeholder@placeholder.iam.gserviceaccount.com',
        privateKey: '-----BEGIN PRIVATE KEY-----\nPLACEHOLDER\n-----END PRIVATE KEY-----\n',
      }),
    });
    console.log('Firebase Admin initialized');
  } catch (error) {
    console.error('Firebase Admin initialization error:', error);
  }
}

export const sendNotification = async (topic: string, title: string, body: string) => {
  const message = {
    notification: { title, body },
    topic,
  };

  try {
    const response = await admin.messaging().send(message);
    console.log(`Successfully sent message to topic ${topic}:`, response);
  } catch (error) {
    console.error(`Error sending message to topic ${topic}:`, error);
  }
};
