package com.gmail.stefvanschiedev.buildinggame.utils.guis.moboptions.mobs;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.gmail.stefvanschiedev.buildinggame.utils.guis.moboptions.BabyMenu;
import com.gmail.stefvanschiedev.buildinggame.utils.plot.Plot;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Bee;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * A menu for changing the options of a bee.
 *
 * @since 8.0.0
 */
public class BeeMenu extends BabyMenu {

    /**
     * {@inheritDoc}
     */
    public BeeMenu(@NotNull Plot plot, @NotNull Bee bee) {
        super(plot, bee);

        //angry
        ItemStack angry = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta angryMeta = angry.getItemMeta();
        angryMeta.setDisplayName(ChatColor.GREEN + "更改此蜜蜂是否愤怒");
        angry.setItemMeta(angryMeta);

        pane.insertItem(new GuiItem(angry, event -> {
            bee.setAnger(bee.getAnger() > 0 ? 0 : Integer.MAX_VALUE);

            event.setCancelled(true);
        }), 0);

        //pollen
        ItemStack pollen = new ItemStack(Material.AZURE_BLUET);
        ItemMeta pollenMeta = pollen.getItemMeta();
        pollenMeta.setDisplayName(ChatColor.GREEN + "更改此蜜蜂是否带着花粉");
        pollen.setItemMeta(pollenMeta);

        pane.insertItem(new GuiItem(pollen, event -> {
            bee.setHasNectar(!bee.hasNectar());

            event.setCancelled(true);
        }), 1);

        //stinger
        ItemStack stinger = new ItemStack(Material.STICK);
        ItemMeta stingerMeta = stinger.getItemMeta();
        stingerMeta.setDisplayName(ChatColor.GREEN + "更改此蜜蜂是否有螫针");
        stinger.setItemMeta(stingerMeta);

        pane.insertItem(new GuiItem(stinger, event -> {
            bee.setHasStung(!bee.hasStung());

            event.setCancelled(true);
        }), 2);
    }
}
