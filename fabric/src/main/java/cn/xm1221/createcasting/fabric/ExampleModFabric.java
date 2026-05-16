package cn.xm1221.createcasting.fabric;

import cn.xm1221.createcasting.CreateCastingMod;
import cn.xm1221.createcasting.block.HexEngineBlock;
import cn.xm1221.createcasting.blockentity.HexEngineBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Fabric 平台入口 —— 使用原版 {@link Registry#register} 注册方块/BE/物品。
 *
 * <p>与 NeoForge 入口（使用 {@code CreateRegistrate}）不同，
 * Fabric 侧直接使用原版注册表以保持轻量和跨平台兼容。</p>
 *
 * <h2>注册流程</h2>
 * <ol>
 *   <li>原版 {@code Registry.register} 注册方块、物品、方块实体</li>
 *   <li>将注册完成的实例填入 {@link CreateCastingMod} 静态字段</li>
 *   <li>调用 {@link CreateCastingMod#postRegistrationInit()} 完成应力值和 Hex 图案注册</li>
 * </ol>
 *
 */
public final class ExampleModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        // ================================================================
        // 步骤 1：注册方块
        // ================================================================
        /*HexEngineBlock block = new HexEngineBlock(
                BlockBehaviour.Properties.of()
                        .strength(2.0f, 3.0f)
                        .requiresCorrectToolForDrops()
        );
        Registry.register(
                BuiltInRegistries.BLOCK,
                CreateCastingMod.modLoc("hex_engine"),
                block
        );
        CreateCastingMod.HEX_ENGINE_BLOCK = block;

        // ================================================================
        // 步骤 2：注册方块物品
        // ================================================================
        BlockItem blockItem = new BlockItem(block, new Item.Properties());
        Registry.register(
                BuiltInRegistries.ITEM,
                CreateCastingMod.modLoc("hex_engine"),
                blockItem
        );
        CreateCastingMod.HEX_ENGINE_ITEM = blockItem;

        // ================================================================
        // 步骤 3：注册方块实体类型
        // ================================================================
        BlockEntityType<HexEngineBlockEntity> beType = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                CreateCastingMod.modLoc("hex_engine"),
                BlockEntityType.Builder
                        .of(HexEngineBlockEntity::new, block)
                        .build(null)
        );
        CreateCastingMod.HEX_ENGINE_BE_TYPE = beType;

        // ================================================================
        // 步骤 4：后置初始化（应力值 + Hex 图案）
        // ================================================================
        CreateCastingMod.postRegistrationInit();*/
    }
}
