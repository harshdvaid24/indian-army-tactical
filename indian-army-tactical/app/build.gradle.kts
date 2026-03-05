plugins {
    id("com.android.application")
}

android {
    namespace = "com.watchforge.tacticalindia"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.watchforge.tacticalindia"
        minSdk = 30          // Wear OS 3+ (broader device reach for Play Store)
        targetSdk = 35
        versionCode = 7
        versionName = "1.1.0"
    }

    // ── Release signing ──────────────────────────────────────────────────────
    // Credentials are kept outside VCS in gradle.properties (or local.properties)
    // Set these in your environment / CI:
    //   KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD
    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH")
                ?: project.findProperty("KEYSTORE_PATH") as String?
            val keystorePass = System.getenv("KEYSTORE_PASSWORD")
                ?: project.findProperty("KEYSTORE_PASSWORD") as String?
            val keyAlias = System.getenv("KEY_ALIAS")
                ?: project.findProperty("KEY_ALIAS") as String?
            val keyPass = System.getenv("KEY_PASSWORD")
                ?: project.findProperty("KEY_PASSWORD") as String?

            if (keystorePath != null) {
                storeFile = file(keystorePath)
                storePassword = keystorePass
                this.keyAlias = keyAlias
                keyPassword = keyPass
            } else {
                // Fallback to debug keystore so local builds still work
                storeFile = signingConfigs.getByName("debug").storeFile
                storePassword = signingConfigs.getByName("debug").storePassword
                this.keyAlias = signingConfigs.getByName("debug").keyAlias
                keyPassword = signingConfigs.getByName("debug").keyPassword
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false     // No code to minify (WFF is resource-only)
            isShrinkResources = false   // WFF resources MUST NOT be removed
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // Produce AAB (required by Play Store) — also produces APK for sideload
    bundle {
        language { enableSplit = false }
        density  { enableSplit = false }
        abi      { enableSplit = false }
    }
}

// No code dependencies — Watch Face Format is 100% resource-based

