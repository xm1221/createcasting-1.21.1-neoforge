package cn.xm1221.createcasting.blockentity;

import cn.xm1221.createcasting.block.HexEngineBlock;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class HexEngineBlockEntity extends GeneratingKineticBlockEntity {

    private float targetSpeed = 0;      // 目标转速 (RPM)
    private int remainingTicks = 0;     // 剩余运行 tick

    public HexEngineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // === 由 Hex 咒术调用 ===
    public void setRotation(float speed, int durationTicks) {
        this.targetSpeed = speed;
        this.remainingTicks = durationTicks;
        if (durationTicks > 0) {
            updateGeneratedRotation();  // 通知动能网络
        }
    }

    // === 覆写：返回当前生成的转速 ===
    @Override
    public float getGeneratedSpeed() {
        if (remainingTicks <= 0)
            return 0;
        return convertToDirection(
                targetSpeed,
                getBlockState().getValue(HexEngineBlock.FACING)
        );
    }

    // === 每 tick 倒计时 ===
    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide)
            return;

        if (remainingTicks > 0) {
            remainingTicks--;
            if (remainingTicks <= 0) {
                // 时间耗尽，通知网络转速归零
                updateGeneratedRotation();
            }
        }
    }

    // === 覆写：提供应力产出（可选，也可通过 BlockStressValues.CAPACITIES 注册）===
    @Override
    public float calculateAddedStressCapacity() {
        return 256.0f; // 基准产能 256 SU @ 1 RPM
    }

    // === 持久化 ===
    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.putFloat("TargetSpeed", targetSpeed);
        tag.putInt("RemainingTicks", remainingTicks);
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        targetSpeed = tag.getFloat("TargetSpeed");
        remainingTicks = tag.getInt("RemainingTicks");
        super.read(tag, registries, clientPacket);
    }
}
