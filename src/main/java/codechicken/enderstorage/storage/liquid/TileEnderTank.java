package codechicken.enderstorage.storage.liquid;

import static codechicken.lib.vec.Vector3.*;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import codechicken.core.fluid.FluidUtils;
import codechicken.enderstorage.api.EnderStorageManager;
import codechicken.enderstorage.common.TileFrequencyOwner;
import codechicken.enderstorage.internal.EnderStorageSPH;
import codechicken.enderstorage.storage.liquid.TankSynchroniser.TankState;
import codechicken.lib.math.MathHelper;
import codechicken.lib.packet.PacketCustom;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Scale;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;

public class TileEnderTank extends TileFrequencyOwner implements IFluidHandler {

    public class EnderTankState extends TankState {

        @Override
        public void sendSyncPacket() {
            PacketCustom packet = new PacketCustom(EnderStorageSPH.channel, 5);
            packet.writeCoord(xCoord, yCoord, zCoord);
            packet.writeFluidStack(s_liquid);
            packet.sendToChunk(worldObj, xCoord >> 4, zCoord >> 4);
        }

        @Override
        public void onLiquidChanged() {
            worldObj.func_147451_t(xCoord, yCoord, zCoord);
        }
    }

    public class PressureState {

        public boolean invert_redstone;
        public boolean a_pressure;
        public boolean b_pressure;

        public double a_rotate;
        public double b_rotate;

        public void update(boolean client) {
            if (client) {
                b_rotate = a_rotate;
                a_rotate = MathHelper.approachExp(a_rotate, approachRotate(), 0.5, 20);
            } else {
                b_pressure = a_pressure;
                a_pressure = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord) != invert_redstone;
                if (a_pressure != b_pressure) sendSyncPacket();
            }
        }

        public double approachRotate() {
            return a_pressure ? -90 : 90;
        }

        private void sendSyncPacket() {
            PacketCustom packet = new PacketCustom(EnderStorageSPH.channel, 6);
            packet.writeCoord(xCoord, yCoord, zCoord);
            packet.writeBoolean(a_pressure);
            packet.sendToChunk(worldObj, xCoord >> 4, zCoord >> 4);
        }

