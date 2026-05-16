package cn.xm1221.createcasting.block;

import cn.xm1221.createcasting.blockentity.HexEngineBlockEntity;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 咒术引擎方块 —— 可通过 Hex 咒术激活的 Create 应力源。
 *
 * <p>设计参考：{@link com.simibubi.create.content.kinetics.motor.CreativeMotorBlock}</p>
 *
 * <p>特性：</p>
 * <ul>
 *   <li>继承 {@link DirectionalKineticBlock}，支持六个方向的 FACING 放置。</li>
 *   <li>实现 {@link IRotate}（通过父类），提供 {@code getRotationAxis} 和 {@code hasShaftTowards}。</li>
 *   <li>实现 {@link IBE}{@code <HexEngineBlockEntity>}，绑定对应的方块实体类。</li>
 *   <li>放置时自动检测相邻动能方块的方向，智能对齐。</li>
 * </ul>
 *
 * <p>动能连接规则：</p>
 * <ul>
 *   <li>传动轴仅能在 FACING 方向连接（输出面）。</li>
 *   <li>旋转轴 = FACING 方向对应的轴。</li>
 *   <li>无隐藏应力影响（与 Creative Motor 不同，护目镜可见应力条）。</li>
 * </ul>
 *
 * @see HexEngineBlockEntity
 */
public class HexEngineBlock extends DirectionalKineticBlock implements IBE<HexEngineBlockEntity> {

    public HexEngineBlock(Properties properties) {
        super(properties);
        // 注册默认方块状态：朝北
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    // ==================== IRotate 实现 (方向与轴) ====================

    /**
     * 获取旋转轴。
     * <p>对于此方块，旋转轴总是 FACING 方向所在的轴。</p>
     *
     * @param state 当前方块状态
     * @return FACING 对应的轴
     */
    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    /**
     * 判断指定面是否有传动轴接口。
     * <p>仅有 FACING 面可以连接传动轴（即此面为输出面）。</p>
     *
     * @param world 世界
     * @param pos   方块位置
     * @param state 当前方块状态
     * @param face  要检测的面
     * @return 当且仅当 face == FACING 时返回 true
     */
    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING);
    }

    // ==================== IBE 实现 (方块实体绑定) ====================

    @Override
    public Class<HexEngineBlockEntity> getBlockEntityClass() {
        return HexEngineBlockEntity.class;
    }

    /**
     * 返回此方块对应的方块实体类型。
     * <p>注意：此处引用 {@link cn.xm1221.createcasting.CreateCastingMod#HEX_ENGINE_BE_TYPE}，
     * 请确保该静态字段在方块创建前已初始化（使用延迟初始化或调整注册顺序）。</p>
     */
    @Override
    public BlockEntityType<? extends HexEngineBlockEntity> getBlockEntityType() {
        // 方块实体类型在 CreateCastingMod 中注册
        return cn.xm1221.createcasting.CreateCastingMod.HEX_ENGINE_BE_TYPE;
    }
}
