package codechicken.enderstorage.event;

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

    public EnderStorageStoredEvent(boolean global, int type) {
        this.global = global;
        this.type = type;
    }
}
