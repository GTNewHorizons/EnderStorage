package codechicken.enderstorage.internal;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import codechicken.enderstorage.EnderStorage;
import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

public class NEIEnderStorageConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        if (EnderStorage.disableVanillaEnderChest) API.hideItem(new ItemStack(Blocks.ender_chest));
    }

    @Override
    public String getName() {
        return "EnderStorage";
    }

    @Override
    public String getVersion() {
        return "1";
    }
}
