package codechicken.enderstorage.api;

import net.minecraft.item.ItemStack;

/**
 * Allows {@link net.minecraft.item.Item items} to register themselves as valid tools for dyeing a deployed ender chest
 * or tank. Setting the frequency of an ender storage block through crafting, while using one of these tools, is not
 * supported.
 */
public interface EnderStorageDyeTool {

    /**
     * Return the dye color that this tool is currently set to use.
     *
     * @return a dye index number. Return -1 if the tool does not provide dye.
     */
    int getDye(ItemStack itemStack);

    /**
     * This method will be called when the tool is being used, to allow the tool to expend a charge or consume power.
     * <p>
     * Defaults to nothing happening.
     */
    @SuppressWarnings("unused")
    default void expendToolUse(ItemStack itemStack) {}
}
