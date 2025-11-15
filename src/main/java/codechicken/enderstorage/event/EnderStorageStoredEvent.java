package codechicken.enderstorage.event;

import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;

import codechicken.enderstorage.api.AbstractEnderStorage;
import cpw.mods.fml.common.eventhandler.Event;

public class EnderStorageStoredEvent extends Event {

    public static final int TYPE_ITEM = 1;
    public static final int TYPE_LIQUID = 2;

    /**
     * is global? or private
     */
    public final boolean global;
    /**
     * storage type 1-item or 2-liquid
     */
    public final int type;
    /**
     * stored data <br/>
     * key: channel <br/>
     * value: the corresponding stored nbt form{@link AbstractEnderStorage#saveToTag()}
     */
    public final Map<Integer, NBTTagCompound> compoundMap;

    public EnderStorageStoredEvent(boolean global, int type, Map<Integer, NBTTagCompound> compoundMap) {
        this.global = global;
        this.type = type;
        this.compoundMap = compoundMap;
    }
}
