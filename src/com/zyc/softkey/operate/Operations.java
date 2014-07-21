package com.zyc.softkey.operate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;

import com.zyc.softkey.utils.ILog;

public class Operations {
    private static final String TAG = "Operations";
    
    private Context mContext;
    
    private static final String SCHEME = "package";
    /** ����ϵͳInstalledAppDetails���������Extra����(����Android 2.1��֮ǰ�汾) */
    private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";
    /** ����ϵͳInstalledAppDetails���������Extra����(����Android 2.2) */
    private static final String APP_PKG_NAME_22 = "pkg";
    /** InstalledAppDetails���ڰ��� */
    private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";
    /** InstalledAppDetails���� */
    private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";
    
    public Operations(Context context){
        mContext = context;
    }
    
    public static void click(Context context){
        ILog.d(TAG, "click");
        
    }
    
    public static void doubleClick(Context context){
        ILog.d(TAG, "doubleClick");
        goToHome(context);
    }
    
    public static void longPress(Context context){
        ILog.d(TAG, "longPress");
//        showInstalledAppDetails(context, "com.zyc.softkey");
        showRecentTask();
    }
    
    private static void goToHome(Context context){
        ILog.d(TAG, "goToHome");
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * ����ϵͳInstalledAppDetails������ʾ�Ѱ�װӦ�ó������ϸ��Ϣ.����Android 2.3��Api Level
     * 9�����ϣ�ʹ��SDK�ṩ�Ľӿڣ� 2.3���£�ʹ�÷ǹ����Ľӿڣ��鿴InstalledAppDetailsԴ�룩�� 
     * 
     * @param context [����˵��]
     * @return void [��������˵��]
     * @exception throws [Υ������] [Υ��˵��]
     */
    private static void showInstalledAppDetails(Context context, String packageName) {
        ILog.d(TAG, "showInstalledAppDetails");
        Intent intent = new Intent();
        
        final int apiLevel = Build.VERSION.SDK_INT;
        if (apiLevel >= 9) { // 2.3��ApiLevel 9�����ϣ�ʹ��SDK�ṩ�Ľӿ�
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts(SCHEME, packageName, null);
            intent.setData(uri);
        } else {
            // 2.3���£�ʹ�÷ǹ����Ľӿڣ��鿴InstalledAppDetailsԴ�룩
            // 2.2��2.1�У�InstalledAppDetailsʹ�õ�APP_PKG_NAME��ͬ��
            final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22 : APP_PKG_NAME_21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME, APP_DETAILS_CLASS_NAME);
            intent.putExtra(appPkgName, packageName);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
    /**
     * ��������
     * <��������> [����˵��]
     * @return void [��������˵��]
     * @exception throws [Υ������] [Υ��˵��]
     */
    private static void showRecentTask() {
        try {
            Class localClass1 = Class.forName("android.os.ServiceManager");
            IBinder localIBinder = (IBinder) localClass1.getMethod("getService", 
                    new Class[] { String.class }).invoke(localClass1, new Object[] { "statusbar" });
            
            Class localClass2 = Class.forName("com.android.internal.statusbar.IStatusBarService").getClasses()[0];
            Object localObject = localClass2.getMethod("asInterface",
                    new Class[] { IBinder.class }).invoke(null, new Object[] { localIBinder });
            
            localClass2.getMethod("toggleRecentApps", new Class[0]).invoke(localObject, new Object[0]);
            return;
        } catch (Exception e) {
            return;
        }
    }
}
