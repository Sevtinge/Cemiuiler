package com.sevtinge.cemiuiler.module.home.folder

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import com.sevtinge.cemiuiler.module.base.BaseHook
import com.sevtinge.cemiuiler.utils.*
import android.app.Application
import android.content.Context
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit


object FolderBlur : BaseHook() {
    @SuppressLint("SuspiciousIndentation")
    override fun init() {
        //修复文件夹背景模糊与始终模糊壁纸冲突
        if (mPrefsMap.getBoolean("home_other_always_blur_launcher_wallpaper")) return

        Application::class.java.hookBeforeMethod("attach", Context::class.java) { it ->
            EzXHelperInit.initHandleLoadPackage(lpparam)
            EzXHelperInit.setLogTag(TAG)
            EzXHelperInit.setToastTag(TAG)
            EzXHelperInit.initAppContext(it.args[0] as Context)


            if (mPrefsMap.getStringAsInt(
                    "home_recent_blur_level", 0
                ) == 4 || !mPrefsMap.getBoolean("home_folder_blur")
            ) {
                if (isAlpha() || checkVersionCode() >= 439096421) {
                    "com.miui.home.launcher.common.BlurUtils".hookBeforeMethod("isUserBlurWhenOpenFolder") {
                        it.result = false
                    }
                }
            } else {
                if (isAlpha() || checkVersionCode() >= 439096421) {
                    "com.miui.home.launcher.common.BlurUtils".hookBeforeMethod("isUserBlurWhenOpenFolder") {
                        it.result = true
                    }
                } else {
                    //copy from miui_xxl，修复文件夹内移动图标shortcut背景模糊丢失
                    var isShouldBlur = false
                    val folderInfo = "com.miui.home.launcher.FolderInfo".findClass()
                    val launcherClass = "com.miui.home.launcher.Launcher".findClass()
                    val blurUtilsClass = "com.miui.home.launcher.common.BlurUtils".findClass()
                    val cancelShortcutMenuReasonClass =
                        "com.miui.home.launcher.shortcuts.CancelShortcutMenuReason".findClass()

                    launcherClass.hookAfterMethod("openFolder", folderInfo, View::class.java) {
                        val mLauncher = it.thisObject as Activity
                        val isInNormalEditing = mLauncher.callMethod("isInNormalEditing") as Boolean
                        if (!isInNormalEditing) blurUtilsClass.callStaticMethod(
                            "fastBlur",
                            1.0f,
                            mLauncher.window,
                            true
                        )
                    }

                    launcherClass.hookAfterMethod("isFolderShowing") {
                        isShouldBlur = it.result as Boolean
                    }

                    launcherClass.hookAfterMethod("closeFolder", Boolean::class.java) {
                        isShouldBlur = false
                        val mLauncher = it.thisObject as Activity
                        val isInNormalEditing = mLauncher.callMethod("isInNormalEditing") as Boolean
                        if (isInNormalEditing) blurUtilsClass.callStaticMethod(
                            "fastBlur",
                            1.0f,
                            mLauncher.window,
                            true,
                            0L
                        )
                        else blurUtilsClass.callStaticMethod(
                            "fastBlur",
                            0.0f,
                            mLauncher.window,
                            true
                        )
                    }

                    launcherClass.hookAfterMethod(
                        "cancelShortcutMenu",
                        Int::class.java,
                        cancelShortcutMenuReasonClass
                    ) {
                        val mLauncher = it.thisObject as Activity
                        if (isShouldBlur) blurUtilsClass.callStaticMethod(
                            "fastBlur",
                            1.0f,
                            mLauncher.window,
                            true,
                            0L
                        )
                    }

                    launcherClass.hookBeforeMethod("onGesturePerformAppToHome") {
                        val mLauncher = it.thisObject as Activity
                        if (isShouldBlur) {
                            blurUtilsClass.callStaticMethod(
                                "fastBlur",
                                1.0f,
                                mLauncher.window,
                                true,
                                0L
                            )
                        }
                    }

                    blurUtilsClass.hookBeforeAllMethods("fastBlurWhenStartOpenOrCloseApp") {
                        val mLauncher = it.args[1] as Activity
                        val isInEditing = mLauncher.callMethod("isInEditing") as Boolean
                        if (isShouldBlur) it.result = blurUtilsClass.callStaticMethod(
                            "fastBlur",
                            1.0f,
                            mLauncher.window,
                            true,
                            0L
                        )
                        else if (isInEditing) it.result = blurUtilsClass.callStaticMethod(
                            "fastBlur",
                            1.0f,
                            mLauncher.window,
                            true,
                            0L
                        )
                    }

                    blurUtilsClass.hookBeforeAllMethods("fastBlurWhenFinishOpenOrCloseApp") {
                        val mLauncher = it.args[0] as Activity
                        val isInEditing = mLauncher.callMethod("isInEditing") as Boolean
                        if (isShouldBlur) it.result = blurUtilsClass.callStaticMethod(
                            "fastBlur",
                            1.0f,
                            mLauncher.window,
                            true,
                            0L
                        )
                        else if (isInEditing) it.result = blurUtilsClass.callStaticMethod(
                            "fastBlur",
                            1.0f,
                            mLauncher.window,
                            true,
                            0L
                        )
                    }

                    blurUtilsClass.hookAfterAllMethods("fastBlurWhenEnterRecents") {
                        it.args[0]?.callMethod("hideShortcutMenuWithoutAnim")
                    }

                    blurUtilsClass.hookAfterAllMethods("fastBlurWhenExitRecents") {
                        val mLauncher = it.args[0] as Activity
                        val isInEditing = mLauncher.callMethod("isInEditing") as Boolean
                        if (isShouldBlur) it.result = blurUtilsClass.callStaticMethod(
                            "fastBlur",
                            1.0f,
                            mLauncher.window,
                            true,
                            0L
                        )
                        else if (isInEditing) it.result = blurUtilsClass.callStaticMethod(
                            "fastBlur",
                            1.0f,
                            mLauncher.window,
                            true,
                            0L
                        )
                    }

                    blurUtilsClass.hookBeforeAllMethods("fastBlurDirectly") {
                        val blurRatio = it.args[0] as Float
                        if (isShouldBlur && blurRatio == 0.0f) it.result = null
                    }

                    /*  if ((getBoolean("miuihome_use_complete_blur", false) && !getBoolean("miuihome_complete_blur_fix", false))
                          || !(getBoolean("miuihome_use_complete_blur", false))
                      ) {
                          navStubViewClass.hookBeforeMethod("appTouchResolution", MotionEvent::class.java) {
                              val mLauncher = it.thisObject.getObjectField("mLauncher") as Activity?
                              if (isShouldBlur) {
                                  blurUtilsClass.callStaticMethod("fastBlurDirectly", 1.0f, mLauncher?.window)
                              }
                          }
                      }*/
                }

            }

        }

    }

}

