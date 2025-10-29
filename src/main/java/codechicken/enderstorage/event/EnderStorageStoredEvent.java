package codechicken.enderstorage.event;

import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.eventhandler.Event;

public class EnderStorageStoredEvent extends Event {

    public final String owner;
    public final String type;
    public final Map<Integer, NBTTagCompound> compoundMap;

    public EnderStorageStoredEvent(String owner, String type, Map<Integer, NBTTagCompound> compoundMap) {
        this.owner = owner;
        this.type = type;
        this.compoundMap = compoundMap;
    }
}
