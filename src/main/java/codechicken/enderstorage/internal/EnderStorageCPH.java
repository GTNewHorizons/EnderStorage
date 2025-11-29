package codechicken.enderstorage.internal;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;

import codechicken.enderstorage.EnderStorage;
import codechicken.enderstorage.api.EnderStorageManager;
import codechicken.enderstorage.common.TileFrequencyOwner;
import codechicken.enderstorage.event.EnderStorageStoredEvent;
import codechicken.enderstorage.storage.item.EnderItemStorage;
import codechicken.enderstorage.storage.liquid.TankSynchroniser;
import codechicken.enderstorage.storage.liquid.TileEnderTank;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.packet.PacketCustom.IClientPacketHandler;
import codechicken.lib.vec.BlockCoord;

public class EnderStorageCPH implements IClientPacketHandler {

    public static final String channel = "ES";

    @Override
    public void handlePacket(PacketCustom packet, Minecraft mc, INetHandlerPlayClient handler) {
        switch (packet.getType()) {
            case 1:
                handleTilePacket(mc.theWorld, packet, packet.readCoord());
                break;
            case 2:
                int windowID = packet.readUByte();

                ((EnderItemStorage) EnderStorageManager.instance(true)
                        .getStorage(packet.readString(), packet.readUShort(), "item")).openClientGui(
                                windowID,
                                mc.thePlayer.inventory,
                                packet.readString(),
                                packet.readUByte());
                break;
            case 3:
                ((EnderItemStorage) EnderStorageManager.instance(true)
                        .getStorage(packet.readString(), packet.readUShort(), "item"))
                                .setClientOpen(packet.readBoolean() ? 1 : 0);
                break;
            case 4:
                TankSynchroniser.syncClient(packet.readUShort(), packet.readString(), packet.readFluidStack());
                break;
            case 5:
            case 6:
                handleTankTilePacket(mc.theWorld, packet.readCoord(), packet);
                break;
            case 7:
                boolean global = packet.readBoolean();
                int type = packet.readInt();
                NBTTagCompound nbtTagCompound = packet.readNBTTagCompound();
                Map<Integer, NBTTagCompound> compoundMap = Arrays.stream(nbtTagCompound.getIntArray("freqs")).boxed()
                        .collect(
                                Collectors.toMap(freq -> freq, freq -> nbtTagCompound.getCompoundTag(freq.toString())));

                updateStorage(global, type, compoundMap);
                MinecraftForge.EVENT_BUS.post(new EnderStorageStoredEvent(global, type));
                break;
        }
    }

    private void handleTankTilePacket(WorldClient world, BlockCoord pos, PacketCustom packet) {
        TileEntity tile = world.getTileEntity(pos.x, pos.y, pos.z);
        if (tile instanceof TileEnderTank) ((TileEnderTank) tile).sync(packet);
    }

    private void handleTilePacket(WorldClient world, PacketCustom packet, BlockCoord pos) {
        TileEntity tile = world.getTileEntity(pos.x, pos.y, pos.z);

        if (tile instanceof TileFrequencyOwner) ((TileFrequencyOwner) tile).handleDescriptionPacket(packet);
    }

    private void updateStorage(boolean global, int type, Map<Integer, NBTTagCompound> compoundMap) {
        EnderStorageManager storageManager = EnderStorageManager.instance(true);
        String owner = global ? "global" : Minecraft.getMinecraft().thePlayer.getDisplayName();
        String typeStr;
        switch (type) {
            case EnderStorageStoredEvent.TYPE_ITEM:
                typeStr = "item";
                break;
            case EnderStorageStoredEvent.TYPE_LIQUID:
                typeStr = "liquid";
                break;
            default:
                EnderStorage.LOGGER
                        .error("EnderStorageCPH:Unknown EnderStorageStoredEvent TYPE,no information is returned.");
                return;
        }
        storageManager.cleanStorage(owner, typeStr);
        for (Map.Entry<Integer, NBTTagCompound> entryMap : compoundMap.entrySet()) {
            storageManager.setStorage(owner, entryMap.getKey(), typeStr, entryMap.getValue());
        }
    }
}
