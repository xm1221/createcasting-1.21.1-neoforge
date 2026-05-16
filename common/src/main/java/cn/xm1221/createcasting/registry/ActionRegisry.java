package cn.xm1221.createcasting.registry;

import at.petrak.hexcasting.api.casting.math.HexDir;
import cn.xm1221.createcasting.api.ActionRegistryHelper;
import cn.xm1221.createcasting.casting.OpActivateHexEngine;

public class ActionRegisry {

    /**
     * 模组初始化时调用，注册所有自定义 Hex 图案动作。
     */
    public static void init() {
        ActionRegistryHelper.register(
                "activate_hex_engine", "aeawwwaeaqeewdwwddae", HexDir.EAST, OpActivateHexEngine.INSTANCE
        );

    }
}
