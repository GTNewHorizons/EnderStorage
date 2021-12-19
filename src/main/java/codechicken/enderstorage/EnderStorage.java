package codechicken.enderstorage;

import java.io.File;

import codechicken.lib.config.ConfigTag;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraft.command.CommandHandler;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import codechicken.core.CommonUtils;
import codechicken.core.launch.CodeChickenCorePlugin;
import codechicken.lib.config.ConfigFile;
import codechicken.enderstorage.api.EnderStorageManager;
import codechicken.enderstorage.common.BlockEnderStorage;
import codechicken.enderstorage.internal.EnderStorageProxy;
import codechicken.enderstorage.storage.EnderItemStoragePlugin;
import codechicken.enderstorage.storage.EnderLiquidStoragePlugin;
import codechicken.enderstorage.storage.item.CommandEnderStorage;
import codechicken.enderstorage.storage.item.ItemEnderPouch;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid = "EnderStorage", name = "EnderStorage", dependencies = "required-after:CodeChickenCore@[" + CodeChickenCorePlugin.version + ",)", acceptedMinecraftVersions = CodeChickenCorePlugin.mcVersion)
public class EnderStorage
{
    @SidedProxy(clientSide = "codechicken.enderstorage.internal.EnderStorageClientProxy", serverSide = "codechicken.enderstorage.internal.EnderStorageProxy")
    public static EnderStorageProxy proxy;

    public static ConfigFile config;

    public static BlockEnderStorage blockEnderChest;
    public static ItemEnderPouch itemEnderPouch;

    public static Item personalItem;
    public static boolean disableVanillaEnderChest;
    public static boolean removeVanillaRecipe;
    public static boolean anarchyMode;
    public static boolean disableFXChest;
    public static boolean disableFXTank;
    public static int enderTankSize;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new ConfigFile(new File(CommonUtils.getMinecraftDir() + "/config", "EnderStorage.cfg")).setComment("EnderStorage Configuration File\nDeleting any element will restore it to it's default value\nBlock ID's will be automatically generated the first time it's run");

        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        loadPersonalItem();
        disableVanillaEnderChest = config.getTag("disable-vanilla").setComment("Set to true to make the vanilla enderchest unplaceable.").getBooleanValue(true);
        removeVanillaRecipe = config.getTag("disable-vanilla_recipe").setComment("Set to true to make the vanilla enderchest uncraftable.").getBooleanValue(false);
        anarchyMode = config.getTag("anarchy-mode").setComment("Causes chests to lose personal settings and drop the diamond on break").getBooleanValue(false);
        disableFXChest = config.getTag("disableFXChest").setComment("Disable the end portal effect in ES ender chests. May help with FPS (not TPS!) problems.").getBooleanValue(false);
        disableFXTank = config.getTag("disableFXTank").setComment("Disable the end portal effect in ender tanks. May help with FPS (not TPS!) problems.").getBooleanValue(false);
        enderTankSize = config.getTag("enderTankSize").setComment("Set the size of ender tanks in buckets (x1000)").getIntValue(256);
        
        EnderStorageManager.loadConfig(config);
        EnderStorageManager.registerPlugin(new EnderItemStoragePlugin());
        EnderStorageManager.registerPlugin(new EnderLiquidStoragePlugin());

        proxy.init();
    }

    private void loadPersonalItem() {
        ConfigTag tag = config.getTag("personalItemID")
                .setComment("The name of the item used to set the chest to personal. Diamond by default");
        String name = tag.getValue("diamond");
        personalItem = (Item) Item.itemRegistry.getObject(name);
        if (personalItem == null) {
            personalItem = Items.diamond;
            tag.setValue("diamond");
        }
    }

    @EventHandler
    public void preServerStart(FMLServerAboutToStartEvent event) {
        EnderStorageManager.reloadManager(false);
    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        CommandHandler commandManager = (CommandHandler) event.getServer().getCommandManager();
        commandManager.registerCommand(new CommandEnderStorage());
    }

    public static ItemStack getPersonalItem() {
        return new ItemStack(personalItem, 1, 0);
    }
}
