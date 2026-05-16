package cn.xm1221.createcasting.blockentity;

import cn.xm1221.createcasting.CreateCastingMod;
import cn.xm1221.createcasting.block.HexEngineBlock;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * 咒术引擎方块实体 —— 通过 Hex 咒术驱动的 Create 应力源。
 * <p>
 * 核心原理：
 * <ul>
 *   <li>继承 {@link GeneratingKineticBlockEntity}，成为 Create 动能网络中的一个「应力发生器」。</li>
 *   <li>覆写 {@link #getGeneratedSpeed()} 返回当前应有转速，覆写 {@link #tick()} 实现倒计时。</li>
 *   <li>对外开放 {@link #setRotation(float, int)} 方法，供 Hex 咒术 Action 调用，设定转速与持续时间。</li>
 * </ul>
 * <p>
 * 应力计算公式（由 {@link com.simibubi.create.content.kinetics.KineticNetwork} 驱动）：
 * <pre>
 *   实际应力产出 = calculateAddedStressCapacity() × |getGeneratedSpeed()|
 * </pre>
 * 因此，基准产能（1 RPM 时）由 {@code BlockStressValues.CAPACITIES} 注册值决定。
 * <p>
 * 转速方向：
 * <ul>
 *   <li>正数 → 按方块 FACING 方向旋转（顺时针）</li>
 *   <li>负数 → 反方向旋转（逆时针）</li>
 * </ul>
 *
 * @see GeneratingKineticBlockEntity
 * @see com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity （参考实现）
 */
public class HexEngineBlockEntity extends GeneratingKineticBlockEntity {

    // ==================== 运行时状态 ====================

    /** 目标转速 (RPM)，正数为 FACING 方向，负数为反向 */
    private float targetSpeed = 0;

    /** 剩余运行时间 (tick)，每 tick 减 1，归零后停止旋转 */
    private int remainingTicks = 0;

    // ==================== 构造 & 初始化 ====================

    /**
     * 标准构造器（由旧版 API 或手动创建时调用）。
     */
    public HexEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Minecraft 1.21.1 的 {@code BlockEntityType.BlockEntitySupplier} 仅需
     * {@code (BlockPos, BlockState)} 两参数。此工厂构造器从静态字段获取
     * {@link CreateCastingMod#HEX_ENGINE_BE_TYPE}（已在注册阶段设置完毕），
     * 确保 {@link cn.xm1221.createcasting.block.HexEngineBlock#getBlockEntityType()}
     * 在方块放置时能返回正确类型。
     */
    public HexEngineBlockEntity(BlockPos pos, BlockState state) {
        this(CreateCastingMod.HEX_ENGINE_BE_TYPE, pos, state);
    }

    @Override
    public void initialize() {
        super.initialize();
        // 若在加载时已有剩余时间（如方块被保留），且未被其他源超驰，则主动激活
        if (!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed())
            updateGeneratedRotation();
    }

    // ==================== 公共 API（Hex 咒术调用入口） ====================

    /**
     * 由 Hex 咒术 Action 调用，设定引擎的转速与运行时长。
     * <p>
     * 调用此方法会自动触发 {@link #updateGeneratedRotation()}，
     * 通知动能网络转速与产能的变化。
     *
     * @param speed         RPM 转速，正数 = FACING 方向，负数 = 反向
     * @param durationTicks 持续 tick 数（20 tick = 1 秒），最小为 1
     */
    public void setRotation(float speed, int durationTicks) {
        this.targetSpeed = speed;
        this.remainingTicks = Math.max(durationTicks, 1); // 至少持续 1 tick
        updateGeneratedRotation();  // 通知动能网络：转速/产能已变化
    }

    /**
     * 获取当前目标转速（未经方向校正的原始值）。
     * 可用于护目镜信息显示或外部查询。
     */
    public float getTargetSpeed() {
        return targetSpeed;
    }

    /**
     * 获取剩余运行 tick 数。
     */
    public int getRemainingTicks() {
        return remainingTicks;
    }

    // ==================== KineticBlockEntity 覆写 ====================

    /**
     * 返回当前生成的转速。
     * <p>
     * 当剩余时间归零时返回 0（停止），否则将目标转速按方块 FACING 方向校正正负。
     * <p>
     * {@link GeneratingKineticBlockEntity#updateGeneratedRotation()} 会调用此方法
     * 来感知速度变化，从而决定是否创建/摧毁动能网络。
     *
     * @return 当前转速（RPM），0 表示停止
     */
    @Override
    public float getGeneratedSpeed() {
        if (remainingTicks <= 0)
            return 0;

        // 安全检查：确保当前方块状态确实是 HexEngineBlock
        if (!(getBlockState().getBlock() instanceof HexEngineBlock))
            return 0;

        // 使用 convertToDirection 根据 FACING 方向校正转速符号
        return convertToDirection(
                targetSpeed,
                getBlockState().getValue(HexEngineBlock.FACING)
        );
    }

    /**
     * 每 tick 调用。
     * <p>
     * 负责倒计时并在时间耗尽时通知动能网络停止。
     * 父类 {@code super.tick()} 处理网络验证、脏标记等。
     */
    @Override
    public void tick() {
        super.tick();

        // 仅在服务端执行倒计时
        if (level == null || level.isClientSide)
            return;

        if (remainingTicks > 0) {
            remainingTicks--;

            if (remainingTicks <= 0) {
                // 时间耗尽 → 通知网络此源已停止
                targetSpeed = 0;
                updateGeneratedRotation();
            }
        }
    }

    /**
     * 覆写：从配置中读取此方块注册的应力产能。
     * <p>
     * 因此实际产能由注册在 {@code BlockStressValues.CAPACITIES} 中的值决定。
     * 此处保留覆写以允许未来自定义动态产能逻辑。
     */
    @Override
    public float calculateAddedStressCapacity() {
        return super.calculateAddedStressCapacity();
    }

    // ==================== NBT 持久化 ====================

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        // 写入自定义数据
        tag.putFloat("TargetSpeed", targetSpeed);
        tag.putInt("RemainingTicks", remainingTicks);
        // 父类数据：speed, source, network, sequenceContext
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        // 读取自定义数据
        targetSpeed = tag.getFloat("TargetSpeed");
        remainingTicks = tag.getInt("RemainingTicks");
        // 父类数据
        super.read(tag, registries, clientPacket);
    }

    // ==================== 护目镜信息（工程师护目镜工具提示） ====================

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        // 先添加父类的发电机信息（产能等）
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        // 添加咒术引擎特有的运行状态信息
        CreateLang.translate("gui.goggles.hex_engine_stats")
                .forGoggles(tooltip);

        if (remainingTicks > 0) {
            // 正在运行
            CreateLang.text("  → ")
                    .style(ChatFormatting.GOLD)
                    .add(CreateLang.translateDirect("tooltip.hex_engine.running"))
                    .forGoggles(tooltip);

            CreateLang.number(targetSpeed)
                    .translate("generic.unit.rpm")
                    .style(ChatFormatting.AQUA)
                    .forGoggles(tooltip, 1);

            // 剩余时间（秒）
            float secondsLeft = remainingTicks / 20.0f;
            CreateLang.text("  → ")
                    .style(ChatFormatting.GOLD)
                    .add(CreateLang.number(secondsLeft)
                            .text("s")
                            .style(ChatFormatting.AQUA))
                    .text(" ")
                    .add(CreateLang.translateDirect("tooltip.hex_engine.remaining"))
                    .forGoggles(tooltip);
        } else {
            // 空闲中
            CreateLang.text("  → ")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.translateDirect("tooltip.hex_engine.idle"))
                    .forGoggles(tooltip);
        }

        return true;
    }
}
