package codechicken.enderstorage.api;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;

public class EnderStorageHandleManager {

    private static final Map<String, IHandleStorageInfo> handleStorageInfoMap = new HashMap<>();

    public interface IHandleStorageInfo {

        void handlePacket(String owner, String type, NBTTagCompound[] compounds);
    }

    public static void registerHandleStorageInfo(String type, IHandleStorageInfo handleStorageInfo) {
        handleStorageInfoMap.put(type, handleStorageInfo);
    }

    public static Collection<IHandleStorageInfo> getHandleStorageInfoList() {
        return handleStorageInfoMap.values();
    }
}
