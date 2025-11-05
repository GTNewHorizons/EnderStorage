package codechicken.enderstorage.event;

import java.util.Map;

import codechicken.enderstorage.api.AbstractEnderStorage;
import codechicken.enderstorage.storage.EnderItemStoragePlugin;
import codechicken.enderstorage.storage.EnderLiquidStoragePlugin;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.eventhandler.Event;

public class EnderStorageStoredEvent extends Event {

    /**
     * global or playerName
     */
    public final String owner;
    /**
     * storage type
     * 1-item or 2-liquid
     * {@link EnderItemStoragePlugin#index}
     * {@link EnderLiquidStoragePlugin#index}
     */
    public final int type;
    /**
     * stored data
     * key: channel
     * value: the corresponding stored nbt form{@link AbstractEnderStorage#saveToTag()}
     */
    public final Map<Integer, NBTTagCompound> compoundMap;

    public EnderStorageStoredEvent(String owner, int type, Map<Integer, NBTTagCompound> compoundMap) {
        this.owner = owner;
        this.type = type;
        this.compoundMap = compoundMap;
    }
}
