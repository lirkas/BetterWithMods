package betterwithmods.module.tweaks;


import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import betterwithmods.common.blocks.tile.TileKiln;
import betterwithmods.common.registry.KilnStructureManager;
import betterwithmods.common.registry.heat.BWMHeatRegistry;
import betterwithmods.module.Feature;


public class KilnChanges extends Feature {
    
    private boolean revertKilnBlock = true;
    private boolean denyKilnBlockPlacement = true;

    @Override
    public String getFeatureDescription() {
        return "Few changes for the brick Kiln";
    }

    @Override
    public void setupConfig() {
        revertKilnBlock = loadPropBool(
            "Revert Kiln Block", 
            "Kiln block gets replaced with the base block when there is no heat source below.", 
            true);
        denyKilnBlockPlacement = loadPropBool(
            "Deny Kiln Block", 
            "Prevent kiln TileEntity creation when the block is placed, unless there is a heat source below.", 
            true);
    }

    @Override
    public boolean hasSubscriptions() {
        return true;
    }

    public static boolean isKilnTile(TileEntity tile) {
        return tile instanceof TileKiln;
    }

    // Turns the kiln block back to its orignal block
    public static boolean disableKiln(World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileKiln)) {
            return false;
        }
        TileKiln tileKiln = (TileKiln) tile;
        tileKiln.doBlockDrop = false;
        world.setBlockState(pos, tileKiln.camoState);
        return true;
    }

    @SubscribeEvent
    public void onKilnBlockCreation(BlockEvent.EntityPlaceEvent event) {
        if(!denyKilnBlockPlacement) {
            return;
        }
        BlockPos kilnPos = event.getPos();
        World world = event.getWorld();
        // Turn the block into a kiln block only if there is a heat source below
        if (isKilnTile(world.getTileEntity(kilnPos)) && BWMHeatRegistry.getHeat(world, event.getPos().down()) > 0) {
            KilnStructureManager.createKiln(world, kilnPos);
        }
    }

    @SubscribeEvent
    public void onHeatSourceChanged(BlockEvent.NeighborNotifyEvent event) {
        if(!revertKilnBlock) {
            return;
        }
        BlockPos kilnPos = event.getPos().up();
        World world = event.getWorld();
        // Reverts the kiln to its orignal block state when there is no more heat source
        if (isKilnTile(world.getTileEntity(kilnPos)) && BWMHeatRegistry.getHeat(world, event.getPos()) <= 0) {
            disableKiln(world, kilnPos);
        }
    }
}