        public void invert() {
            invert_redstone = !invert_redstone;
            worldObj.getChunkFromBlockCoords(xCoord, zCoord).setChunkModified();
        }
    }

    private static Cuboid6[] selectionBoxes = new Cuboid6[4];
    public static Transformation[] buttonT = new Transformation[3];

    static {
        for (int i = 0; i < 3; i++) {
            buttonT[i] = new Scale(0.6).with(new Translation(0.35 + i * 0.15, 0.91, 0.5));
            selectionBoxes[i] = selection_button.copy().apply(buttonT[i]);
        }
        selectionBoxes[3] = new Cuboid6(0.358, 0.268, 0.05, 0.662, 0.565, 0.15);
    }

    public int rotation;
    public EnderTankState liquid_state = new EnderTankState();
    public PressureState pressure_state = new PressureState();

    private EnderLiquidStorage storage;
    private boolean described;

    @Override
    public void updateEntity() {
        super.updateEntity();

        pressure_state.update(worldObj.isRemote);
        if (pressure_state.a_pressure) ejectLiquid();

        liquid_state.update(worldObj.isRemote);
    }

    private void ejectLiquid() {
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity t = worldObj.getTileEntity(xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ);
            if (!(t instanceof IFluidHandler)) continue;

            IFluidHandler c = (IFluidHandler) t;
            FluidStack liquid = drain(null, 100, false);
            if (liquid == null) continue;
            int qty = c.fill(side.getOpposite(), liquid, true);
            if (qty > 0) drain(null, qty, true);
        }
    }

    public void reloadStorage() {
        storage = (EnderLiquidStorage) EnderStorageManager.instance(worldObj.isRemote)
                .getStorage(owner, freq, "liquid");
        if (!worldObj.isRemote) liquid_state.reloadStorage(storage);
    }

    @Override
    public EnderLiquidStorage getStorage() {
        return storage;
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        return storage.fill(from, resource, doFill);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return storage.drain(from, maxDrain, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        return storage.drain(from, resource, doDrain);
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return storage.canDrain(from, fluid);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return storage.canFill(from, fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        if (worldObj.isRemote)
            return new FluidTankInfo[] { new FluidTankInfo(liquid_state.s_liquid, EnderLiquidStorage.CAPACITY) };

        return storage.getTankInfo(from);
    }

    @Override
    public void onPlaced(EntityLivingBase entity) {
        rotation = (int) Math.floor(entity.rotationYaw * 4 / 360 + 2.5D) & 3;
        pressure_state.b_rotate = pressure_state.a_rotate = pressure_state.approachRotate();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setByte("rot", (byte) rotation);
        tag.setBoolean("ir", pressure_state.invert_redstone);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        rotation = tag.getByte("rot");
        pressure_state.invert_redstone = tag.getBoolean("ir");
    }

    @Override
    public void writeToPacket(PacketCustom packet) {
        packet.writeByte(rotation);
        packet.writeFluidStack(liquid_state.s_liquid);
        packet.writeBoolean(pressure_state.a_pressure);
    }

    @Override
    public void handleDescriptionPacket(PacketCustom desc) {
        super.handleDescriptionPacket(desc);
        rotation = desc.readUByte();
        liquid_state.s_liquid = desc.readFluidStack();
        pressure_state.a_pressure = desc.readBoolean();
        if (!described) {
            liquid_state.c_liquid = liquid_state.s_liquid;
            pressure_state.b_rotate = pressure_state.a_rotate = pressure_state.approachRotate();
        }
        described = true;
    }

    @Override
    public boolean activate(EntityPlayer player, int subHit) {
        if (subHit == 4) {
            pressure_state.invert();
            return true;
        }

        ItemStack held = player.getCurrentEquippedItem();
        if (held == null) return false;

        // Look for IFluidContainerItem (Large Fluid Cells) - IFCI henceforth
        if (held.getItem() instanceof net.minecraftforge.fluids.IFluidContainerItem) {
            if (handleIFCI(player, held)) return true;
        }

        // If no IFCI then regular Fluid Container logic (IC2 / GT cells / buckets / flasks) - FC henceforth
        if (handleFCFullToEmpty(player, held)) return true;
        if (handleFCEmptyToFull(player, held)) return true;

        return false;
    }

    // Handles the IFCIs
    private boolean handleIFCI(EntityPlayer player, ItemStack held) {
        final net.minecraftforge.fluids.IFluidContainerItem cont = (net.minecraftforge.fluids.IFluidContainerItem) held
                .getItem();

        // Tests for stacked item & unstacks for use
        ItemStack target = held;
        boolean split = false;
        if (held.stackSize > 1) {
            target = held.copy();
            target.stackSize = 1;
            split = true;
        }

        // This is for putting cell fluid -) tank
        net.minecraftforge.fluids.FluidStack inCell = cont.getFluid(target);
        if (inCell != null && inCell.amount > 0) {
            int tankCan = fill(null, inCell, false);
            int wantMove = Math.min(tankCan, inCell.amount);
            if (wantMove > 0) {
                // simulated drain to check if possible
                net.minecraftforge.fluids.FluidStack simDrain = cont.drain(target, wantMove, false);
                if (simDrain != null && simDrain.amount > 0) {
                    if (worldObj.isRemote) return true;

                    // For creative mode makes changes to tank but not for item
                    if (player.capabilities.isCreativeMode) {
                        fill(
                                null,
                                new net.minecraftforge.fluids.FluidStack(simDrain.getFluid(), simDrain.amount),
                                true);
                        return true;
                    }

                    // Actualizing simulated drain if true
                    net.minecraftforge.fluids.FluidStack drained = cont.drain(target, simDrain.amount, true);
                    if (drained != null && drained.amount > 0) {
                        fill(null, drained, true);

                        // If split bc multiple in stack give other cell & sync inv
                        if (split) {
                            held.stackSize--;
                            giveOrDrop(player, target);
                        }
                        syncInv(player);
                    }
                    return true;
                }
            }
        }

        // This is for filling cell from tank
        net.minecraftforge.fluids.FluidStack tankFluid = storage.getFluid();
        if (tankFluid != null && tankFluid.getFluid() != null) {
            final net.minecraftforge.fluids.FluidStack currentFluid = cont.getFluid(target);
            if (currentFluid == null || currentFluid.getFluid() == tankFluid.getFluid()) {
                int capacity = cont.getCapacity(target);
                int remaining = capacity - (currentFluid != null ? currentFluid.amount : 0);
                if (remaining > 0) {
                    int wantMove = Math.min(remaining, tankFluid.amount);
                    if (wantMove > 0) {
                        // Simulated fill
                        int canFill = cont.fill(
                                target,
                                new net.minecraftforge.fluids.FluidStack(tankFluid.getFluid(), wantMove),
                                false);
                        if (canFill > 0) {
                            if (worldObj.isRemote) return true;

                            // Will still drain from tank but not change item if in creative mode
                            if (player.capabilities.isCreativeMode) {
                                net.minecraftforge.fluids.FluidStack pulled = drain(null, canFill, true);
                                return pulled != null && pulled.amount > 0;
                            }

                            // Actualized simulated fill if true
                            net.minecraftforge.fluids.FluidStack pulled = drain(null, canFill, true);
                            if (pulled != null && pulled.amount > 0) {
                                int filled = cont.fill(target, pulled, true);
                                int leftover = pulled.amount - filled;
                                if (leftover > 0) {
                                    fill(
                                            null,
                                            new net.minecraftforge.fluids.FluidStack(pulled.getFluid(), leftover),
                                            true);
                                }

                                // Split cell fill logic
                                if (filled > 0) {
                                    if (split) {
                                        held.stackSize--;
                                        giveOrDrop(player, target);
                                    }
                                    syncInv(player);
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        // False called if not IFCI cell
        return false;
    }

    // For putting FC into tank
    private boolean handleFCFullToEmpty(EntityPlayer player, ItemStack held) {
        // Isolate to one cell
        ItemStack single = held.copy();
        single.stackSize = 1;

        // Check FC, check tank, do fill
        net.minecraftforge.fluids.FluidStack offered = net.minecraftforge.fluids.FluidContainerRegistry
                .getFluidForFilledItem(single);
        if (offered == null || offered.amount <= 0) return false;
        if (fill(null, offered, false) < offered.amount) return false;
        if (worldObj.isRemote) return true;
        fill(null, offered, true);

        // Creative mode exception to alter tank not item
        if (!player.capabilities.isCreativeMode) {
            held.stackSize--;
            if (held.stackSize <= 0) {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }

            // Return empty container & sync if no creative
            ItemStack empty = net.minecraftforge.fluids.FluidContainerRegistry.drainFluidContainer(single);
            if (empty != null) giveOrDrop(player, empty);
            syncInv(player);
        }
        return true;
    }

    // For putting tank liquid into FC
    private boolean handleFCEmptyToFull(EntityPlayer player, ItemStack held) {
        // Get Fluid
        net.minecraftforge.fluids.FluidStack tankFluid = storage.getFluid();
        if (tankFluid == null || tankFluid.getFluid() == null) return false;

        // Isolate to one cell
        ItemStack singleEmpty = held.copy();
        singleEmpty.stackSize = 1;

        // Build fake cell to get mb number (unit) needed to fill cell (typically 1000 or 144)
        ItemStack preview = net.minecraftforge.fluids.FluidContainerRegistry.fillFluidContainer(
                new net.minecraftforge.fluids.FluidStack(tankFluid.getFluid(), tankFluid.amount),
                singleEmpty);
        if (preview == null) return false;
        net.minecraftforge.fluids.FluidStack inside = net.minecraftforge.fluids.FluidContainerRegistry
                .getFluidForFilledItem(preview);
        if (inside == null || inside.amount <= 0) return false;
        int unit = inside.amount;

        // Checks if drain is possible and does if so
        net.minecraftforge.fluids.FluidStack can = drain(null, unit, false);
        if (can == null || can.amount < unit) return false;
        if (worldObj.isRemote) return true;
        drain(null, unit, true);

        // Duck out early if creative since only need tank change
        if (player.capabilities.isCreativeMode) {
            return true;
        }

        // Give the filled cell if not creative & syncinv
        if (held.stackSize > 1) {
            held.stackSize--;
            giveOrDrop(player, preview);
        } else {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, preview);
        }
        syncInv(player);
        return true;
    }

    // Add item to inv or drop if inv full
    private static void giveOrDrop(EntityPlayer player, ItemStack stack) {
        if (stack == null) return;
        boolean added = player.inventory.addItemStackToInventory(stack);
        if (!added) {
            player.dropPlayerItemWithRandomChoice(stack, false);
        }
    }

    // sync for instant client ui update
    private static void syncInv(EntityPlayer player) {
        player.inventory.markDirty();
        if (player instanceof net.minecraft.entity.player.EntityPlayerMP) {
            net.minecraft.entity.player.EntityPlayerMP mp = (net.minecraft.entity.player.EntityPlayerMP) player;
            mp.inventoryContainer.detectAndSendChanges();
            mp.sendContainerToPlayer(mp.inventoryContainer);
        }
    }

    @Override
    public void addTraceableCuboids(List<IndexedCuboid6> cuboids) {
        Vector3 pos = new Vector3(xCoord, yCoord, zCoord);
        cuboids.add(new IndexedCuboid6(0, new Cuboid6(0.15, 0, 0.15, 0.85, 0.916, 0.85).add(pos)));

        for (int i = 0; i < 4; i++) cuboids.add(
                new IndexedCuboid6(
                        i + 1,
                        selectionBoxes[i].copy().apply(Rotation.quarterRotations[rotation ^ 2].at(center)).add(pos)));
    }

    @Override
    public int getLightValue() {
        if (liquid_state.s_liquid.amount > 0)
            return FluidUtils.getLuminosity(liquid_state.c_liquid, liquid_state.s_liquid.amount / 16D);

        return 0;
    }

    @Override
    public boolean redstoneInteraction() {
        return true;
    }

    public void sync(PacketCustom packet) {
        if (packet.getType() == 5) liquid_state.sync(packet.readFluidStack());
        else if (packet.getType() == 6) pressure_state.a_pressure = packet.readBoolean();
    }

    @Override
    public boolean rotate() {
        if (!worldObj.isRemote) {
            rotation = (rotation + 1) % 4;
            PacketCustom.sendToChunk(getDescriptionPacket(), worldObj, xCoord >> 4, zCoord >> 4);
        }

        return true;
    }

    @Override
    public int comparatorInput() {
        FluidTankInfo tank = storage.getTankInfo(null)[0];
        return tank.fluid.amount * 14 / tank.capacity + (tank.fluid.amount > 0 ? 1 : 0);
    }
}
