package com.gamesense.client.module.modules.misc;

import com.gamesense.api.setting.values.BooleanSetting;
import com.gamesense.api.util.player.InventoryUtil;
import com.gamesense.api.util.player.PlacementUtil;
import com.gamesense.api.util.world.BlockUtil;
import com.gamesense.client.module.Category;
import com.gamesense.client.module.Module;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author aesthetical
 * Taken from my client, Inferno.
 */
@Module.Declaration(name = "Scaffold", category = Category.Misc)
public class Scaffold extends Module {
    private static final BlockPos[] DIRECTION_OFFSETS = new BlockPos[] {
            new BlockPos(0, 0, 0),
            new BlockPos(-1, 0, 0),
            new BlockPos(1, 0, 0),
            new BlockPos(0, 0, -1),
            new BlockPos(0, 0, 1)
    };

    BooleanSetting silent = registerBoolean("Silent", false);
    BooleanSetting rotate = registerBoolean("Rotate", true);
    BooleanSetting roundRotation = registerBoolean("RoundRotation", false);

    private final Queue<BlockPos> blocks = new ConcurrentLinkedQueue<>();

    @Override
    protected void onDisable() {
        this.blocks.clear();
    }

    @Override
    public void onUpdate() {
        BlockPos base = new BlockPos(mc.player.posX, mc.player.posY - 1.0, mc.player.posZ);
        if (mc.world.isAirBlock(base)) {
            int slot = InventoryUtil.findFirstItemSlot(ItemBlock.class, 0, 8);
            if (slot == -1) {
                // rip
                return;
            }

            int oldSlot = mc.player.inventory.currentItem;
            InventoryUtil.switchTo(slot, this.silent.getValue());

            this.updateBlocks(base);
            while (!this.blocks.isEmpty()) {
                BlockPos pos = this.blocks.poll();
                if (pos == null) {
                    return;
                }

                if (mc.player.getDistance(pos.x, pos.y, pos.z) > 4.5) {
                    continue;
                }

                if (this.rotate.getValue()) {
                    BlockUtil.faceVectorPacketInstant(new Vec3d(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5), this.roundRotation.getValue());
                }

                PlacementUtil.place(pos, EnumHand.MAIN_HAND, false);
            }

            InventoryUtil.switchTo(oldSlot, this.silent.getValue());
        }
    }

    private void updateBlocks(BlockPos base) {
        if (!mc.world.isAirBlock(base)) {
            return;
        }

        for (BlockPos offset : Scaffold.DIRECTION_OFFSETS) {
            for (EnumFacing facing : EnumFacing.values()) {
                if (facing == EnumFacing.DOWN) {
                    continue;
                }

                BlockPos neighbor = base.add(offset);
                if (facing == EnumFacing.UP) {
                    this.blocks.add(neighbor);
                    return;
                } else {
                    this.blocks.add(neighbor.add(facing.getDirectionVec()));
                    return;
                }
            }
        }
    }
}