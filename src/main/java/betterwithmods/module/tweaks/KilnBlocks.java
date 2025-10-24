package betterwithmods.module.tweaks;

import java.util.Collection;
import java.util.LinkedHashSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import betterwithmods.common.registry.KilnStructureManager;
import betterwithmods.module.Feature;


public class KilnBlocks extends Feature {

    public boolean enabledByDefault = true;

    public static boolean forbidBlockRegistration;
    public static String[] kilnBlocksWhitelist;
    
    @Override
    public String getFeatureDescription() {
        return "Defines valid blocks to form a kiln.";
    }

    @Override
    public void setupConfig() {
        forbidBlockRegistration = loadPropBool(
            "Forbid Blocks Registration", 
            "Prevent other mods from registering their own valid kiln blocks.", 
            true
        );
        kilnBlocksWhitelist = loadPropStringList(
            "Whitelist", 
            "Valid kiln blocks.", 
            new String[]{"minecraft:brick_block"}
        );
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        if(forbidBlockRegistration) {
            KilnStructureManager.KILN_BLOCKS = new FakeSet<IBlockState>();
        }
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
                        registerKilnBlock(coloredBlockState);
                    }
                }
            }
            else {
                registerKilnBlock(block.getDefaultState());
            }
        }
    }

    public static void registerKilnBlock(IBlockState kilnBlockState) {
        if(forbidBlockRegistration) {
            ((FakeSet<IBlockState>) KilnStructureManager.KILN_BLOCKS).addForReal(kilnBlockState);
        }
        else {
            KilnStructureManager.KILN_BLOCKS.add(kilnBlockState);
        }
    }

    public static void unregisterKilnBlocks() {
        KilnStructureManager.KILN_BLOCKS.clear();
    }

    public static class FakeSet<T> extends LinkedHashSet<T> {

        @Override
        public boolean add(T element) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends T> elements) {
            return false;
        }

        public boolean addForReal(T element) {
            return super.add(element);
        }

        public boolean addAllForReal(Collection<? extends T> elements) {
            return super.addAll(elements);
        }
    }
}