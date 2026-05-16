package cn.xm1221.createcasting;

import cn.xm1221.createcasting.blockentity.HexEngineBlockEntity;
import cn.xm1221.createcasting.registry.ActionRegisry;
import com.simibubi.create.api.stress.BlockStressValues;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * CreateCasting 模组主类 —— 桥梁：Create 机械动力 ↔ Hex Casting 咒术。
 *
 * <h2>模块清单</h2>
 * <ul>
 *   <li>「咒术引擎」方块 — {@link cn.xm1221.createcasting.block.HexEngineBlock} /
 *       {@link HexEngineBlockEntity}</li>
 *   <li>「激活咒术引擎」Hex 图案 — {@link cn.xm1221.createcasting.casting.OpActivateHexEngine}</li>
 * </ul>
 *
 * <h2>跨平台注册架构</h2>
 * 碎片化注册（方块/BE/物品）由各平台模块处理：
 * <ul>
 *   <li><b>NeoForge</b> → {@code CreateRegistrate}（Create 官方 API）
 *       — 见 {@code cn.xm1221.createcasting.neoforge.CreateCastingNeo}</li>
 *   <li><b>Fabric</b> → 原版 {@code Registry.register()}
 *       — 见 {@code cn.xm1221.createcasting.fabric.ExampleModFabric}</li>
 * </ul>
 * 平台入口负责将注册完成的实例填入下方静态字段，然后调用 {@link #postRegistrationInit()}。
 *
 * <h2>本类职责</h2>
 * 仅处理跨平台通用、且依赖已注册方块实例的后置初始化：
 * <ol>
 *   <li>Create 应力值 → {@link BlockStressValues}</li>
 *   <li>Hex 咒术图案 → {@link ActionRegisry}</li>
 * </ol>
 */
public final class CreateCastingMod {

    public static final String MOD_ID = "createcasting";

    // ==================== 平台注入的实例 ====================

    /** 咒术引擎方块（平台入口注册后填入）。 */
    public static Block HEX_ENGINE_BLOCK;

    /** 咒术引擎方块物品。 */
    public static BlockItem HEX_ENGINE_ITEM;

    /** 咒术引擎方块实体类型。引用于 {@link cn.xm1221.createcasting.block.HexEngineBlock#getBlockEntityType()}。 */
    public static BlockEntityType<HexEngineBlockEntity> HEX_ENGINE_BE_TYPE;

    // ==================== 工具 ====================

    public static ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    // ==================== 后置初始化 ====================

    /**
     * 跨平台后置初始化 —— 须在方块/BE/物品注册<b>之后</b>调用。
     *
     * <p>负责完成依赖已注册方块实例的通用设定：</p>
     * <ol>
     *   <li>向 {@link BlockStressValues#CAPACITIES} 注册应力产能</li>
     *   <li>向 {@link BlockStressValues#RPM} 注册发电机转速（tooltip）</li>
     *   <li>初始化 Hex 咒术图案</li>
     * </ol>
     */
    public static void postRegistrationInit() {
        // 基准产能 256 SU @ 1 RPM === Creative Motor 级别
        //BlockStressValues.CAPACITIES.register(HEX_ENGINE_BLOCK, () -> 64.0);

        // 工具提示显示最大转速
        //BlockStressValues.setGeneratorSpeed(256).accept(HEX_ENGINE_BLOCK);

        // Hex 咒术图案注册
        ActionRegisry.init();
    }
}
