package com.asdflj.appliedcooking.common.tile;

import net.blay09.mods.cookingforblockheads.api.kitchen.IKitchenStorageProvider;
import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.world.World;

import com.asdflj.appliedcooking.util.Util;
import com.asdflj.appliedcooking.util.WirelessObject;

import appeng.api.implementations.IPowerChannelState;
import appeng.tile.AEBaseTile;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.util.Platform;

public class TileKitchenStation extends AEBaseTile implements IKitchenStorageProvider, IPowerChannelState {

    private long securityKey;
    private final WirelessObject wirelessObject;
    private boolean isPowered;

    public TileKitchenStation() {
        this.wirelessObject = new WirelessObject(this);
    }

    public void setKey(long key) {
        this.securityKey = key;
    }

    public long getKey() {
        return this.securityKey;
    }

    @Override
    public IInventory getInventory() {
        if (this.wirelessObject.reCheckIsConnect()) {
            return this.wirelessObject.getInventory();
        }
        return Util.EmptyInventory;
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBTEvent(NBTTagCompound data) {
        this.setKey(data.getLong("key"));
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public NBTTagCompound writeToNBTEvent(NBTTagCompound data) {
        data.setLong("key", this.getKey());
        return data;
    }

    private void onComparatorUpdate(World world, int x, int y, int z, Block block) {
        world.func_147453_f(x, y, z, block);
    }

    private void updatePowerState() {
        boolean newState = this.wirelessObject.reCheckIsConnect();
        if (newState != this.isPowered) {
            this.isPowered = newState;
            this.onComparatorUpdate(this.worldObj, this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
            this.markForUpdate();
        }
    }

    @TileEvent(TileEventType.TICK)
    public void update() {
        if (Platform.isClient()) return;
        this.updatePowerState();
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        writeToNBTWithoutCoords(nbtTag);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        super.onDataPacket(net, packet);
        this.readFromNBTWithoutCoords(packet.func_148857_g());
    }

    private void writeToNBTWithoutCoords(NBTTagCompound tag) {
        tag.setBoolean("powered", this.isPowered);
    }

    public void readFromNBTWithoutCoords(NBTTagCompound tag) {
        this.isPowered = tag.getBoolean("powered");
    }

    @Override
    public boolean isPowered() {
        return isPowered;
    }

    @Override
    public boolean isActive() {
        return isPowered;
    }
}
