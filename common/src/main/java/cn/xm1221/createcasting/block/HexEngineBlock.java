package cn.xm1221.createcasting.block;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class HexEngineBlock extends DirectionalKineticBlock {
    public static final DirectionProperty FACING =
            BlockStateProperties.FACING; // 或 Axis 方向

    public HexEngineBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos,
                                   BlockState state, Direction face) {
        return face.getAxis() == getRotationAxis(state);
    }

    // ... createBlockStateDefinition, getStateForPlacement 等
}
