package ti.optimizationpreferences;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;

import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;

@Kroll.module(name="TiOptimizationpreferences", id="ti.optimizationpreferences")
public class TiOptimizationpreferencesModule extends KrollModule {
    @Kroll.constant
    public static final int HUAWEI = 0;

    @Kroll.constant
    public static final int SAMSUNG = 1;

    private Intent huawei = intentClassName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity");
    private Intent samsung = intentClassName("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity");
    private int[] ALL = new int[]{HUAWEI, SAMSUNG};

    private final String saveIfSkip = "skipProtectedAppsMessage";
    private static final String LCAT = "TiOptimizationpreferencesModule";

    public TiOptimizationpreferencesModule()
    {
        super();
    }

    @Kroll.onAppCreate
    public static void onAppCreate(TiApplication app)
    {
        Log.d(LCAT, "onAppCreate");
    }

    @Kroll.method
    public void check(int[] brandNames)
    {

        final String packageName = getContext().getPackageName();
        if (shouldSkip()) {
            return;
        }

        for (int brand : brandNames) {
            if (isCallable(getContext(), getIntentFromConstant(brand))) {
                showWarning(getContext());
            }
        }
    }

    @Kroll.method
    public boolean needWarning(int[] brandNames)
    {
        for (int brand : brandNames) {
            if (isCallable(getContext(), getIntentFromConstant(brand))) {
                return true;
            }
        }
        return false;
    }

    private Intent getIntentFromConstant(int intentId)
    {
        Intent intent;
        switch (intentId) {
            case HUAWEI:
                return huawei;
            case SAMSUNG:
                return samsung;
            default:
                throw new IllegalArgumentException("Intent for " + intentId + " is not supported");
        }
    }

    private Intent intentClassName(String packageName, String className)
    {
        Intent intent = new Intent();
        intent.setClassName(packageName, className);
        return intent;
    }

    private String getUserSerial(Context context)
    {
        Object userManager = context.getSystemService(Context.USER_SERVICE);
        if (null == userManager)
            return "";
        try {
            Method myUserHandleMethod = android.os.Process.class.getMethod("myUserHandle", (Class<?>[]) null);
            Object myUserHandle = myUserHandleMethod.invoke(android.os.Process.class, (Object[]) null);
            Method getSerialNumberForUser = userManager.getClass().getMethod("getSerialNumberForUser", myUserHandle.getClass());
            Long userSerial = (Long) getSerialNumberForUser.invoke(userManager, myUserHandle);
            if (userSerial != null) {
                return String.valueOf(userSerial);
            } else {
                return "";
            }
        } catch (NoSuchMethodException ignored) {
        } catch (IllegalArgumentException ignored) {
        } catch (InvocationTargetException ignored) {
        } catch (IllegalAccessException ignored) {
        }
        return "";
    }

    private void huaweiProtectedApps(Context context)
    {
        try {
            String cmd = "am start -n " + huawei.getComponent().flattenToShortString();
            if (Build.VERSION.SDK_INT >= 17) {
                cmd += " --user " + getUserSerial(context);
            }
            Runtime.getRuntime().exec(cmd);
        } catch (IOException ignored) {
        }
    }

    private boolean isCallable(Context context, Intent intent)
    {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private AlertDialog.Builder huaweiWarning(Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(RHelper.getString("ti_optimizationpreferences_huawei_title"));
        builder.setMessage(RHelper.getString("ti_optimizationpreferences_huawei_message"));
        builder.setNeutralButton(android.R.string.no, null);
        builder.setView(createDontShowAgain());
        return builder;
    }

    private AlertDialog.Builder samsungWarning(Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(RHelper.getString("ti_optimizationpreferences_samsung_title"));
        builder.setMessage(RHelper.getString("ti_optimizationpreferences_samsung_message"));
        builder.setNeutralButton(android.R.string.no, null);
        builder.setView(createDontShowAgain());
        return builder;
    }

    private AlertDialog.Builder buildWarning(final Context context)
    {
        if (isCallable(context, huawei)) {
            AlertDialog.Builder builder = huaweiWarning(context);
            builder.setPositiveButton(RHelper.getString("ti_optimizationpreferences_settings"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    huaweiProtectedApps(context);
                }
            });
            return builder;
        } else if (isCallable(context, samsung)) {
            AlertDialog.Builder builder = samsungWarning(context);
            builder.setPositiveButton(RHelper.getString("ti_optimizationpreferences_settings"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    context.startActivity(samsung);
                }
            });
            return builder;
        }
        return null;
    }

    private RelativeLayout createDontShowAgain()
    {
        final AppCompatCheckBox dontShowAgain = new AppCompatCheckBox(getContext());
        dontShowAgain.setText(RHelper.getString("ti_optimizationpreferences_dont_show_again"));
        dontShowAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setSkip(isChecked);
            }
        });

        final RelativeLayout layout = new RelativeLayout(getContext());
        layout.setPadding(30, 30, 0, 0);
        layout.addView(dontShowAgain);

        return layout;
    }

    private void showWarning(Context context)
    {
        AlertDialog.Builder builder = buildWarning(context);
        builder.create().show();
    }

    private SharedPreferences getSettings()
    {
        return PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    private boolean shouldSkip() {
        return getSettings().getBoolean(saveIfSkip, false);
    }

    private void setSkip(boolean isChecked)
    {
        final SharedPreferences.Editor editor = getSettings().edit();
        editor.putBoolean(saveIfSkip, isChecked);
        editor.apply();
    }

    private Context getContext()
    {
        return TiApplication.getInstance().getCurrentActivity();
    }
}