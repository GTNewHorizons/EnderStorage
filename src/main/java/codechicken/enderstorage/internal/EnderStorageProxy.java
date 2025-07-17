package codechicken.enderstorage.internal;

import static codechicken.enderstorage.EnderStorage.*;

import net.minecraftforge.common.MinecraftForge;

import codechicken.enderstorage.api.EnderStorageManager.EnderStorageSaveHandler;
import codechicken.enderstorage.common.BlockEnderStorage;
import codechicken.enderstorage.common.EnderStorageRecipe;
import codechicken.enderstorage.common.ItemEnderStorage;
import codechicken.enderstorage.storage.item.ItemEnderPouch;
import codechicken.enderstorage.storage.item.TileEnderChest;
import codechicken.enderstorage.storage.liquid.TankSynchroniser;
import codechicken.enderstorage.storage.liquid.TileEnderTank;
import codechicken.lib.packet.PacketCustom;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;

public class EnderStorageProxy {

    public void init() {
        blockEnderChest = new BlockEnderStorage();
        blockEnderChest.setBlockName("enderchest");
        GameRegistry.registerBlock(blockEnderChest, ItemEnderStorage.class, "enderChest");
        MinecraftForge.EVENT_BUS.register(blockEnderChest.handler);

        itemEnderPouch = new ItemEnderPouch();
        itemEnderPouch.setUnlocalizedName("enderpouch");
        GameRegistry.registerItem(itemEnderPouch, "enderPouch");

        GameRegistry.registerTileEntity(TileEnderChest.class, "Ender Chest");
        GameRegistry.registerTileEntity(TileEnderTank.class, "Ender Tank");

        PacketCustom.assignHandler(EnderStorageSPH.channel, new EnderStorageSPH());
        EnderStorageRecipe.init();
    }

    public void preInit() {
        MinecraftForge.EVENT_BUS.register(new EnderStorageRecipe());
        FMLCommonHandler.instance().bus().register(new EnderStorageSaveHandler());
        MinecraftForge.EVENT_BUS.register(new EnderStorageSaveHandler());
        FMLCommonHandler.instance().bus().register(new TankSynchroniser());
        MinecraftForge.EVENT_BUS.register(new TankSynchroniser());

        if (disableVanillaEnderChest) EnderStorageRecipe.removeVanillaChest();

    }
}
