package codechicken.enderstorage.storage.item;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

public class ItemEnderPouchRenderer implements IItemRenderer {

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return helper == ItemRendererHelper.ENTITY_BOBBING;
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        GL11.glPushMatrix();
        switch (type) {
            case INVENTORY:
                GL11.glScalef(16, 16, 0);
                break;
            case EQUIPPED:
            case ENTITY:
                GL11.glTranslatef(1, 1, 0);
                GL11.glScalef(-1, -1, 1);
        }
        final Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(0, 0, 0, 0, 0);
        tess.addVertexWithUV(0, 1, 0, 0, 0.0625);
        tess.addVertexWithUV(1, 1, 0, 0.0625, 0.0625);
        tess.addVertexWithUV(1, 0, 0, 0.0625, 0);
        tess.addVertexWithUV(0, 0, 0, 0, 0);
        tess.addVertexWithUV(1, 0, 0, 0.0625, 0);
        tess.addVertexWithUV(1, 1, 0, 0.0625, 0.0625);
        tess.addVertexWithUV(0, 1, 0, 0, 0.0625);
        tess.draw();
        GL11.glPopMatrix();
    }
}
