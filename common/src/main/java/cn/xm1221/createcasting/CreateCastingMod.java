package cn.xm1221.createcasting;

import cn.xm1221.createcasting.registry.ActionRegisry;

public final class CreateCastingMod {
    public static final String MOD_ID = "createcasting";

    public static void init() {
        // Write common init code here.
        ActionRegisry.init();
    }
}
