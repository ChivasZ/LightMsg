package com.lightmsg.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Qinghua on 2016/2/26.
 */
public class Md5Sum {

    private static byte[] getStringMd5(String s) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md5.update(s.getBytes());

        byte[] b = md5.digest();
        return b;
    }

    private static byte[] getFileMd5(File file) {
        System.out.println("getFileMD5(), file="+file);
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }

        byte[] b = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            InputStream is = new FileInputStream(file);
            int len;
            byte buf[] = new byte[1024];
            while ((len = is.read(buf)) != -1) {
                md5.update(buf, 0, len);
            }
            b = md5.digest();
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return b;
    }

    private static String getHexString(byte[] b){
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < b.length; i ++){
            int val = b[i] & 0xff;
            if (val < 16) sb.append("0");
            sb.append(Integer.toHexString(val));
        }
        return sb.toString();
    }

    /**
     * Get Md5 message-digest from a string or file.
     *
     * @param obj String or File object
     * @return md5 code or null if failed
     */
    public static String getMd5(Object obj) {
        byte[] b = null;
        if (obj instanceof String) {
            b = getStringMd5((String)obj);
        } else if (obj instanceof File) {
            b = getFileMd5((File)obj);
        }

        //System.out.println("byte[]="+b.toString());

        /*char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
        int j = b.length;
        char str[] = new char[j * 2];
        int k = 0;
        for (int i = 0; i < j; i++) {
            byte byte0 = b[i];
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        return new String(str);*/
        return getHexString(b);
    }

    public static void main(String[] args) {
        System.out.println("args[0]="+args[0]);
        System.out.println("md5="+getMd5(args[0]));
        System.out.println("md5="+getMd5("hello\n"));
        System.out.println("md5="+getMd5(new File("./Md5Sum.class")));
    }
}
