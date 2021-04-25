package codechicken.enderstorage.storage.item;

import codechicken.enderstorage.EnderStorage;
import codechicken.enderstorage.api.EnderStorageManager;
import codechicken.lib.render.SpriteSheetManager;
import codechicken.lib.render.SpriteSheetManager.SpriteSheet;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

public class ItemEnderViewer extends Item
{
    private static String[] typeList = {"Fluid_Global", "Fluid_Self"};

    public ItemEnderViewer()
    {
        setMaxStackSize(1);
        //setHasSubtypes(true);
        setCreativeTab(CreativeTabs.tabDecorations);
    }
    
    @Override
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player)
    {
        if(world.isRemote)
            return item;

        NBTTagCompound nbt;
        if (item.hasTagCompound())
        {
            nbt = item.getTagCompound();
        }
        else
        {
            nbt = new NBTTagCompound();
        }
        if(!nbt.hasKey("Modes"))
        {
            nbt.setInteger("Modes", 0);
        }
        if(player.isSneaking())
        {
            if(nbt.hasKey("Modes"))
            {
                nbt.setInteger("Modes", (nbt.getInteger("Modes") + 1) % typeList.length);
            }
            else
            {
                nbt.setInteger("Modes", 0);
            }
            item.setTagCompound(nbt);
            
            return item;
        }

        Minecraft mc = Minecraft.getMinecraft();
        switch (nbt.getInteger("Modes")) {
            case 0:
                mc.displayGuiScreen(new GuiEnderViewer(null));
                break;
            case 1:
                mc.displayGuiScreen(new GuiEnderViewer(player));
                break;
        }
        return item;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register)
    {
        itemIcon = register.registerIcon("enderstorage:enderviewer");
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltips, boolean b)
    {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Modes"))
        {
            tooltips.add(StatCollector.translateToLocal("enderstorage.modes." + typeList[stack.getTagCompound().getInteger("Modes")]));
        }
    }
}
