package codechicken.enderstorage.common;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import codechicken.enderstorage.storage.item.EnderChestRenderer;
import codechicken.enderstorage.storage.liquid.EnderTankRenderer;
import codechicken.enderstorage.storage.liquid.TankSynchroniser;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Vector3;

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
        Vector3 d = new Vector3();
        if (type != ItemRenderType.EQUIPPED_FIRST_PERSON && type != ItemRenderType.EQUIPPED) d.add(-0.5, -0.5, -0.5);

        int freq = item.getItemDamage() & 0xFFF;
        String owner = item.hasTagCompound() ? item.getTagCompound().getString("owner") : "global";
        int rotation = 0;
        if (type == ItemRenderType.ENTITY) rotation = 3;

        final CCRenderState state = CCRenderState.instance();
        switch (item.getItemDamage() >> 12) {
            case 0:
                EnderChestRenderer.renderChest(state, rotation, freq, !owner.equals("global"), d.x, d.y, d.z, 0, 0);
                break;
            case 1:
                state.reset();
                state.pullLightmap();
                state.useNormals = true;
                EnderTankRenderer.renderTank(state, rotation, 0, freq, !owner.equals("global"), d.x, d.y, d.z, 0);
                EnderTankRenderer.renderLiquid(TankSynchroniser.getClientLiquid(freq, owner), d.x, d.y, d.z);
                break;
        }
    }
}
