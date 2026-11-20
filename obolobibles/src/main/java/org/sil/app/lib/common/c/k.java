package org.sil.app.lib.common.c;

public class k {
    static {
        try {
            System.loadLibrary("fntmangr_cath");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static native void initMobile(byte[] bytes, int len);
}
