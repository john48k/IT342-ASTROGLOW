import { initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";
import { getStorage } from "firebase/storage";

const firebaseConfig = {
  apiKey: "AIzaSyC1crIbnjmZ1uNlqZLH0T3_p4H7lWVJMyQ",
  authDomain: "astroglowfirebase-d2411.firebaseapp.com",
  projectId: "astroglowfirebase-d2411",
  storageBucket: "astroglowfirebase-d2411.firebasestorage.app",
  appId: "1:550900944978:web:2f1c2c235b20fa1b644f8b",
};

const app = initializeApp(firebaseConfig);
const db = getFirestore(app);
// Initialize Cloud Storage and get a reference to the service
const storage = getStorage(app);

export { db, storage };
