package cn.xm1221.createcasting.neoforge;

import cn.xm1221.createcasting.CreateCastingMod;
import cn.xm1221.createcasting.block.HexEngineBlock;
import cn.xm1221.createcasting.blockentity.HexEngineBlockEntity;
import cn.xm1221.createcasting.registry.ActionRegisry;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Set;

/**
 * NeoForge 平台入口。
 *
 * <h2>注册策略</h2>
 * <ul>
 *   <li><b>方块 & 物品</b> → {@link CreateRegistrate} API，通过 {@code .onRegister()} 注入应力值。</li>
 *   <li><b>方块实体类型</b> → 原版 {@link RegisterEvent} 直接监听，确保
 *       {@link CreateCastingMod#HEX_ENGINE_BE_TYPE} 在注册事件中<b>同步</b>设置，
 *       避免 {@code IBE.getBlockEntityType()} 在方块放置时返回 null。</li>
 * </ul>
 */
@Mod(CreateCastingMod.MOD_ID)
public final class CreateCastingNeo {

    public CreateCastingNeo(ModContainer modContainer) {
        IEventBus modBus = modContainer.getEventBus();

        // ================================================================
        // 步骤 1：CreateRegistrate 实例化 + 绑定事件总线
        // ================================================================
        CreateRegistrate registrate = CreateRegistrate.create(CreateCastingMod.MOD_ID);
        registrate.registerEventListeners(modBus);

        // ================================================================
        // 步骤 2：方块（CreateRegistrate API）
        // ================================================================
        BlockEntry<HexEngineBlock> blockEntry = registrate
                .block("hex_engine", HexEngineBlock::new)
                .properties(p -> p.strength(2.0f, 3.0f).requiresCorrectToolForDrops())
                .onRegister(block -> {
                    CreateCastingMod.HEX_ENGINE_BLOCK = block;
                    BlockStressValues.CAPACITIES.register(block, () -> 256.0);
                    BlockStressValues.setGeneratorSpeed(256).accept(block);
                })
                .register();

        // ================================================================
        // 步骤 3：物品（CreateRegistrate API）
        // ================================================================
        ItemEntry<BlockItem> itemEntry = registrate
                .item("hex_engine", p -> new BlockItem(blockEntry.get(), p))
                .onRegister(item -> CreateCastingMod.HEX_ENGINE_ITEM = item)
                .register();

        // ================================================================
        // 步骤 4：BE 类型（原版 RegisterEvent，同步注入到静态字段）
        // ================================================================
        modBus.addListener(RegisterEvent.class, event -> {
            if (!event.getRegistryKey().equals(Registries.BLOCK_ENTITY_TYPE))
                return;
            event.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    CreateCastingMod.modLoc("hex_engine"),
                    () -> {
                        // 使用 2 参数工厂构造器适配 Minecraft 1.21.1 BlockEntitySupplier
                        BlockEntityType<HexEngineBlockEntity> type =
                                new BlockEntityType<>(
                                        HexEngineBlockEntity::new,  // (BlockPos, BlockState) 构造器
                                        Set.of(CreateCastingMod.HEX_ENGINE_BLOCK),
                                        null
                                );
                        CreateCastingMod.HEX_ENGINE_BE_TYPE = type;
                        return type;
                    }
            );
            ActionRegisry.init();
        });


    }
}
