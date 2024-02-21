package com.asdflj.appliedcooking.common.block;

import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.List;

import net.blay09.mods.cookingforblockheads.CookingForBlockheads;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import com.asdflj.appliedcooking.common.item.ItemKitchenStation;
import com.asdflj.appliedcooking.common.tile.TileKitchenStation;
import com.asdflj.appliedcooking.loader.IRegister;
import com.asdflj.appliedcooking.util.NameConst;
import com.asdflj.appliedcooking.util.Util;

import appeng.api.AEApi;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.features.IWirelessTermRegistry;
import appeng.core.localization.PlayerMessages;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockKitchenStation extends BaseBlockContainer implements IRegister<BlockKitchenStation> {

    public BlockKitchenStation() {
        super(Material.iron);
        this.setBlockName(NameConst.BLOCK_KITCHEN_STATION);
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public BlockKitchenStation register() {
        GameRegistry.registerBlock(this, ItemKitchenStation.class, NameConst.BLOCK_KITCHEN_STATION);
        GameRegistry.registerTileEntity(TileKitchenStation.class, NameConst.BLOCK_KITCHEN_STATION);
        setCreativeTab(CookingForBlockheads.creativeTab);
        return this;
    }

    public TileEntity getTileEntity(World worldObj, int x, int y, int z) {
        return worldObj.getTileEntity(x, y, z);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int facing, float hitX,
        float hitY, float hitZ) {
        if (Platform.isClient()) return true;
        TileKitchenStation tile = (TileKitchenStation) getTileEntity(world, x, y, z);
        if (tile == null) return false;
        IWirelessTermRegistry term = AEApi.instance()
            .registries()
            .wireless();
        ItemStack is = player.getCurrentEquippedItem();
        if (is != null && is.getItem() instanceof ToolWirelessTerminal) {
            final IWirelessTermHandler handler = term.getWirelessTerminalHandler(is);
            final String unparsedKey = handler.getEncryptionKey(is);
            if (unparsedKey.equals("")) {
                player.addChatMessage(PlayerMessages.DeviceNotLinked.get());
                return false;
            }
            final long parsedKey = Long.parseLong(unparsedKey);
            final ILocatable securityStation = Util.getSecurityStation(parsedKey);
            if (securityStation == null) {
                player.addChatMessage(PlayerMessages.StationCanNotBeLocated.get());
                return false;
            }
            tile.setKey(parsedKey);
        } else {
            final ILocatable securityStation = Util.getSecurityStation(tile.getKey());
            if (securityStation != null) {
                player.addChatMessage(new ChatComponentText(I18n.format(NameConst.TT_CONNECTED)));
            } else {
                player.addChatMessage(PlayerMessages.StationCanNotBeLocated.get());
                return false;
            }
        }
        return true;
    }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        TileKitchenStation tile = (TileKitchenStation) getTileEntity(worldIn, x, y, z);
        if (tile != null) {
            tile.removeListener();
        }
        super.breakBlock(worldIn, x, y, z, blockBroken, meta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack itemStack, final EntityPlayer player, final List<String> toolTip,
        final boolean advancedToolTips) {
        if (isShiftKeyDown()) {
            toolTip.add(I18n.format(NameConst.TT_KITCHEN_STATION));
        } else {
            toolTip.add(I18n.format(NameConst.TT_SHIFT_FOR_MORE));
        }

    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileKitchenStation();
    }
}
