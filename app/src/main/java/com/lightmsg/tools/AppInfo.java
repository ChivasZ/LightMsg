package com.lightmsg.tools;


import android.R.integer;
import android.graphics.drawable.Drawable;

public class AppInfo {

    private int id;

    private String appName = "";

    private String packageName = "";
    
    private String versionName = "null";
    
    private int versionCode = 0;
    /**
     * 鍥炬爣id
     */

    private int appIcon = 0;
    
    /**
     * 鍥炬爣drawable
     */
    private Drawable icon=null;
    /**
     * 瀵艰埅绫诲埆
     */

    private int type=0;
    
    /**
     * app绫诲埆
     * 0涓篴pk锛?涓虹綉椤碉紝2涓虹郴缁熻缃?
     */

    private int apptype=0;
    
    private boolean isInstalled=false;
    
    public final static int APP=0;
    public final static int WEB=1;
    public final static int SET=2;
    public final static int INTENT=3;
    
    /**
     * orm妗嗘灦蹇呴』鐨勯粯璁ゆ瀯閫犲櫒
     */
    public AppInfo(){
        
    }
    
    
    
    public AppInfo(String appName, String packageName, int appIcon, int type,
            int apptype, boolean isInstalled) {		
        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
        this.type = type;
        this.apptype = apptype;
        this.isInstalled = isInstalled;
    }



    public AppInfo(String appName, String packageName, String versionName,
            int versionCode, int appIcon,int type,boolean isinstalled) {		
        this.appName = appName;
        this.packageName = packageName;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.appIcon = appIcon;
        this.type=type;
        this.isInstalled=isinstalled;
    }
    
    
    
    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
    

    public boolean getisInstalled() {
        return isInstalled;
    }

    public void setInstalled(boolean isInstalled) {
        this.isInstalled = isInstalled;
    }

    public int getApptype() {
        return apptype;
    }

    public void setApptype(int apptype) {
        this.apptype = apptype;
    }

    
    
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    
    public String getAppName() {
        return appName;
    }
    public void setAppName(String appName) {
        this.appName = appName;
    }
    public String getPackageName() {
        return packageName;
    }
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    public String getVersionName() {
        return versionName;
    }
    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
    public int getVersionCode() {
        return versionCode;
    }
    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }
    public int getAppIcon() {
        return appIcon;
    }
    public void setAppIcon(int appIcon) {
        this.appIcon = appIcon;
    }

    @Override
    public String toString() {
        return "AppInfo [id=" + id + ", appName=" + appName + ", packageName="
                + packageName + ", versionName=" + versionName
                + ", versionCode=" + versionCode + ", appIcon=" + appIcon
                + ", type=" + type + ", apptype=" + apptype + ", isInstalled="
                + isInstalled + "]\n";
    }

}
