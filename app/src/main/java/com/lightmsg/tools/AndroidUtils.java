package com.lightmsg.tools;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.view.View;
/**
 * 一个android的工具库
 * <p>功能还在不断完善中...</p>
 * 
 *
 */
public class AndroidUtils {
    private static final String TAG="AndroidUtils";

    /**
     * 安装APK
     * @param file
     *   ---------kuoa  2014.3.4
     */              
    public static void installApk(File file,Context context) {	
        Log.i(TAG,"install app:"+file);
        if (file.toString().endsWith(".apk")) {       	 	 
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);	
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
            intent.setDataAndType(Uri.fromFile(file),
                    "application/vnd.android.package-archive");

            context.startActivity(intent);                 
        } 
    }

    /**
     * 获取所有的应用信息 
     */
    public static  List<com.lightmsg.tools.AppInfo> getAllAppInfo(Context context) {
        // TODO Auto-generated method stub
        // 取得所有APP信息，图片
        ArrayList<com.lightmsg.tools.AppInfo> appList = new ArrayList<com.lightmsg.tools.AppInfo>(); // 用来存储获取的应用信息数据
        List<PackageInfo> packages = getAllAppsInfos(context);		
        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            com.lightmsg.tools.AppInfo tmpInfo = new com.lightmsg.tools.AppInfo();
            tmpInfo.setAppName(packageInfo.applicationInfo.loadLabel(
                    context.getPackageManager()).toString());
            tmpInfo.setPackageName(packageInfo.packageName);
            tmpInfo.setVersionName(packageInfo.versionName);
            tmpInfo.setVersionCode(packageInfo.versionCode);
            tmpInfo.setIcon(packageInfo.applicationInfo.loadIcon(context.getPackageManager()));
            tmpInfo.setAppIcon(packageInfo.applicationInfo.icon);	
            tmpInfo.setInstalled(true);			
            appList.add(tmpInfo);
        }
        return appList;
    }

    /** 
     * 查询手机内非系统应用 
     * @param context 
     * @return 
     */  
    public static List<PackageInfo> getAllAppsInfos(Context context) {  
        List<PackageInfo> apps = new ArrayList<PackageInfo>();  
        PackageManager pManager = context.getPackageManager();  
        //获取手机内所有应用  
        List<PackageInfo> paklist = pManager.getInstalledPackages(0);  
        for (int i = 0; i < paklist.size(); i++) {  
            PackageInfo pak = (PackageInfo) paklist.get(i);  
            //      Log.i(TAG, pak.applicationInfo.loadLabel(pManager)+"的packagename:"+pak.packageName);

            //判断是否为非系统预装的应用程序  
            if ((pak.applicationInfo.flags & pak.applicationInfo.FLAG_SYSTEM) <= 0) {  	            
                apps.add(pak);  	            
            }  
        }  
        return apps;  
    }  

    /**
     * 获取mac地址
     * @return mac地址
     */
    public static final String getMAC(Context ctx) {
        byte[] mac = null;
        StringBuffer sb = new StringBuffer();
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();

                while (address.hasMoreElements()) {
                    InetAddress ip = address.nextElement();
                    if (ip.isAnyLocalAddress() || !(ip instanceof Inet4Address)
                            || ip.isLoopbackAddress())
                        continue;
                    if (ip.isSiteLocalAddress())
                        mac = ni.getHardwareAddress();
                    else if (!ip.isLinkLocalAddress()) {
                        mac = ni.getHardwareAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (mac != null) {
            for (int i = 0; i < mac.length; i++) {
                sb.append(parseByte(mac[i]));
            }
            return sb.substring(0, sb.length() - 1);
        } else {
            return null;
        }
    }
    // 获取当前连接网络的网卡的mac地址
    private static String parseByte(byte b) {
        String s = "00" + Integer.toHexString(b) + ":";
        return s.substring(s.length() - 3);
    }


    /**
     * 判断当前网络是否连接
     * @param context
     * @return true 成功
     * <p> false 失败
     */
    @SuppressWarnings("unused")
    public static  boolean isConnectivityEnabled(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);  
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();  
        if(networkInfo != null)  
        {
            Log.d(TAG,"Connectivityable");
            return true;        
        }
        else          
        {
            Log.d(TAG,"ConnectivityUNable");
            return false; 
        }
    }

    /** 
     * uninstall apk file 
     * @param packageName  
     */  
    public static void uninstallAPK(Context context,String packageName){  
        Log.i(TAG,"uninstall "+packageName);
        Uri uri=Uri.parse("package:"+packageName);  
        Intent intent=new Intent(Intent.ACTION_DELETE,uri);  
        context.startActivity(intent);  
    }  

    /** 含有标题、内容、两个按钮的对话框 **/
    public static  void showAlertDialog(Context context,String title, String message,
            String positiveText,
            DialogInterface.OnClickListener onPositiveClickListener,
            String negativeText,
            DialogInterface.OnClickListener onNegativeClickListener) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText, onPositiveClickListener)
                .setNegativeButton(negativeText, onNegativeClickListener).create();
        alertDialog.show();
    }

    /**
     * 获取APP的图标
     */
    public static Drawable getLocalAppIcon(Context context,String packagename) {
        // TODO Auto-generated method stub
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(packagename, 0);
            Drawable appIcon = packInfo.applicationInfo.loadIcon(context
                    .getPackageManager());
            return appIcon;
            // Version = packInfo.versionName;
            // Log.d("versionname", Version);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 获取app名字
     */
    public static String getLocalAppName(Context context,String packagename) {
        // TODO Auto-generated method stub
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(packagename, 0);
            String appname = packInfo.applicationInfo.loadLabel(
                    context.getPackageManager()).toString();
            Log.i(TAG,"get app "+packagename+" name "+appname);
            return appname;
            // Version = packInfo.versionName;
            // Log.d("versionname", Version);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 使V获得焦点
     * @param v
     */
    public static  void getFocus(View v){
        if(v!=null){
            Log.i(TAG,"getFocus "+v);
            v.setFocusable(true);		
            v.setFocusableInTouchMode(true);
            v.requestFocus();
            v.requestFocusFromTouch();	
        }		

    }

    /**
     * 根据包名启动应用
     * @param context
     * @param packagename
     */
    public static void startApp(Context context,String packagename){	
        Log.i(TAG,"startapp "+packagename);
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(packagename);
        context.startActivity(intent);
    }



    /**
     * 判断某个应用是否已安装
     */
    @SuppressWarnings("unused")
    public static  boolean isAvilible(Context context, String packageName){ 
        final PackageManager packageManager = context.getPackageManager();//获取packagemanager 
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);//获取所有已安装程序的包信息 
        List<String> pName = new ArrayList<String>();//用于存储所有已安装程序的包名 
        //从pinfo中将包名字逐一取出，压入pName list中 
        if(pinfo != null){ 
            for(int i = 0; i < pinfo.size(); i++){ 
                String pn = pinfo.get(i).packageName; 
                pName.add(pn); 
            } 
        } 
        return pName.contains(packageName);//判断pName中是否有目标程序的包名，有TRUE，没有FALSE 
    } 


}
