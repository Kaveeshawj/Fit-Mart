FitMart is an Android application designed to simplify fitness commerce by integrating product shopping, trainer booking, and gym location services into a single platform. Key features include:

Product Browsing : Search, filter, and purchase fitness supplements, equipment, and apparel.

Trainer Booking : Schedule sessions with certified personal trainers using real-time availability checks.

Secure Payments : Process transactions securely via integrated payment gateways.

Gym Locations : Find nearby gyms using Google Maps integration.

Admin Panel : Manage users, products, and analytics for business owners.


🌟 Features
User-Friendly Interface : Built with Material Design for a clean, intuitive experience.

Firebase Integration : Real-time database, authentication, and cloud storage for seamless data management.

Google Maps : Locate nearby gyms and get directions.

Notifications : Reminders for appointments.

Responsive Design : Optimized for all screen sizes and orientations.


🛠 Technologies Used

Android Studio : Primary development environment.

Java/Kotlin : Core programming languages.

Firebase : Authentication, Firestore, and Storage for backend services.

Google Maps API : For gym location services.

PayHere : For Payments.

Glide : Image loading and caching.

MPAndroidChart : Data visualization for analytics.

Material Design Components : For UI consistency.


📱 Screenshots

![Screenshot 2025-02-21 121416](https://github.com/user-attachments/assets/4de68110-1cdd-4f4f-a390-f54e63bb9895)
![Screenshot 2025-02-21 121505](https://github.com/user-attachments/assets/85406eb8-e186-4f8b-b6fd-b7aaa79af06a)
![Screenshot 2025-02-21 122329](https://github.com/user-attachments/assets/c7b62d07-2c9c-497f-8e0d-85df2e025200)
![Screenshot 2025-02-21 122422](https://github.com/user-attachments/assets/b18a942c-fbb2-47db-bdcf-0d0fac311cc8)
![Screenshot 2025-02-21 123353](https://github.com/user-attachments/assets/7a54a3ce-ebb6-40d8-a7f3-5397555efe4f)
![Screenshot 2025-02-21 123852](https://github.com/user-attachments/assets/e777400f-ae2e-413e-8515-b9c95f3c252a)
![Screenshot 2025-02-21 123926](https://github.com/user-attachments/assets/f9eda5ad-63ed-47a6-bb1d-fbc0fa17bcd0)
![Screenshot 2025-02-21 124035](https://github.com/user-attachments/assets/4b316a05-3f3b-4c95-bbde-d13358bf60e7)
![Screenshot 2025-02-21 125707](https://github.com/user-attachments/assets/34972012-d78d-4f4c-8c8a-6138892a7a83)


🎲 Getting Started

Prerequisites

Android Studio (Latest Version)

Firebase Account (https://firebase.google.com )

Payhere Account 

Google Maps API Key (https://console.cloud.google.com )

Setup
Clone the Repository
bash
Copy
1
git clone https://github.com/your-username/fitmart.git  

Import into Android Studio

Open the project in Android Studio.

Sync Gradle files.

Configure Firebase

Create a Firebase project and enable Firestore, Authentication, and Storage.

Download google-services.json and place it in app/src/main/.

Set Up Google Maps API

Generate an API key from the Google Cloud Console.

Add the key to app/src/main/AndroidManifest.xml:
xml
<meta-data  
    android:name="com.google.android.geo.API_KEY"  
    android:value="YOUR_API_KEY" />  
Build and Run'=

Connect an Android device or use an emulator.

Run ./gradlew build to ensure no errors.

Click Run 'app' in Android Studio.


📝 Usage

User Login/Registration :

Register with details.
Log in securely using Firebase Authentication.

Product Shopping :
Browse products by category.
Add items to cart and proceed to checkout.

Trainer Booking :
View trainer profiles and availability.
Book sessions via calendar integration.

Admin Panel :
Manage users, products, and trainer schedules.
Access sales analytics and update inventory.


📞 Contact
Author : Kaveesha Wijeweera

Email : kaveewijeweera20@gmail.com

LinkedIn :https://www.linkedin.com/in/kavee-wijeweera-30855528b/


📚 Acknowledgments

Firebase : For real-time database and authentication services.

Google Maps API : For location-based features.

MPAndroidChart : For data visualization in the admin panel.


🔍 Future Work

Add virtual training sessions via video calls.

Implement a subscription service for recurring purchases.

Expand product categories (e.g., organic foods, wellness products).


👀 How to Use the App
Download : Link to APK (if available)

Features :

Explore products in the Home tab.

Book trainers using the Trainers tab.

Track orders and appointments in the Profile tab.


🛠 Troubleshooting

Build Errors :
Delete app/build and gradle/caches folders.
Run ./gradlew clean build.

Firebase Issues :
Ensure google-services.json is correctly placed.
Check Firebase console for enabled services.
