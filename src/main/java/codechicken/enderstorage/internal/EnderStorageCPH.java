package codechicken.enderstorage.internal;

import net.minecraft.client.Minecraft;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.multiplayer.WorldClient;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.packet.PacketCustom.IClientPacketHandler;
import codechicken.lib.vec.BlockCoord;
import codechicken.enderstorage.api.EnderStorageManager;
import codechicken.enderstorage.common.TileFrequencyOwner;
import codechicken.enderstorage.storage.item.EnderItemStorage;
import codechicken.enderstorage.storage.liquid.TankSynchroniser;
import codechicken.enderstorage.storage.liquid.TileEnderTank;

public class EnderStorageCPH implements IClientPacketHandler
{
    public static final String channel = "ES";

    @Override
    public void handlePacket(PacketCustom packet, Minecraft mc, INetHandlerPlayClient handler) {
        switch (packet.getType()) {
            case 1:
                handleTilePacket(mc.theWorld, packet, packet.readCoord());
                break;
            case 2:
                int windowID = packet.readUByte();

                ((EnderItemStorage) EnderStorageManager.instance(true).getStorage(packet.readString(), packet.readUShort(), "item"))
                        .openClientGui(windowID, mc.thePlayer.inventory, packet.readString(), packet.readUByte());
                break;
            case 3:
                ((EnderItemStorage) EnderStorageManager.instance(true).getStorage(packet.readString(), packet.readUShort(), "item"))
                        .setClientOpen(packet.readBoolean() ? 1 : 0);
                break;
            case 4:
                TankSynchroniser.syncClient(packet.readUShort(), packet.readString(), packet.readFluidStack());
                break;
            case 5:
            case 6:
                handleTankTilePacket(mc.theWorld, packet.readCoord(), packet);
                break;
        }
    }

    private void handleTankTilePacket(WorldClient world, BlockCoord pos, PacketCustom packet) {
        TileEntity tile = world.getTileEntity(pos.x, pos.y, pos.z);
        if (tile instanceof TileEnderTank)
            ((TileEnderTank) tile).sync(packet);
    }

    private void handleTilePacket(WorldClient world, PacketCustom packet, BlockCoord pos) {
        TileEntity tile = world.getTileEntity(pos.x, pos.y, pos.z);

        if (tile instanceof TileFrequencyOwner)
            ((TileFrequencyOwner) tile).handleDescriptionPacket(packet);
    }
}
