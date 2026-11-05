package com.gmail.stefvanschiedev.buildinggame.utils.guis.moboptions.mobs.axolotl;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.util.Slot;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Axolotl;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * A menu for changing an axolotl's variant.
 *
 * @since 10.0.0
 */
class AxolotlVariantMenu extends ChestGui {

    /**
     * Create a menu to change the specified axolotl's variant.
     *
     * @param axolotl the axolotl to change
     * @since 10.0.0
     */
    AxolotlVariantMenu(@NotNull Axolotl axolotl) {
        super(1, ChatColor.GREEN + "美西螈变种");

        var pane = new OutlinePane(5, 1);

        //lucy
        var lucy = new ItemStack(Material.PINK_WOOL);
        ItemMeta lucyMeta = lucy.getItemMeta();
        lucyMeta.setDisplayName(ChatColor.GREEN + "露西");
        lucy.setItemMeta(lucyMeta);

        pane.addItem(new GuiItem(lucy, event -> {
            axolotl.setVariant(Axolotl.Variant.LUCY);

            event.setCancelled(true);
        }));

        //wild
        var wild = new ItemStack(Material.BROWN_WOOL);
        ItemMeta wildMeta = wild.getItemMeta();
        wildMeta.setDisplayName(ChatColor.GREEN + "野生");
        wild.setItemMeta(wildMeta);

        pane.addItem(new GuiItem(wild, event -> {
            axolotl.setVariant(Axolotl.Variant.WILD);

            event.setCancelled(true);
        }));

        //gold
        var gold = new ItemStack(Material.YELLOW_WOOL);
        ItemMeta goldMeta = gold.getItemMeta();
        goldMeta.setDisplayName(ChatColor.GREEN + "金色");
        gold.setItemMeta(goldMeta);

        pane.addItem(new GuiItem(gold, event -> {
            axolotl.setVariant(Axolotl.Variant.GOLD);

            event.setCancelled(true);
        }));

        //cyan
        var cyan = new ItemStack(Material.CYAN_WOOL);
        ItemMeta cyanMeta = cyan.getItemMeta();
        cyanMeta.setDisplayName(ChatColor.GREEN + "青色");
        cyan.setItemMeta(cyanMeta);

        pane.addItem(new GuiItem(cyan, event -> {
            axolotl.setVariant(Axolotl.Variant.CYAN);

            event.setCancelled(true);
        }));

        //blue
        var blue = new ItemStack(Material.BLUE_WOOL);
        ItemMeta blueMeta = blue.getItemMeta();
        blueMeta.setDisplayName(ChatColor.GREEN + "蓝色");
        blue.setItemMeta(blueMeta);

        pane.addItem(new GuiItem(blue, event -> {
            axolotl.setVariant(Axolotl.Variant.BLUE);

            event.setCancelled(true);
        }));

        addPane(Slot.fromXY(2, 0), pane);
    }
}
