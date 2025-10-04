package codechicken.enderstorage.api;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;

public class EnderStorageHandleManager {

    private static final Map<String, IHandleStorageInfo> handleStorageInfoMap = new HashMap<>();

    public interface IHandleStorageInfo {

        void handle(String owner, String type, Map<Integer, NBTTagCompound> compoundMap);
    }

    public static void registerHandleStorageInfo(String type, IHandleStorageInfo handleStorageInfo) {
        handleStorageInfoMap.put(type, handleStorageInfo);
    }

    public static void execHandleStorageInfo(String owner, String type, Map<Integer, NBTTagCompound> compoundMap) {
        handleStorageInfoMap.values().forEach(handler -> handler.handle(owner, type, compoundMap));
    }
}
