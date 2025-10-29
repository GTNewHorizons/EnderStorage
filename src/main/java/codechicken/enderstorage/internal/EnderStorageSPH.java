package codechicken.enderstorage.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;

import codechicken.enderstorage.EnderStorage;
import codechicken.enderstorage.api.EnderStorageManager;
import codechicken.enderstorage.storage.EnderItemStoragePlugin;
import codechicken.enderstorage.storage.EnderLiquidStoragePlugin;
import codechicken.enderstorage.storage.item.EnderItemStorage;
import codechicken.enderstorage.storage.liquid.EnderLiquidStorage;
import codechicken.enderstorage.storage.liquid.TankSynchroniser;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.packet.PacketCustom.IServerPacketHandler;

public class EnderStorageSPH implements IServerPacketHandler {

    public static final String channel = "ES";

    @Override
    public void handlePacket(PacketCustom packet, EntityPlayerMP sender, INetHandlerPlayServer handler) {
        switch (packet.getType()) {
            case 1:
                TankSynchroniser.handleVisiblityPacket(sender, packet);
                break;
            case 2:
                sendStorageStored(packet, sender);
                break;
        }
    }

    private void sendStorageStored(PacketCustom packet, EntityPlayerMP sender) {
        String owner = packet.readString();
        String type = packet.readString();
        if (Objects.equals(owner, "global")) {
            if (!EnderStorage.allPlayerCanSeePublicInventory && MinecraftServer.getServer().getConfigurationManager()
                    .func_152603_m().func_152700_a(sender.getDisplayName()) == null) {
                return;
            }
        } else {
            owner = sender.getDisplayName();
        }

        PacketCustom res = new PacketCustom(channel, 7);
        res.writeString(owner);
        res.writeString(type);

        EnderStorageManager storageManager = EnderStorageManager.instance(false);

        NBTTagCompound data = new NBTTagCompound();
        List<Integer> freqList = new ArrayList<>();
        String owner_ = owner;
        switch (type) {
            case "item":
                Map<Integer, EnderItemStorage> chestMap = new LinkedHashMap<>();
                IntStream.rangeClosed(0, 0xFFF).forEach(
                        freq -> chestMap.put(freq, (EnderItemStorage) storageManager.getStorage(owner_, freq, type)));

                chestMap.entrySet().stream().filter(entry -> !EnderItemStoragePlugin.isEmpty(entry.getValue()))
                        .forEach(chest -> {
                            freqList.add(chest.getKey());
                            data.setTag(chest.getKey().toString(), chest.getValue().saveToTag());
                        });
                break;
            case "liquid":
                Map<Integer, EnderLiquidStorage> liquidMap = new LinkedHashMap<>();
                IntStream.rangeClosed(0, 0xFFF).forEach(
                        freq -> liquidMap
                                .put(freq, (EnderLiquidStorage) storageManager.getStorage(owner_, freq, type)));

                liquidMap.entrySet().stream().filter(entry -> !EnderLiquidStoragePlugin.isEmpty(entry.getValue()))
                        .forEach(tank -> {
                            freqList.add(tank.getKey());
                            data.setTag(tank.getKey().toString(), tank.getValue().saveToTag());
                        });
                break;
        }
        data.setIntArray("freqs", freqList.stream().mapToInt(i -> i).toArray());

        res.writeNBTTagCompound(data);
        res.sendToPlayer(sender);
    }

    public static void sendOpenUpdateTo(EntityPlayer player, String owner, int freq, boolean open) {
        PacketCustom packet = new PacketCustom(channel, 3);
        packet.writeString(owner);
        packet.writeShort(freq);
        packet.writeBoolean(open);

        packet.sendToPlayer(player);
    }
}
