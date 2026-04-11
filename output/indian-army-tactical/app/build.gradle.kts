plugins {
    id("com.android.application")
}

android {
    namespace = "com.army.tectical"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.army.tectical"
        minSdk = 34          // Wear OS 4+ (Required for WFF letterSpacing)
        targetSdk = 35
        versionCode = 10
        versionName = "1.2.2"
    }

    // ── Release signing ──────────────────────────────────────────────────────
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

    bundle {
        language { enableSplit = false }
        density  { enableSplit = false }
        abi      { enableSplit = false }
    }

    packaging {
        resources {
            excludes += "**/*.dex"
        }
    }
}

// No code dependencies — Watch Face Format is 100% resource-based

afterEvaluate {
    tasks.named("bundleRelease").configure {
        doLast {
            val aabFile = layout.buildDirectory.file("outputs/bundle/release/app-release.aab").get().asFile
            if (aabFile.exists()) {
                println("WFF: Forcibly stripping base/dex/ from ${aabFile.name}")
                exec {
                    commandLine("zip", "-d", aabFile.absolutePath, "base/dex/*")
                    isIgnoreExitValue = true
                }
            }
        }
    }
}
