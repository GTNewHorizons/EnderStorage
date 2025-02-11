package codechicken.enderstorage.storage.item;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import codechicken.core.ClientUtils;
import codechicken.enderstorage.EnderStorage;
import codechicken.enderstorage.api.EnderStorageManager;
import codechicken.enderstorage.common.RenderCustomEndPortal;
import codechicken.enderstorage.internal.EnderStorageClientProxy;
import codechicken.lib.render.CCModelLibrary;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;

public class EnderChestRenderer extends TileEntitySpecialRenderer {

    private static final ResourceLocation ENDERCHEST_TEXTURE = new ResourceLocation(
            "enderstorage:textures/enderchest.png");
    private static final ResourceLocation BUTTONS_TEXTURE = new ResourceLocation("enderstorage:textures/buttons.png");
    private static final ResourceLocation HEDRON_TEXTURE = new ResourceLocation("enderstorage:textures/hedronmap.png");
    private static final ModelEnderChest model = new ModelEnderChest();
    private static final Vector3 Y = new Vector3(0, 1, 0);

    public EnderChestRenderer() {}

    public static void renderChest(CCRenderState state, int rotation, int freq, boolean owned, double x, double y,
            double z, int offset, float lidAngle) {
        final boolean isChestOpen = lidAngle < 0f;
        if (isChestOpen && !EnderStorage.disableFXChest) {
            renderEndPortal.renderAt(x, y, z);
        }
        GL11.glColor4f(1, 1, 1, 1);

        CCRenderState.changeTexture(ENDERCHEST_TEXTURE);
        GL11.glEnable(GL11.GL_NORMALIZE);
        GL11.glPushMatrix();
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glTranslated(x, y + 1.0, z + 1.0F);
        GL11.glScalef(1.0F, -1F, -1F);
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
        GL11.glRotatef(rotation * 90, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        model.chestLid.rotateAngleX = lidAngle;
        model.render(owned);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        CCRenderState.changeTexture(BUTTONS_TEXTURE);
        drawButton(0, EnderStorageManager.getColourFromFreq(freq, 0), rotation, lidAngle);
        drawButton(1, EnderStorageManager.getColourFromFreq(freq, 1), rotation, lidAngle);
        drawButton(2, EnderStorageManager.getColourFromFreq(freq, 2), rotation, lidAngle);
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_NORMALIZE);

        if (isChestOpen) {
            double time = ClientUtils.getRenderTime() + offset;
            Matrix4 pearlMat = CCModelLibrary.getRenderMatrix(
                    x + 0.5,
                    y + 0.2 + lidAngle * -0.5 + EnderStorageClientProxy.getPearlBob(time),
                    z + 0.5,
                    new Rotation(time / 3, Y),
                    0.04);

            GL11.glDisable(GL11.GL_LIGHTING);
            CCRenderState.changeTexture(HEDRON_TEXTURE);
            state.startDrawingInstance(4);
            CCModelLibrary.icosahedron4.render(pearlMat);
            state.drawInstance();
            GL11.glEnable(GL11.GL_LIGHTING);
        }
    }

    private static void drawButton(int button, int colour, int rot, double lidAngle) {
        float texx = 0.25F * (colour % 4);
        float texy = 0.25F * (colour / 4);

        GL11.glPushMatrix();

        EnderDyeButton ebutton = TileEnderChest.buttons[button].copy();
        ebutton.rotate(0, 0.5625, 0.0625, 1, 0, 0, lidAngle);
        ebutton.rotateMeta(rot);
        Vector3[] verts = ebutton.verts;

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        addVecWithUV(verts[7], texx + 0.0938, texy + 0.0625);
        addVecWithUV(verts[3], texx + 0.0938, texy + 0.1875);
        addVecWithUV(verts[2], texx + 0.1562, texy + 0.1875);
        addVecWithUV(verts[6], texx + 0.1562, texy + 0.0625);

        addVecWithUV(verts[4], texx + 0.0938, texy + 0.0313);
        addVecWithUV(verts[7], texx + 0.0938, texy + 0.0313);
        addVecWithUV(verts[6], texx + 0.1562, texy + 0.0624);
        addVecWithUV(verts[5], texx + 0.1562, texy + 0.0624);

        addVecWithUV(verts[0], texx + 0.0938, texy + 0.2186);
        addVecWithUV(verts[1], texx + 0.1562, texy + 0.2186);
        addVecWithUV(verts[2], texx + 0.1562, texy + 0.1876);
        addVecWithUV(verts[3], texx + 0.0938, texy + 0.1876);

        addVecWithUV(verts[6], texx + 0.1563, texy + 0.0626);
        addVecWithUV(verts[2], texx + 0.1563, texy + 0.1874);
        addVecWithUV(verts[1], texx + 0.1874, texy + 0.1874);
        addVecWithUV(verts[5], texx + 0.1874, texy + 0.0626);

        addVecWithUV(verts[7], texx + 0.0937, texy + 0.0626);
        addVecWithUV(verts[4], texx + 0.0626, texy + 0.0626);
        addVecWithUV(verts[0], texx + 0.0626, texy + 0.1874);
        addVecWithUV(verts[3], texx + 0.0937, texy + 0.1874);
        tessellator.draw();

        GL11.glPopMatrix();
    }

    private static void addVecWithUV(Vector3 vec, double u, double v) {
        Tessellator.instance.addVertexWithUV(vec.x, vec.y, vec.z, u, v);
    }

    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        final CCRenderState state = CCRenderState.instance();
        state.resetInstance();
        state.setBrightnessInstance(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
        state.useNormals = true;

        TileEnderChest chest = (TileEnderChest) tile;
        renderChest(
                state,
                chest.rotation,
                chest.freq,
                !chest.owner.equals("global"),
                x,
                y,
                z,
                EnderStorageClientProxy.getTimeOffset(chest.xCoord, chest.yCoord, chest.zCoord),
                chest.getRadianLidAngle(partialTicks));
    }

    public static final double phi = 1.618034;

    static RenderCustomEndPortal renderEndPortal = new RenderCustomEndPortal(0.626, 0.188, 0.812, 0.188, 0.812);
}
