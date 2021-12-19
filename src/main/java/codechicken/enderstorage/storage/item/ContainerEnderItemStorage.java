package codechicken.enderstorage.storage.item;

import codechicken.enderstorage.storage.EnderItemStoragePlugin;
import net.minecraft.inventory.Container;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.Slot;

public class ContainerEnderItemStorage extends Container
{
    public EnderItemStorage chestInv;

    public ContainerEnderItemStorage(IInventory invplayer, EnderItemStorage chestInv, boolean client)
    {
        this.chestInv = chestInv;
        chestInv.openInventory();
        
        switch(chestInv.getSize())
        {
            case 0:
                for(int row = 0; row < 3; ++row)
                    for(int col = 0; col < 3; ++col)
                        addSlotToContainer(new Slot(chestInv, col + row * 3, 62 + col * 18, 17 + row * 18));
                addPlayerSlots(invplayer, 84);
                break;
            case 1:
                for(int row = 0; row < 3; ++row)
                    for(int col = 0; col < 9; ++col)
                        addSlotToContainer(new Slot(chestInv, col + row * 9, 8 + col * 18, 18 + row * 18));
                addPlayerSlots(invplayer, 85);
                break;
            case 2:
                for(int row = 0; row < 6; ++row)
                    for(int col = 0; col < 9; ++col)
                        addSlotToContainer(new Slot(chestInv, col + row * 9, 8 + col * 18, 18 + row * 18));
                addPlayerSlots(invplayer, 140);
                break;
        }
        
    }

    private void addPlayerSlots(IInventory invplayer, int yOffset)
    {
        for(int row = 0; row < 3; row++)
            for(int col = 0; col < 9; col++)
                addSlotToContainer(new Slot(invplayer, col + row * 9 + 9, 8 + col * 18, yOffset + row * 18));
        
        for(int col = 0; col < 9; col++)
            addSlotToContainer(new Slot(invplayer, col, 8 + col * 18, yOffset+58));
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer)
    {
        return chestInv.isUseableByPlayer(entityplayer);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i)
    {
        ItemStack itemstack = null;
        Slot slot = (Slot)inventorySlots.get(i);
        
        if(slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            
            int chestSlots = EnderItemStoragePlugin.sizes[chestInv.getSize()];
            if(i < chestSlots)
            {
                if(!mergeItemStack(itemstack1, chestSlots, inventorySlots.size(), true))
                {
                    return null;
                }
            } 
            else if(!mergeItemStack(itemstack1, 0, chestSlots, false))
            {
                return null;
            }
            if(itemstack1.stackSize == 0)
            {
                slot.putStack(null);
            } else
            {
                slot.onSlotChanged();
            }
        }
        return itemstack;
    }

    @Override
    public void onContainerClosed(EntityPlayer entityplayer)
    {
        super.onContainerClosed(entityplayer);
        chestInv.closeInventory();
    }
}
