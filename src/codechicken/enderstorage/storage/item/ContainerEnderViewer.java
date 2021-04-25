package codechicken.enderstorage.storage.item;

import codechicken.enderstorage.api.EnderStorageManager;
import codechicken.enderstorage.storage.EnderItemStoragePlugin;
import codechicken.enderstorage.storage.liquid.EnderLiquidStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerEnderViewer extends Container
{
    EntityPlayer player;
    public ContainerEnderViewer(EntityPlayer inputPlayer)
    {
        player = inputPlayer;
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer)
    {
        return true;
    }
}
