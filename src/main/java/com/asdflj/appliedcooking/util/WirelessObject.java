package com.asdflj.appliedcooking.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import com.asdflj.appliedcooking.common.tile.TileKitchenStation;
import com.asdflj.appliedcooking.inventory.AppEngInternalInventoryBridge;
import com.asdflj.appliedcooking.inventory.IWirelessObject;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.features.ILocatable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.core.AELog;
import appeng.tile.inventory.InvOperation;

public class WirelessObject implements IWirelessObject {

    private IStorageGrid sg;
    private IGrid targetGrid;
    private IGridNode iGridNode;
    private IMEMonitor<IAEItemStack> itemStorage;
    private final TileKitchenStation tile;
    private AppEngInternalInventoryBridge inv;
    private final MachineSource source;
    private boolean needUpdate = false;
    private long lastKey;

    public WirelessObject(TileKitchenStation tile) {
        this.tile = tile;
        this.source = new MachineSource(this);
        this.reinitialize();
    }

    public boolean reCheckIsConnect() {
        if (lastKey != tile.getKey()) {
            this.reinitialize();
        }
        ILocatable obj = null;
        try {
            obj = AEApi.instance()
                .registries()
                .locatable()
                .getLocatableBy(tile.getKey());

        } catch (final NumberFormatException err) {
            // :P
        }
        if (obj instanceof IGridHost ig) {
            final IGridNode n = ig.getGridNode(ForgeDirection.UNKNOWN);
            return n.isActive();
        }
        return false;
    }

    public void reinitialize() {
        ILocatable obj = null;

        try {
            obj = AEApi.instance()
                .registries()
                .locatable()
                .getLocatableBy(tile.getKey());
        } catch (final NumberFormatException err) {
            // :P
        }
        if (obj instanceof IGridHost) {
            final IGridNode n = ((IGridHost) obj).getGridNode(ForgeDirection.UNKNOWN);
            if (n != null) {
                this.iGridNode = n;
                this.targetGrid = n.getGrid();
                if (targetGrid != null) {
                    this.removeListener(this); // remove old listener
                    this.lastKey = this.tile.getKey();
                    this.sg = targetGrid.getCache(IStorageGrid.class);
                    if (this.sg != null) {
                        this.itemStorage = this.sg.getItemInventory();
                        this.addListener(this, this.getItemStorage());
                    }
                }
            }
        }
    }

    @Override
    public IMEMonitor<IAEItemStack> getItemInventory() {
        if (this.sg == null) {
            return null;
        }
        return this.sg.getItemInventory();
    }

    @Override
    public IMEMonitor<IAEFluidStack> getFluidInventory() {
        if (this.sg == null) {
            return null;
        }
        return this.sg.getFluidInventory();
    }

    @Override
    public IConfigManager getConfigManager() {
        return null;
    }

    @Override
    public IItemList<IAEItemStack> getAvailableItems(IItemList out) {
        if (this.itemStorage != null) {
            return this.itemStorage.getAvailableItems(out);
        }
        return out;
    }

    @Override
    public IItemList<IAEItemStack> getStorageList() {
        if (this.itemStorage != null) {
            return this.itemStorage.getStorageList();
        }
        return null;
    }

    @Override
    public void addListener(IMEMonitorHandlerReceiver<IAEItemStack> l, Object verificationToken) {
        if (this.itemStorage != null) {
            this.itemStorage.addListener(l, verificationToken);
        }
    }

    @Override
    public void removeListener(IMEMonitorHandlerReceiver<IAEItemStack> l) {
        if (this.itemStorage != null) {
            this.itemStorage.removeListener(l);
        }
    }

    @Override
    public AccessRestriction getAccess() {
        if (this.itemStorage != null) {
            return this.itemStorage.getAccess();
        }
        return AccessRestriction.NO_ACCESS;
    }

    @Override
    public boolean isPrioritized(IAEItemStack input) {
        if (this.itemStorage != null) {
            return this.itemStorage.isPrioritized(input);
        }
        return false;
    }

    @Override
    public boolean canAccept(IAEItemStack input) {
        if (this.itemStorage != null) {
            return this.itemStorage.canAccept(input);
        }
        return false;
    }

    @Override
    public int getPriority() {
        if (this.itemStorage != null) {
            return this.itemStorage.getPriority();
        }
        return 0;
    }

    @Override
    public int getSlot() {
        if (this.itemStorage != null) {
            return this.itemStorage.getSlot();
        }
        return 0;
    }

    @Override
    public boolean validForPass(int i) {
        return this.itemStorage.validForPass(i);
    }

    @Override
    public IAEItemStack injectItems(IAEItemStack input, Actionable type, BaseActionSource src) {
        if (this.itemStorage != null) {
            return this.itemStorage.injectItems(input, type, src);
        }
        return input;
    }

    @Override
    public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource src) {
        if (this.itemStorage != null) {
            return this.itemStorage.extractItems(request, mode, src);
        }
        return null;
    }

    @Override
    public StorageChannel getChannel() {
        if (this.itemStorage != null) {
            return this.itemStorage.getChannel();
        }
        return StorageChannel.ITEMS;
    }

    @Override
    public IInventory getInventory() {
        if (this.itemStorage != null) {
            if (this.needUpdate || this.inv == null) {
                this.inv = new AppEngInternalInventoryBridge(this);
                int i = 0;
                for (IAEItemStack is : this.itemStorage.getStorageList()) {
                    inv.push(i, is.getItemStack());
                    i++;
                }
                this.needUpdate = false;
            }
            return inv;
        }
        return null;
    }

    @Override
    public void saveChanges() {

    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack resultStack,
        ItemStack oldStack) {
        IAEItemStack stored;
        if (resultStack == null) {
            // removed all item
            IAEItemStack is = AEApi.instance()
                .storage()
                .createItemStack(oldStack);
            stored = this.getStorageList()
                .findPrecise(is);
            if (stored != null && stored.getStackSize() != 0) {
                this.extractItems(stored, Actionable.MODULATE, this.source);
            } else {
                AELog.error("Cache breakdown stored item is null, cannot extract item");
                this.needUpdate = true;
            }
        } else {
            IAEItemStack is = AEApi.instance()
                .storage()
                .createItemStack(resultStack);
            stored = this.getStorageList()
                .findPrecise(is);
            if (stored != null && stored.getStackSize() != 0) {
                if (stored.getStackSize() > Integer.MAX_VALUE) {
                    is.incStackSize(stored.getStackSize() - Integer.MAX_VALUE);
                }
                stored.decStackSize(is.getStackSize());
                this.extractItems(stored, Actionable.MODULATE, this.source);
                stored = this.getStorageList()
                    .findPrecise(is);
                inv.getStackInSlot(slot).stackSize = stored.getItemStack().stackSize; // refill itemStack
            } else {
                AELog.error("Cache breakdown stored item is null, cannot extract item");
                this.needUpdate = true;
            }
        }
    }

    @Override
    public IGridNode getActionableNode() {
        return this.iGridNode;
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return this.iGridNode;
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return null;
    }

    @Override
    public void securityBreak() {

    }

    public IMEMonitor<IAEItemStack> getItemStorage() {
        return this.itemStorage;
    }

    @Override
    public boolean isValid(Object verificationToken) {
        return this.itemStorage != null && this.itemStorage == verificationToken /* && !conflict */;
    }

    @Override
    public void postChange(IBaseMonitor<IAEItemStack> monitor, Iterable<IAEItemStack> change,
        BaseActionSource actionSource) {
        this.needUpdate = true;
    }

    @Override
    public void onListUpdate() {

    }
}
