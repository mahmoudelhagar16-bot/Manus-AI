package com.farm.layermanager.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * نقطة دخول Hilt. يجب تسجيل هذا الصف في AndroidManifest.xml:
 * <application android:name=".app.LayerFarmApplication" ... >
 */
@HiltAndroidApp
class LayerFarmApplication : Application()
