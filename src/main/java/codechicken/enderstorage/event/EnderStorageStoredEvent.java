package codechicken.enderstorage.event;

import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;

import codechicken.enderstorage.api.AbstractEnderStorage;
import codechicken.enderstorage.storage.EnderItemStoragePlugin;
import codechicken.enderstorage.storage.EnderLiquidStoragePlugin;
import cpw.mods.fml.common.eventhandler.Event;

public class EnderStorageStoredEvent extends Event {

    /**
     * global or playerName
     */
    public final String owner;
    /**
     * storage type 1-item or 2-liquid <br/>
     * {@link EnderItemStoragePlugin#index} <br/>
     * {@link EnderLiquidStoragePlugin#index}
     */
    public final int type;
    /**
     * stored data <br/>
     * key: channel <br/>
     * value: the corresponding stored nbt form{@link AbstractEnderStorage#saveToTag()}
     */
    public final Map<Integer, NBTTagCompound> compoundMap;

    public EnderStorageStoredEvent(String owner, int type, Map<Integer, NBTTagCompound> compoundMap) {
        this.owner = owner;
        this.type = type;
        this.compoundMap = compoundMap;
    }
}
