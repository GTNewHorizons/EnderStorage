package codechicken.enderstorage.api;

import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractEnderStorage
{
    public final EnderStorageManager manager;
    public final int freq;
    public final String owner;
    private boolean dirty;
    private int changeCount;
    
    public AbstractEnderStorage(EnderStorageManager manager, String owner, int freq)
    {
        this.manager = manager;
        this.owner = owner;
        this.freq = freq;
    }
    
    public void setDirty()
    {
        if(manager.client)
            return;
        
        if(!dirty)
        {
            dirty = true;
            manager.requestSave(this);
        }
        changeCount++;
    }
    
    public void setClean()
    {
        dirty = false;
    }
    
    public int getChangeCount()
    {
        return changeCount;
    }
    
    public abstract String type();

    public abstract NBTTagCompound saveToTag();
    
    public abstract void loadFromTag(NBTTagCompound tag);
}