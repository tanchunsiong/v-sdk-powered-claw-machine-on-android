package us.zoom.sdksample.util;

import android.text.TextUtils;

import java.security.MessageDigest;

public class RandomUtil {

    private static String getMd5(String content) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(content.getBytes("UTF-8"));
        } catch (Exception e) {
            return null;
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    public static String getEightRandom() {
        String md5 = getMd5(String.valueOf(System.currentTimeMillis()));
        if (TextUtils.isEmpty(md5) || md5.length() < 8) {
            return "lite0123";
        }
        return md5.substring(0, 8);
    }
}
