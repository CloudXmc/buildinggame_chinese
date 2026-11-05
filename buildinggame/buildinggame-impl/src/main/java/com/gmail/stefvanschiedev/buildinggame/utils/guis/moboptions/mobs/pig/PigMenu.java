package com.gmail.stefvanschiedev.buildinggame.utils.guis.moboptions.mobs.pig;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.gmail.stefvanschiedev.buildinggame.utils.guis.moboptions.BabyMenu;
import com.gmail.stefvanschiedev.buildinggame.utils.nms.Version;
import com.gmail.stefvanschiedev.buildinggame.utils.plot.Plot;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Pig;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * A menu for changing the options of a pig
 *
 * @since 5.3.0
 */
public class PigMenu extends BabyMenu {

    /**
     * {@inheritDoc}
     */
    public PigMenu(Plot plot, Pig pig) {
        super(plot, pig);

        //saddle
        var saddle = new ItemStack(Material.SADDLE);
        var saddleMeta = saddle.getItemMeta();
        saddleMeta.setDisplayName(ChatColor.GREEN + "更改此猪是否装备鞍");
        saddle.setItemMeta(saddleMeta);

        pane.insertItem(new GuiItem(saddle, event -> {
            pig.setSaddle(!pig.hasSaddle());

            event.setCancelled(true);
        }), 0);

        if (Version.getVersion().isAtLeast(Version.V1_21_5)) {
            //variant
            var variant = new ItemStack(Material.WHITE_DYE);
            var variantMeta = variant.getItemMeta();
            Objects.requireNonNull(variantMeta).setDisplayName(ChatColor.GREEN + "更改猪的变种");
            variant.setItemMeta(variantMeta);

            super.pane.insertItem(new GuiItem(variant, event -> {
                new PigVariantMenu(pig).show(event.getWhoClicked());

                event.setCancelled(true);
            }), 0);
        }
    }
}
