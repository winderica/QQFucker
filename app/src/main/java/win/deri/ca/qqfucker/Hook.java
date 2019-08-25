package win.deri.ca.qqfucker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.lang.reflect.Method;
import java.util.Arrays;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Hook implements IXposedHookLoadPackage {
    private interface FilterFunction {
        boolean shouldHook(Method method);
    }

    private interface CallbackFunction {
        Object cb(XC_MethodHook.MethodHookParam param);
    }

    private void findAndHook(LoadPackageParam lpp, String className, FilterFunction filter, CallbackFunction callback) {
        final Class Cls = XposedHelpers.findClass(className, lpp.classLoader);
        if (Cls == null) return;
        Arrays.stream(Cls.getDeclaredMethods())
                .filter(filter::shouldHook)
                .forEach((method) -> hookMethod(className, method, callback));
    }

    private void hookMethod(String className, Method method, CallbackFunction callback) {
        XposedBridge.hookMethod(method, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) {
                XposedBridge.log("QQFucker: fucked " + className + "." + method.getName());
                return callback.cb(param);
            }
        });
    }

    private void hook(LoadPackageParam lpp, String className, String methodName) {
        findAndHook(lpp, className, (method) -> method.getName().equals(methodName), (XC_MethodHook.MethodHookParam param) -> null);
    }

    private void hook(LoadPackageParam lpp, String className, String methodName, Integer methodCount, CallbackFunction callback) {
        findAndHook(lpp, className, (method) -> method.getName().equals(methodName) && method.getParameterCount() == methodCount, callback);
    }

    private CallbackFunction openWithBrowser = (XC_MethodHook.MethodHookParam param) -> {
        Context activity = (Context) param.args[0];
        String uri = (String) param.args[1];
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse(uri))
                .putExtra("exlink", true)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        return intent;
    };

    public void handleLoadPackage(final LoadPackageParam lpp) {
        if ("com.tencent.qqlite".equals(lpp.packageName)) {
            hook(lpp, "com.tencent.mobileqq.mini.sdk.MiniAppLauncher", "startMiniApp");
            hook(lpp, "jvo", "F");
            hook(lpp, "jxl", "onClick");
            hook(lpp, "cooperation.qwallet.plugin.QWalletHelper", "launchQWalletAct");
            hook(lpp, "kri", "onClick");
            hook(lpp, "krk", "onClick");
            hook(lpp, "krm", "onClick");
            hook(lpp, "krg", "onClick");
            hook(lpp, "cooperation.qzone.QzonePluginProxyActivity", "a");
            hook(lpp, "oki", "a", 8, openWithBrowser);
        }
    }
}