package cn.xm1221.createcasting.casting

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.misc.MediaConstants
import cn.xm1221.createcasting.blockentity.HexEngineBlockEntity
import net.minecraft.core.BlockPos

/**
 * 咒术引擎激活术 —— 消耗媒质，使目标位置的咒术引擎以指定转速旋转指定时长。
 *
 * ## 栈上参数（共 3 个，按 Hex 栈顶→栈底顺序）：
 * 1. **Double** — 持续时间（tick），20 tick = 1 秒。会自动钳位到 [1, 72000]。
 * 2. **Double** — 转速（RPM），正数为引擎 FACING 方向，负数为反向。会自动钳位到 [-256, 256]。
 * 3. **Vec3**  — 目标咒术引擎方块的中心坐标。
 *
 * ## 施法条件：
 * - 目标位置必须在施法者的施法范围内（[CastingEnvironment.assertPosInRange]）。
 * - 目标位置处必须存在一个 [HexEngineBlockEntity]（即已放置的咒术引擎）。
 * - 若目标位置没有引擎方块，世界操作静默失败（无 Mishap）。
 *
 * ## 媒质消耗：
 * ```
 *   基础消耗 = 5 × DUST_UNIT
 *   转速加成 = |speed| / 2 × DUST_UNIT
 *   时长加成 = duration / 40 × DUST_UNIT  （每 2 秒额外消耗 1 dust）
 *   总消耗 = 基础 + 转速加成 + 时长加成
 * ```
 *
 * ## 使用示例：
 * ```
 *   // Hex 栈准备（从顶到底）：
 *   // 200.0               ← 持续 200 tick (10 秒)
 *   // 64.0                ← 转速 64 RPM
 *   // Vec3(10, 64, 10)    ← 引擎位置
 *
 *   激活咒术引擎 ← 画出此 Pattern
 * ```
 *
 * @see HexEngineBlockEntity.setRotation
 */
object OpActivateHexEngine : SpellAction {

    override val argc: Int = 3

    /** 最大转速绝对值（与 Creative Motor 一致） */
    private const val MAX_SPEED = 256.0f
    /** 最大持续时间 tick（1 小时 = 20 × 60 × 60） */
    private const val MAX_DURATION = 72000

    /**
     * 从栈上取出参数，校验并封装为一个延迟执行的 [RenderedSpell]。
     *
     * 此方法在 Hex 求值阶段执行。任何失败都通过抛出 [Mishap] 反馈给施法者。
     *
     * @param args 从栈上弹出的参数列表（索引 0 = 栈底，即最早入栈的参数）
     * @param env  施法环境
     * @return 包含延迟咒术、媒质消耗和粒子效果的 Result
     * @throws Mishap 位置超出施法范围时抛出
     */
    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        // ---- 1. 取出栈参数 ----
        // args[0] = 栈顶-2 → Vec3（引擎位置）
        // args[1] = 栈顶-1 → Double（转速 RPM）
        // args[2] = 栈顶   → Double（持续时间 tick）
        val posVec = args.getVec3(0, 3)
        val rawSpeed = args.getDouble(1, 3)
        val rawDuration = args.getDouble(2, 3)

        // ---- 2. 位置校验（超出范围会抛出 Mishap） ----
        val targetPos = BlockPos.containing(posVec)
        env.assertPosInRange(targetPos)

        // ---- 3. 转速钳位到安全范围 [-256, 256] ----
        val speed = rawSpeed.toFloat().coerceIn(-MAX_SPEED, MAX_SPEED)

        // ---- 4. 持续时间钳位到 [1, MAX_DURATION] ----
        val durationTicks = rawDuration.toInt()*20.coerceIn(1, MAX_DURATION)

        // ---- 5. 计算媒质消耗 ----
        val baseCost = 5 * MediaConstants.DUST_UNIT
        val speedCost = (kotlin.math.abs(speed) / 2.0).toLong() * MediaConstants.DUST_UNIT
        val durationCost = (durationTicks / 40).toLong() * MediaConstants.DUST_UNIT
        val totalCost = baseCost + speedCost + durationCost

        // ---- 6. 粒子效果预览 ----
        val particles = listOf(
            ParticleSpray.burst(posVec, 2.0),   // 目标位置 burst
            ParticleSpray.cloud(posVec, 1.5)    // 目标位置 cloud
        )

        // ---- 7. 封装延迟咒术，交给 HexMod 引擎在 side-effects 阶段执行 ----
        return SpellAction.Result(
            ActivateSpell(targetPos, speed, durationTicks),
            totalCost,
            particles,
            1  // opCount
        )
    }

    // ==================== 延迟执行的 RenderedSpell ====================

    /**
     * 实际执行世界操作的咒术。
     *
     * 在 HexMod 的 side-effects 阶段被调用，此时已有权修改世界。
     */
    private class ActivateSpell(
        val pos: BlockPos,
        val speed: Float,
        val ticks: Int
    ) : RenderedSpell {

        /**
         * 查找目标位置的 [HexEngineBlockEntity] 并激活它。
         *
         * 如果目标位置没有对应的方块实体，静默失败
         *（execute 阶段已有位置校验，此处不会再抛 mishap）。
         */
        override fun cast(env: CastingEnvironment) {
            val be = env.world.getBlockEntity(pos)
            if (be is HexEngineBlockEntity) {
                be.setRotation(speed, ticks)
            }
        }
    }
}
