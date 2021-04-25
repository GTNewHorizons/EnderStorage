package codechicken.enderstorage.storage.item;

import codechicken.enderstorage.api.EnderStorageManager;
import codechicken.enderstorage.common.EnderStorageRecipe;
import codechicken.enderstorage.storage.liquid.EnderLiquidStorage;
import codechicken.lib.render.CCRenderState;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.swing.*;

@SideOnly(Side.CLIENT)
public class GuiEnderViewer extends GuiContainer
{
    EntityPlayer player;
    int firstColor;
    static int marginUP = 0;
    static int marginDown = 0;
    static int marginLeft = 0;
    static int marginRight = 50;
    static int marginInnerX = 10;
    static int marginInnerY = 2;
    static int textureSize = 8;
    static String[] dyeList = {"white", "orange", "magenta", "lblue",
                                "yellow", "lime", "pink", "gray",
                                "lgray", "cyan", "purple", "blue",
                                "brown", "green", "red", "black",};

    public GuiEnderViewer(EntityPlayer inputPlayer)
    {
        super(new ContainerEnderViewer(inputPlayer));
        player = inputPlayer;
        firstColor = 0;
        this.xSize = textureSize * 16 + marginInnerX * 15 + marginLeft + marginRight;
        this.ySize = textureSize * 16 + marginInnerY * 15 + marginUP + marginDown;
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        firstColor = button.id;
    }


    @Override
    public void initGui()
    {
        super.initGui();
        this.buttonList.clear();
        int offsetX = (this.width - this.xSize) / 2, offsetY = (this.height - this.ySize) / 2;
        for (int i = 0; i < 16; i++) {
            this.buttonList.add(new GuiButton(i,
                    offsetX + textureSize * 16 + marginInnerX * 15 + marginLeft + 10,
                    offsetY + textureSize * i + marginInnerY * i,
                    40, 8,
                    StatCollector.translateToLocal("enderstorage.colo." + dyeList[i])));
        }
        Mouse.setGrabbed(false);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2)
    {
        EnderStorageManager manager = EnderStorageManager.instance(false);
        String playerName = ((player == null) ? null : player.getDisplayName());

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                int freq = manager.getFreqFromColours(j, i, firstColor);
                FluidStack fluid = ((EnderLiquidStorage) manager.getStorage(playerName, freq, "liquid")).getFluid();
                if(fluid.amount == 0) continue;

                Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glColor3f(1.0F, 1.0F, 1.0F);
                GL11.glColor3f((fluid.getFluid().getColor() >> 16 & 0xFF) / 255.0F, (fluid.getFluid().getColor() >> 8 & 0xFF) / 255.0F, (fluid.getFluid().getColor() & 0xFF) / 255.0F);
                drawTexturedModelRectFromIcon(textureSize * j + marginInnerX * j + marginLeft, textureSize * i + marginInnerY * i + marginUP,
                        fluid.getFluid().getIcon(), textureSize, textureSize);
                GL11.glColor3f(1.0F, 1.0F, 1.0F);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_BLEND);

            }
        }
    }
    
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
    {
        drawRect((this.width - this.xSize) / 2, (this.height - this.ySize) / 2, (this.width - this.xSize) / 2 + this.xSize, (this.height - this.ySize) / 2 + this.ySize, 0xFFffffff);
        drawRect((this.width - this.xSize) / 2 + this.xSize - 50, (this.height - this.ySize) / 2, (this.width - this.xSize) / 2 + this.xSize - 40, (this.height - this.ySize) / 2 + this.ySize, 0xFF000000);
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        super.drawScreen(par1, par2, par3);
        EnderStorageManager manager = EnderStorageManager.instance(false);
        String playerName = player == null ? null : player.getDisplayName();
        int mouseOnGUIX = par1 - (this.width - this.xSize) / 2;
        int mouseOnGuiY = par2 - (this.height - this.ySize) / 2;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                int freq = manager.getFreqFromColours(j, i, firstColor);
                FluidStack fluid = ((EnderLiquidStorage) manager.getStorage(playerName, freq, "liquid")).getFluid();
                if(fluid.amount == 0) continue;
                if (textureSize * j + marginInnerX * j + marginLeft < mouseOnGUIX
                        && textureSize * (j + 1) + marginInnerX * j + marginLeft > mouseOnGUIX
                        && textureSize * i + marginInnerY * i + marginUP < mouseOnGuiY
                        && textureSize * (i + 1) + marginInnerY * i + marginUP > mouseOnGuiY) {
                    String titleStr = " " + fluid.getLocalizedName();
                    String amountStr = " " +  fluid.amount + "ml";
                    String colorStr = " " + StatCollector.translateToLocal("enderstorage.colo." + dyeList[firstColor]) +
                            " " + StatCollector.translateToLocal("enderstorage.colo." + dyeList[i]) +
                            " " + StatCollector.translateToLocal("enderstorage.colo." + dyeList[j]);

                    int maxStrLen = Math.max(this.fontRendererObj.getStringWidth(titleStr), this.fontRendererObj.getStringWidth(amountStr));
                    maxStrLen = Math.max(maxStrLen, this.fontRendererObj.getStringWidth(colorStr));
                    int fixPar1 = (par1 + maxStrLen > this.width) ? (this.width - maxStrLen): par1;

                    drawRect(fixPar1, par2, fixPar1 + maxStrLen, par2 + 32, 0xFF000000);
                    this.fontRendererObj.drawString(titleStr, fixPar1, par2 + 8, 0xffffff);
                    this.fontRendererObj.drawString(amountStr, fixPar1, par2 + 16, 0xffffff);
                    this.fontRendererObj.drawString(colorStr, fixPar1, par2 + 24, 0xffffff);
                    return;
                }
            }
        }
    }
}
