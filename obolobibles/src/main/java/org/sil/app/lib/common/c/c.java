package org.sil.app.lib.common.c;

public class c {
    static {
        try {
            System.loadLibrary("fntmangr_prot");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static native void initMobile(byte[] bytes, int len);
}
