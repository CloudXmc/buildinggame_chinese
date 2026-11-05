package com.gmail.stefvanschiedev.buildinggame.utils.guis.moboptions.mobs.wolf;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A menu for changing a wolf's variant.
 *
 * @since 12.5.0
 */
class WolfVariantMenu extends ChestGui {

    /**
     * Creates a menu to change the provided wolf's variant.
     *
     * @param wolf the wolf to change
     * @since 12.5.0
     */
    WolfVariantMenu(@NotNull Wolf wolf) {
        super(1, ChatColor.GREEN + "狼的变种");

        OutlinePane pane = new OutlinePane(9, 1);

        pane.setOnClick(event -> event.setCancelled(true));

        pane.addItem(new GuiItem(itemStackWithName(Material.SNOWBALL, ChatColor.GREEN + "雪白"), event ->
            wolf.setVariant(Wolf.Variant.SNOWY)));
        pane.addItem(new GuiItem(itemStackWithName(Material.DARK_OAK_SAPLING, ChatColor.GREEN + "条纹"), event ->
            wolf.setVariant(Wolf.Variant.STRIPED)));
        pane.addItem(new GuiItem(itemStackWithName(Material.TERRACOTTA, ChatColor.GREEN + "斑点"), event ->
            wolf.setVariant(Wolf.Variant.SPOTTED)));
        pane.addItem(new GuiItem(itemStackWithName(Material.COPPER_INGOT, ChatColor.GREEN + "锈色"), event ->
            wolf.setVariant(Wolf.Variant.RUSTY)));
        pane.addItem(new GuiItem(itemStackWithName(Material.PITCHER_POD, ChatColor.GREEN + "栗色"), event ->
            wolf.setVariant(Wolf.Variant.CHESTNUT)));
        pane.addItem(new GuiItem(itemStackWithName(Material.BLACK_DYE, ChatColor.GREEN + "黑色"), event ->
            wolf.setVariant(Wolf.Variant.BLACK)));
        pane.addItem(new GuiItem(itemStackWithName(Material.FIREWORK_STAR, ChatColor.GREEN + "灰烬"), event ->
            wolf.setVariant(Wolf.Variant.ASHEN)));
        pane.addItem(new GuiItem(itemStackWithName(Material.OAK_SAPLING, ChatColor.GREEN + "森林"), event ->
            wolf.setVariant(Wolf.Variant.WOODS)));
        pane.addItem(new GuiItem(itemStackWithName(Material.WHITE_DYE, ChatColor.GREEN + "苍白"), event ->
            wolf.setVariant(Wolf.Variant.PALE)));

        addPane(Slot.fromXY(0, 0), pane);
    }

    /**
     * Generates an item stack for the given material and name. The name will be applied as the display name.
     *
     * @param material the material of the item stack
     * @param name the display name of the item stack
     * @return the created item stack
     * @since 12.5.0
     */
    @NotNull
    @Contract(pure = true)
    private ItemStack itemStackWithName(@NotNull Material material, @NotNull String name) {
        var itemStack = new ItemStack(material);
        var itemMeta = itemStack.getItemMeta();

        if (itemMeta != null) {
            itemMeta.setDisplayName(name);
            itemStack.setItemMeta(itemMeta);
        }

        return itemStack;
    }
}
