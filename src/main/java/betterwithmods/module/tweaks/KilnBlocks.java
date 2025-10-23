package betterwithmods.module.tweaks;

import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import betterwithmods.common.registry.KilnStructureManager;
import betterwithmods.module.Feature;


public class KilnBlocks extends Feature {

    public String configCategory;
    public String configName;
    public boolean enabledByDefault = true;

    public static HashSet<String> kilnBlocksWhitelist;
    
    @Override
    public String getFeatureDescription() {
        return "Defines valid blocks to form a kiln.";
    }

    @Override
    public void setupConfig() {
        kilnBlocksWhitelist = loadPropStringHashSet(
            "Whitelist", 
            "Valid kiln blocks", 
            new String[]{"minecraft:brick_block"}
        );
    }

    @Override
    public void init(FMLInitializationEvent event) {
        applyWhitelist();
    }

    public static void applyWhitelist() {
        unregisterKilnBlocks();
        registerKilnBlocks();
    }

    public static void registerKilnBlocks() {
        for(String blockName : kilnBlocksWhitelist) {
            Block block = Block.getBlockFromName(blockName);
            if(block == null) {
                continue;
            }
            if(block.getDefaultState().getProperties().get(BlockColored.COLOR) != null) {
                for(EnumDyeColor enumdyecolor : EnumDyeColor.values()) {
                    IBlockState coloredBlockState = block.getDefaultState().withProperty(BlockColored.COLOR, enumdyecolor);
                    if(coloredBlockState != null) {
                        KilnStructureManager.registerKilnBlock(coloredBlockState);
                    }
                }
            }
            else {
                KilnStructureManager.registerKilnBlock(block.getDefaultState());
            }
        }
    }

    public static void unregisterKilnBlocks() {
        KilnStructureManager.KILN_BLOCKS.clear();
    }
}