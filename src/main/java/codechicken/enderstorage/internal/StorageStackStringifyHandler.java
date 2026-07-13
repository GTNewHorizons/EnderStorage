package codechicken.enderstorage.internal;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import codechicken.enderstorage.EnderStorage;
import codechicken.nei.api.IStackStringifyHandler;

public class StorageStackStringifyHandler implements IStackStringifyHandler {

    private final Item itemEnderStorage;

    public StorageStackStringifyHandler() {
        itemEnderStorage = Item.getItemFromBlock(EnderStorage.blockEnderChest);
    }

    // Use the un-dyed version for recipe/usage lookup
    @Override
    public ItemStack normalizeRecipeQueryStack(ItemStack stack) {
        if (stack.getItem() == itemEnderStorage) {
            boolean isTank = stack.getItemDamage() >> 12 == 1;
            return new ItemStack(itemEnderStorage, stack.stackSize, isTank ? 1 << 12 : 0);
        }
        return null;
    }
}
