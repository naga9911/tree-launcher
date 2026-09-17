buildscript {
    extra.apply {
        set("composeUiVersion", "1.5.4")
        set("kotlinVersion", "1.9.0")
    }
}

val composeUiVersion = extra["composeUiVersion"] as String
val kotlinVersion = extra["kotlinVersion"] as String

plugins {
    id("com.android.application") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}
