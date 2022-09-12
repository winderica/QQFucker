package winderi.ca.qqfucker

import android.content.Context
import android.content.Intent
import android.net.Uri
import de.robv.android.xposed.*
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.lang.reflect.Method

class Hook : IXposedHookLoadPackage {
    private fun filterAndHook(
        classLoader: ClassLoader,
        className: String,
        filter: (Method?) -> Boolean,
        callback: (MethodHookParam?) -> Any? = { null }
    ) {
        XposedHelpers.findClass(className, classLoader).declaredMethods
            .filter { filter(it) }
            .forEach {
                XposedBridge.hookMethod(it, object : XC_MethodReplacement() {
                    override fun replaceHookedMethod(param: MethodHookParam): Any? {
                        XposedBridge.log("QQFucker: fucked $className.${it.name}")
                        return callback(param)
                    }
                })
            }
    }

    override fun handleLoadPackage(lpp: LoadPackageParam) {
        if (lpp.packageName != "com.tencent.qqlite") {
            return
        }
        val classLoader = lpp.classLoader
        mapOf(
            "com.tencent.mobileqq.mini.sdk.MiniAppLauncher" to "startMiniApp",
            "cooperation.qwallet.plugin.QWalletHelper" to "launchQWalletAct",
            "cooperation.qzone.QzonePluginProxyActivity" to "a",
            "jvo" to "F",
            "jxl" to "onClick",
            "kri" to "onClick",
            "krk" to "onClick",
            "krm" to "onClick",
            "krg" to "onClick",
            "com.tencent.mobileqq.app.upgrade.UpgradeController" to "f"
        ).forEach { (key, value) -> filterAndHook(classLoader, key, { it?.name == value }) }
        filterAndHook(classLoader, "qhh", { it?.name == "a" && it.parameterCount == 8 }) {
            it?.let {
                (it.args[0] as Context).startActivity(
                    Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse(it.args[1] as String))
                        .putExtra("exlink", true)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
        filterAndHook(classLoader, "rtp", { it?.name == "a" && it.parameterCount == 4 }) {
            it?.let {
                if (it.args[0].javaClass.name != "rts") {
                    XposedBridge.invokeOriginalMethod(it.method, it.thisObject, it.args)
                }
            }
        }
        XposedHelpers.findAndHookMethod(
            "rkk", classLoader, "b", Int::class.java, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    param?.let {
                        if (it.args[0] == -5008) {
                            it.args[0] = -1000
                        }
                    }
                }
            }
        )
    }
}