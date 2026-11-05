package com.gmail.stefvanschiedev.buildinggame.managers.messages;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import com.gmail.stefvanschiedev.buildinggame.managers.files.SettingsManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for sending and modifying messages
 *
 * @since 2.1.0
 */
public final class MessageManager {

    /**
     * Constructs a new MessageManager. This shouldn't be called to keep this class a singleton.
     */
	private MessageManager() {}

	/**
     * An instance of this class for the singleton principle
     */
	private static final MessageManager INSTANCE = new MessageManager();

	/**
     * Returns the instance of this singleton class
     *
     * @return an instance of this class
     * @since 2.1.0
     */
	@NotNull
	@Contract(pure = true)
    public static MessageManager getInstance() {
		return INSTANCE;
	}

	/**
     * Sends a single message to the player with the prefix appended to the message and the message variables edited
     *
     * @param sender the sender to send the message to
     * @param message the message to send
     * @since 2.1.0
     */
	@Contract("_, null -> fail; null, !null -> fail")
	@SuppressWarnings("MethodMayBeStatic")
    public void send(CommandSender sender, String message) {
		if (message.isEmpty())
			return;

		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && sender instanceof Player) {
		    var player = (Player) sender;

            sender.sendMessage(translate(SettingsManager.getInstance().getMessages()
                    .getString("global.prefix"), player) + translate(message, player));
        } else
            sender.sendMessage(translate(SettingsManager.getInstance().getMessages()
                    .getString("global.prefix")) + translate(message));
	}

	/**
     * Sends multiple messages to the player with the prefix appended to the message and the message variables edited
     *
     * @param sender the sender to send the message to
     * @param messages an iterable containing all messages
     * @since 2.1.0
     */
	@SuppressWarnings("MethodMayBeStatic")
    public void send(CommandSender sender, Iterable<? extends String> messages) {
		for (String message : messages) {
			if (message.isEmpty())
				return;

            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && sender instanceof Player) {
                Player player = (Player) sender;

                sender.sendMessage(translate(SettingsManager.getInstance().getMessages()
                        .getString("global.prefix"), player) + translate(message, player));
            } else
                sender.sendMessage(translate(SettingsManager.getInstance().getMessages()
                        .getString("global.prefix")) + translate(message));
		}
	}

	/**
     * Returns the string provided with all variables edited to their values
     *
     * @param s the string to edit the variables in
     * @return the modified string
     * @since 2.1.0
     */
	@NotNull
    @Contract(value = "null -> fail", pure = true)
    public static String translate(String s) {
		return ChatColor.translateAlternateColorCodes('&', s
            .replace("%:a%", "ä")
            .replace("%:e%", "ë")
            .replace("%:i%", "ï")
            .replace("%:o%", "ö")
            .replace("%:u%", "ü")
            .replace("%:A%", "Ä")
            .replace("%:E%", "Ë")
            .replace("%:I%", "Ï")
            .replace("%:O%", "Ö")
            .replace("%:U%", "Ü")
            .replace("%/a%", "á")
            .replace("%/e%", "é")
            .replace("%/i%", "í")
            .replace("%/o%", "ó")
            .replace("%/u%", "ú")
            .replace("%ss%", "ß")
            .replace("%xayxay%", "塞尔达摇篮曲")
            .replace("%axyaxy%", "艾波娜之歌")
            .replace("%ryxryx%", "萨莉亚之歌")
            .replace("%yrayra%", "太阳之歌")
            .replace("%ylrylr%", "时之歌")
            .replace("%lralra%", "风暴之歌")
            .replace("%laxyxy%", "森林小步舞曲")
            .replace("%rlrlyryr%", "火焰波莱罗")
            .replace("%lryyx%", "水之小夜曲")
            .replace("%lrlyrl%", "灵魂安魂曲")
            .replace("%xyylxyr%", "暗影夜曲")
            .replace("%ayayxa%", "光之序曲")
            .replace("%xyrxyr%", "治愈之歌")
            .replace("%rxarxa%", "飞翔之歌")
            .replace("%axaxlyl%", "觉醒奏鸣曲")
            .replace("%lyxlyxyl%", "哥隆摇篮曲")
            .replace("%xaxyrxy%", "新浪波萨诺瓦")
            .replace("%yxyryax%", "虚无挽歌")
            .replace("%yrlrya%", "秩序之誓")
            .replace("%rlyrly%", "逆转时之歌")
            .replace("%yyllrr%", "双倍时之歌"));
	}

    /**
     * Does the same as {@link #translate(String)}, but it now also replaces PlaceholderAPI placeholders when it's
     * enabled
     *
     * @param s the string to edit the placeholders in
     * @param player the player to use for PlaceholderAPI placeholders
     * @return the modified string
     * @since 5.4.0
     */
    @NotNull
    @Contract(value = "null, _ -> fail", pure = true)
	public static String translate(String s, Player player) {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") ?
                PlaceholderAPI.setPlaceholders((OfflinePlayer) player, translate(s)) : translate(s);
    }

	/**
     * Returns a list containing all provided strings with all variables edited to their corresponding values
     *
     * @param s an iterable containing all strings that should be modified
     * @return a list of strings which have been modified
     * @since 2.1.0
     */
	@NotNull
    @Contract(pure = true)
	public static List<String> translate(Collection<String> s) {
		return s.stream().map(MessageManager::translate).collect(Collectors.toList());
	}

    /**
     * Does the same as {@link #translate(Collection)}, but it now also replaced PlaceholderAPI placeholders when it's
     * enabled
     *
     * @param s an iterable containing all strings that should bhe modified
     * @param player the player to use for PlaceholderAPI placeholders
     * @return a list of strings which have been modified
     * @since 5.4.0
     */
    @NotNull
    @Contract(pure = true)
    public static List<String> translate(Collection<String> s, Player player) {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") ?
                PlaceholderAPI.setPlaceholders((OfflinePlayer) player, translate(s)) : translate(s);
    }
}