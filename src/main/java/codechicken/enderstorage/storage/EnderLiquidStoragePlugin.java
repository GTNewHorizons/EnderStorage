package codechicken.enderstorage.storage;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.FluidStack;

import codechicken.enderstorage.api.AbstractEnderStorage;
import codechicken.enderstorage.api.EnderStorageManager;
import codechicken.enderstorage.api.EnderStoragePlugin;
import codechicken.enderstorage.storage.liquid.EnderLiquidStorage;
import codechicken.lib.config.ConfigTag;

public class EnderLiquidStoragePlugin implements EnderStoragePlugin {

    @Override
    public AbstractEnderStorage createEnderStorage(EnderStorageManager manager, String owner, int freq) {
        return new EnderLiquidStorage(manager, owner, freq);
    }

    @Override
    public String identifer() {
        return "liquid";
    }

    public void loadConfig(ConfigTag config) {}

    @Override
    public void sendClientInfo(EntityPlayer player, List<AbstractEnderStorage> list) {}

    public static boolean isEmpty(EnderLiquidStorage storage) {
        FluidStack fluidStack = storage.getFluid();
        return fluidStack == null || fluidStack.amount == 0;
    }
}
