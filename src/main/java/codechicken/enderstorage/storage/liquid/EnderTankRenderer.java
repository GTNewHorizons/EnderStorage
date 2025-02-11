package codechicken.enderstorage.storage.liquid;

import java.util.ArrayList;
import java.util.Map;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import codechicken.core.ClientUtils;
import codechicken.core.fluid.FluidUtils;
import codechicken.enderstorage.EnderStorage;
import codechicken.enderstorage.api.EnderStorageManager;
import codechicken.enderstorage.common.RenderCustomEndPortal;
import codechicken.enderstorage.common.RenderEnderStorage;
import codechicken.enderstorage.internal.EnderStorageClientProxy;
import codechicken.lib.math.MathHelper;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCModelLibrary;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.render.uv.UVTranslation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.SwapYZ;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;

public class EnderTankRenderer extends TileEntitySpecialRenderer {

    private static final ResourceLocation ENDERTANK_TEXTURE = new ResourceLocation(
            "enderstorage:textures/endertank.png");
    private static final ResourceLocation BUTTONS_TEXTURE = new ResourceLocation("enderstorage:textures/buttons.png");
    private static final ResourceLocation HEDRON_TEXTURE = new ResourceLocation("enderstorage:textures/hedronmap.png");

    private static final CCModel tankModel;
    private static final CCModel valveModel;
    private static final CCModel[] buttons;

    private static final UVTranslation[] UVTranslationButtons = new UVTranslation[16];
    private static final UVTranslation UVTvalveOwned = new UVTranslation(0, 13 / 64D);
    private static final UVTranslation UVTvalveNotOwned = new UVTranslation(0, 0);
    private static final Vector3 Y = new Vector3(0, 1, 0);
    private static final Vector3 Z = new Vector3(0, 0, 1);
    private static final Vector3 point = new Vector3(0, 0.4165, 0);

    private static final RenderCustomEndPortal renderEndPortal = new RenderCustomEndPortal(
            0.1205,
            0.24,
            0.76,
            0.24,
            0.76);

    static {
        Map<String, CCModel> models = CCModel
                .parseObjModels(new ResourceLocation("enderstorage", "models/endertank.obj"), new SwapYZ());
        ArrayList<CCModel> tankParts = new ArrayList<>();
        tankParts.add(models.get("Blazerod1"));
        tankParts.add(models.get("Blazerod2"));
        tankParts.add(models.get("Blazerod3"));
        tankParts.add(models.get("Blazerod4"));
        tankParts.add(models.get("Top"));
        tankParts.add(models.get("Top2"));
        tankParts.add(models.get("Base"));
        tankParts.add(models.get("Glass"));
        tankParts.add(models.get("Valvebase"));

        Transformation fix = new Translation(-0.0099 - 0.5, 0, -0.0027 - 0.5);

        tankModel = CCModel.combine(tankParts).apply(fix).computeNormals();
        valveModel = models.get("Valve").apply(fix).computeNormals();

        buttons = new CCModel[3];
        for (int i = 0; i < 3; i++) {
            buttons[i] = RenderEnderStorage.button.copy()
                    .apply(TileEnderTank.buttonT[i].with(new Translation(-0.5, 0, -0.5)));
        }

        for (int colour = 0; colour < 16; colour++) {
            UVTranslationButtons[colour] = new UVTranslation(0.25 * (colour % 4), 0.25 * (colour / 4));
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f) {
        TileEnderTank tank = (TileEnderTank) tile;

        final CCRenderState state = CCRenderState.instance();
        state.resetInstance();
        state.pullLightmapInstance();
        state.useNormals = true;

        renderTank(
                state,
                tank.rotation,
                (float) MathHelper.interpolate(tank.pressure_state.b_rotate, tank.pressure_state.a_rotate, f)
                        * 0.01745F,
                tank.freq,
                !tank.owner.equals("global"),
                x,
                y,
                z,
                EnderStorageClientProxy.getTimeOffset(tile.xCoord, tile.yCoord, tile.zCoord),
                shouldRenderFx(tile)); // only render when within 16 blocks
        renderLiquid(tank.liquid_state.c_liquid, x, y, z);
    }

    private static boolean shouldRenderFx(TileEntity tile) {
        final TileEntityRendererDispatcher info = TileEntityRendererDispatcher.instance;
        final double viewX = info.field_147560_j;
        final double viewY = info.field_147561_k;
        final double viewZ = info.field_147558_l;
        return tile.getDistanceFrom(viewX, viewY, viewZ) < 256d;
    }

    /**
     * @param renderFx set to true to render the portal texture and the floating hedron
     */
    public static void renderTank(CCRenderState state, int rotation, float valve, int freq, boolean owned, double x,
            double y, double z, int offset, boolean renderFx) {
        if (renderFx && !EnderStorage.disableFXTank) {
            renderEndPortal.renderAt(x, y, z);
        }
        GL11.glColor4f(1, 1, 1, 1);

        GL11.glEnable(GL11.GL_NORMALIZE);
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        GL11.glRotatef(-90 * (rotation + 2), 0, 1, 0);

        CCRenderState.changeTexture(ENDERTANK_TEXTURE);
        state.startDrawingInstance(4);
        tankModel.render();
        state.drawInstance();

        CCRenderState.changeTexture(BUTTONS_TEXTURE);
        state.startDrawingInstance(7);
        for (int i = 0; i < 3; i++) {
            int colour = EnderStorageManager.getColourFromFreq(freq, i);
            buttons[i].render(UVTranslationButtons[colour]);
        }
        state.drawInstance();

        new Rotation(valve, Z).at(point).glApply();

        CCRenderState.changeTexture(ENDERTANK_TEXTURE);
        state.startDrawingInstance(4);
        valveModel.render(owned ? UVTvalveOwned : UVTvalveNotOwned);
        state.drawInstance();
        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_NORMALIZE);

        if (renderFx) {
            double time = ClientUtils.getRenderTime() + offset;
            Matrix4 pearlMat = CCModelLibrary.getRenderMatrix(
                    x + 0.5,
                    y + 0.45 + EnderStorageClientProxy.getPearlBob(time) * 2,
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

    public static void renderLiquid(FluidStack liquid, double x, double y, double z) {
        RenderUtils.renderFluidCuboid(
                liquid,
                new Cuboid6(0.22 + x, 0.12 + y, 0.22 + z, 0.78 + x, 0.121 + 0.63 + y, 0.78 + z),
                liquid.amount / ((double) EnderStorage.enderTankSize * FluidUtils.B),
                0.75);
    }
}
