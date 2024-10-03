package codechicken.enderstorage.common;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import codechicken.enderstorage.storage.item.EnderChestRenderer;
import codechicken.enderstorage.storage.liquid.EnderTankRenderer;
import codechicken.enderstorage.storage.liquid.TankSynchroniser;
import codechicken.lib.render.CCRenderState;

public class ItemEnderStorageRenderer implements IItemRenderer {

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        final double x;
        final double y;
        final double z;
        if (type != ItemRenderType.EQUIPPED_FIRST_PERSON && type != ItemRenderType.EQUIPPED) {
            x = -0.5;
            y = -0.5;
            z = -0.5;
        } else {
            x = 0;
            y = 0;
            z = 0;
        }
        int freq = item.getItemDamage() & 0xFFF;
        String owner = item.hasTagCompound() ? item.getTagCompound().getString("owner") : "global";
        int rotation = 0;
        if (type == ItemRenderType.ENTITY) rotation = 3;

        final CCRenderState state = CCRenderState.instance();
        switch (item.getItemDamage() >> 12) {
            case 0:
                EnderChestRenderer.renderChest(state, rotation, freq, !owner.equals("global"), x, y, z, 0, 0);
                break;
            case 1:
                state.resetInstance();
                state.pullLightmapInstance();
                state.useNormals = true;
                EnderTankRenderer.renderTank(state, rotation, 0, freq, !owner.equals("global"), x, y, z, 0, false);
                EnderTankRenderer.renderLiquid(TankSynchroniser.getClientLiquid(freq, owner), x, y, z);
                break;
        }
    }
}
