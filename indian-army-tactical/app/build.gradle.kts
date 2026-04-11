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
        versionCode = 11
        versionName = "1.2.3"
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

    // ── Exclude all dex from the bundle ──────────────────────────────────────
    // WFF v2 bundles MUST be strictly resource-only (no dex files).
    // AGP generates an empty classes.dex by default even when hasCode=false;
    // we exclude it at the packaging layer so Play Store validation passes.
    packaging {
        resources {
            excludes += "**/*.dex"
        }
    }
}

// No code dependencies — Watch Face Format is 100% resource-based

// ── Strip dex files from release bundle before Play Store submission ──────────
// Even with android:hasCode="false", AGP produces an empty classes.dex.
// Since standard AGP excludes don't work reliably for AABs, we forcefully 
// strip the dex directory from the final AAB using the system zip utility.
afterEvaluate {
    tasks.named("bundleRelease").configure {
        doLast {
            val aabFile = layout.buildDirectory.file("outputs/bundle/release/app-release.aab").get().asFile
            if (aabFile.exists()) {
                println("WFF: Forcibly stripping base/dex/ from ${aabFile.name}")
                exec {
                    commandLine("zip", "-d", aabFile.absolutePath, "base/dex/*")
                    isIgnoreExitValue = true // Ignore error if already deleted
                }
            }
        }
    }
}
