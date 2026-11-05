package com.gmail.stefvanschiedev.buildinggame.managers.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import com.gmail.stefvanschiedev.buildinggame.Main;
import com.gmail.stefvanschiedev.buildinggame.game.BuildingGamePhase;
import com.gmail.stefvanschiedev.buildinggame.game.LobbyGamePhase;
import com.gmail.stefvanschiedev.buildinggame.game.VotingGamePhase;
import com.gmail.stefvanschiedev.buildinggame.game.util.GamePhase;
import com.gmail.stefvanschiedev.buildinggame.game.util.SubjectVoting;
import com.gmail.stefvanschiedev.buildinggame.managers.arenas.ArenaManager;
import com.gmail.stefvanschiedev.buildinggame.managers.files.SettingsManager;
import com.gmail.stefvanschiedev.buildinggame.managers.mainspawn.MainSpawnManager;
import com.gmail.stefvanschiedev.buildinggame.managers.messages.MessageManager;
import com.gmail.stefvanschiedev.buildinggame.managers.stats.StatManager;
import com.gmail.stefvanschiedev.buildinggame.timers.*;
import com.gmail.stefvanschiedev.buildinggame.utils.*;
import com.gmail.stefvanschiedev.buildinggame.utils.arena.Arena;
import com.gmail.stefvanschiedev.buildinggame.utils.arena.ArenaMode;
import com.gmail.stefvanschiedev.buildinggame.utils.gameplayer.GamePlayer;
import com.gmail.stefvanschiedev.buildinggame.utils.gameplayer.GamePlayerType;
import com.gmail.stefvanschiedev.buildinggame.utils.guis.ArenaSelection;
import com.gmail.stefvanschiedev.buildinggame.utils.guis.ReportMenu;
import com.gmail.stefvanschiedev.buildinggame.utils.item.ClickEvent;
import com.gmail.stefvanschiedev.buildinggame.utils.item.ItemBuilder;
import com.gmail.stefvanschiedev.buildinggame.utils.item.datatype.PlotDataType;
import com.gmail.stefvanschiedev.buildinggame.utils.plot.Plot;
import com.gmail.stefvanschiedev.buildinggame.utils.potential.PotentialLocation;
import com.gmail.stefvanschiedev.buildinggame.utils.stats.StatType;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

/**
 * This class handles all subcommands for the buildinggame command
 *
 * @since 2.1.0
 */
@CommandAlias("buildinggame|bg")
public class CommandManager extends BaseCommand {

    /**
     * Called whenever a command sender wants to start a booster
     *
     * @param sender the command sender
     * @param multiplier the booster multiplier
     * @param duration the duration of the booster
     * @param player the player for which the booster is intended, when null, the booster is global
     * @since 5.8.0
     */
    @Subcommand("booster")
    @Description("激活一个加成")
    @CommandPermission("bg.booster")
    @CommandCompletion("@nothing @nothing @players")
    public void onBooster(CommandSender sender, float multiplier, int duration, @Optional Player player) {
        new Booster(sender, multiplier, duration, player).start();

        MessageManager.getInstance().send(sender, ChatColor.GREEN + "已激活" +
            (player == null ? "全局加成" : "玩家 " + player.getName() + " 的加成") + "，持续 " + duration + " 秒");
    }

    /**
     * Called whenever a command sender wants to create a new arena
     *
     * @param sender the command sender
     * @param name the arena name
     * @since 5.8.0
     */
    @Subcommand("createarena")
    @Description("创建一个竞技场")
    @CommandPermission("bg.createarena")
    public void onCreateArena(CommandSender sender, @Conditions("arenanotexist") @Single String name) {
        YamlConfiguration arenas = SettingsManager.getInstance().getArenas();
        YamlConfiguration config = SettingsManager.getInstance().getConfig();
        YamlConfiguration messages = SettingsManager.getInstance().getMessages();

        int buildTime = config.getInt("timers.build");
        int lobbyTime = config.getInt("timers.lobby");
        int voteTime = config.getInt("timers.vote");
        int winTime = config.getInt("timers.win");

        arenas.set(name + ".mode", "SOLO");
        arenas.set(name + ".timer", buildTime);
        arenas.set(name + ".lobby-timer", lobbyTime);
        arenas.set(name + ".vote-timer", voteTime);
        arenas.set(name + ".win-timer", winTime);
        arenas.set(name + ".matches", 1);
        SettingsManager.getInstance().save();

        Arena arena = new Arena(name);
        arena.setMode(ArenaMode.SOLO);

        ArenaManager.getInstance().getArenas().add(arena);

        messages.getStringList("commands.createarena.success").forEach(message ->
            MessageManager.getInstance().send(sender, message.replace("%arena%", name)));
    }

    /**
     * Called whenever a command sender wants to delete an arena
     *
     * @param sender the command sender
     * @param arena the arena to delete
     * @since 5.8.0
     */
    @Subcommand("deletearena")
    @Description("删除一个竞技场")
    @CommandPermission("bg.deletearena")
    @CommandCompletion("@arenas")
    public void onDeleteArena(CommandSender sender, Arena arena) {
        YamlConfiguration arenas = SettingsManager.getInstance().getArenas();
        YamlConfiguration messages = SettingsManager.getInstance().getMessages();

        arenas.set(arena.getName(), null);
        SettingsManager.getInstance().save();

        ArenaManager.getInstance().getArenas().remove(arena);

        messages.getStringList("commands.deletearena.success").forEach(message ->
            MessageManager.getInstance().send(sender, message.replace("%arena%", arena.getName())));
    }

    /**
     * Called whenever a command sender wants to delete a spawn
     *
     * @param sender the command sender
     * @param arena the arena
     * @param spawn the spawn point to delete
     * @since 5.8.0
     */
    @Subcommand("deletespawn")
    @Description("删除一个出生点")
    @CommandPermission("bg.deletespawn")
    @CommandCompletion("@arenas @nothing")
    public void onDeleteSpawn(CommandSender sender, Arena arena, int spawn) {
        YamlConfiguration arenas = SettingsManager.getInstance().getArenas();
        YamlConfiguration messages = SettingsManager.getInstance().getMessages();

        var plot = arena.getPlot(spawn);

        if (plot == null) {
            MessageManager.getInstance().send(sender,
                ChatColor.RED + "这不是有效的地块，请先创建一个。");
            return;
        }

        var maxPlayers = arenas.getInt(arena.getName() + ".maxplayers");

        arena.removePlot(plot);

        for (var i = plot.getId(); i < maxPlayers; i++) {
            arenas.set(arena.getName() + '.' + i, arenas.getConfigurationSection(arena.getName() + '.' + i + 1));

            if (i != plot.getId()) {
                arena.getPlot(i).setId(i - 1);
            }
        }

        arenas.set(arena.getName() + '.' + maxPlayers, null);
        arenas.set(arena.getName() + ".maxplayers", maxPlayers - 1);

        SettingsManager.getInstance().save();

        arena.setMaxPlayers(maxPlayers - 1);

        messages.getStringList("commands.deletespawn.success").forEach(message ->
            MessageManager.getInstance().send(sender, message.replace("%place%", plot.getId() + "")));
    }

    /**
     * Called whenever a command sender wants to enable or disable money in a given arena
     *
     * @param sender the command sender
     * @param arena the arena
     * @param enableMoney whether money should be enabled
     * @since 5.8.0
     */
    @Subcommand("enablemoney")
    @Description("启用或禁用竞技场的金钱奖励")
    @CommandPermission("bg.enablemoney")
    @CommandCompletion("@arenas true|false")
    public void onEnableMoney(CommandSender sender, Arena arena, boolean enableMoney) {
        YamlConfiguration arenas = SettingsManager.getInstance().getArenas();

        arenas.set(arena.getName() + ".enable-money", enableMoney);

        arena.setMoneyEnabled(enableMoney);

        SettingsManager.getInstance().save();

        MessageManager.getInstance().send(sender, ChatColor.GREEN + (enableMoney ? "已启用" : "已禁用") +
            "竞技场 " + arena.getName() + " 的金钱奖励");
    }

    /**
     * Called whenever a command sender wants to start an arena
     *
     * @param sender the command sender
     * @param arena the arena, if the sender is a player and the player is in an arena, this may be null
     */
    @Subcommand("forcestart")
    @Description("强制竞技场开始")
    @CommandPermission("bg.forcestart")
    @CommandCompletion("@arenas")
    public void onForceStart(CommandSender sender, @Optional Arena arena) {
        if (sender instanceof Player) {
            var player = (Player) sender;
            var playerArena = ArenaManager.getInstance().getArena(player);

            if (playerArena != null && arena == null) {
                GamePhase phase = playerArena.getCurrentPhase();

                if (!(phase instanceof LobbyGamePhase)) {
                    player.sendMessage(ChatColor.RED + "游戏已经开始");
                } else {
                    ((LobbyGamePhase) phase).endTimer();
                }

                return;
            }
        }

        if (arena == null) {
            MessageManager.getInstance().send(sender, ChatColor.RED + "请指定一个竞技场");
            return;
        }

        if (arena.getPlayers() < 1) {
            MessageManager.getInstance().send(sender,
                ChatColor.RED + "竞技场无法开始，没有玩家！");
            return;
        }

        GamePhase phase = arena.getCurrentPhase();

        if (!(phase instanceof LobbyGamePhase)) {
            sender.sendMessage(ChatColor.RED + "游戏已经开始");
        } else {
            ((LobbyGamePhase) phase).endTimer();
        }
    }

    /**
     * Called whenever a players wants to specify a theme
     *
     * @param player the player
     * @param theme the theme
     * @since 5.8.0
     */
    @Subcommand("forcetheme")
    @Description("强制指定主题")
    @CommandPermission("bg.forcetheme")
    @CommandCompletion("@arenas @nothing")
    public void onForceTheme(Player player, String theme) {
        YamlConfiguration messages = SettingsManager.getInstance().getMessages();

        var arena = ArenaManager.getInstance().getArena(player);

        if (arena == null) {
            MessageManager.getInstance().send(player, ChatColor.RED + "你不在竞技场中");
            return;
        }

        GamePhase phase = arena.getCurrentPhase();

        if (phase instanceof SubjectVoting) {
            ((SubjectVoting) phase).getSubjectMenu().forceTheme(theme);

            messages.getStringList("commands.forcetheme.success").forEach(message ->
                MessageManager.getInstance().send(player, message.replace("%theme%", theme)));
        } else {
            player.sendMessage(ChatColor.RED + "当前没有正在选择主题，无法强制指定");
        }
    }

    /**
     * Called whenever a player wants to join an arena
     *
     * @param sender the executor
     * @param arena the arena to join
     * @param player the player to join. If this is unspecified, the sender will join the game.
     * @since 5.8.0
     */
    @Subcommand("join")
    @Description("加入一个竞技场")
    @CommandPermission("bg.join")
    @CommandCompletion("@arenas @players")
    public void onJoin(Player sender, @Optional Arena arena, @Optional @Flags("other") Player player) {
        YamlConfiguration messages = SettingsManager.getInstance().getMessages();

        if (player == null) {
            player = sender;
        }

        if (arena == null) {
            for (Arena a : ArenaManager.getInstance().getArenas()) {
                if (!a.canJoin()) {
                    continue;
                }

                new ArenaSelection().show(player);
                return;
            }

            MessageManager.getInstance().send(player, messages.getStringList("join.no-arena"));
            return;
        }

        arena.join(player);
    }

    /**
     * Called whenever a player wants to leave the currently joined arena
     *
     * @param player the player who wants to leave
     * @since 5.8.0
     */
    @Subcommand("leave")
    @Description("离开你所在的竞技场")
    @CommandPermission("bg.leave")
    public void onLeave(Player player) {
        var arena = ArenaManager.getInstance().getArena(player);

        if (arena == null) {
            MessageManager.getInstance().send(player, ChatColor.RED + "你不在竞技场中");
            return;
        }

        arena.leave(player);
    }

    /**
     * Called whenever a command sender wants to view a list of all arenas
     *
     * @param sender the command sender who wants to view all arenas
     * @since 5.8.0
     */
    @Subcommand("list")
    @Description("列出所有竞技场")
    @CommandPermission("bg.list")
    public void onList(CommandSender sender) {
        ArenaManager.getInstance().getArenas().forEach(arena ->
            MessageManager.getInstance().send(sender, ChatColor.DARK_AQUA + " - " + arena.getName() +
                ChatColor.DARK_GREEN + " - " + arena.getCurrentPhase().getName()));
    }

    /**
     * Called whenever a command sender wants to reload the plugin
     *
     * @param sender the command sender
     * @since 5.8.0
     */
    @Subcommand("reload")
    @Description("重载插件")
    @CommandPermission("bg.reload")
    public void onReload(CommandSender sender) {
        ArenaManager.getInstance().getArenas().stream().filter(arena -> arena.getPlayers() > 0).forEach(Arena::stop);

        StatManager instance = StatManager.getInstance();

        if (instance.getMySQLDatabase() == null)
            instance.saveToFile();
        else
            instance.saveToDatabase();

        FileCheckerTimer runnable = SettingsManager.getInstance().getRunnable();

        if (runnable != null && !runnable.isCancelled()) {
            runnable.cancel();
        }

        Main.getInstance().loadPlugin(true);

        MessageManager.getInstance().send(sender, ChatColor.GREEN + "插件已重载！");
    }

    /**
     * Called when a player wants to view the reportsd
     *
     * @param player the player executing this command
     * @since 6.5.0
     */
    @Subcommand("reports")
    @Description("查看所有举报")
    @CommandPermission("bg.reports")
    public void onReports(Player player) {
        if (!Bukkit.getPluginManager().isPluginEnabled("WorldEdit")) {
            player.sendMessage(ChatColor.RED + "需要启用 WorldEdit 才能使用此功能。");
            return;
        }

        new ReportMenu().show(player);
    }

    /**
     * Called whenever a player wants to set the boundary of a plot
     *
     * @param player the player
     * @param arena the arena
     * @param id the plot id to set the boundary of
     * @since 5.8.0
     */
    @Subcommand("setbounds|setboundary")
    @Description("设置地块边界（含边界）")
    @CommandPermission("bg.setbounds")
    @CommandCompletion("@arenas @nothing")
    public void onSetBounds(Player player, Arena arena, int id) {
        final var plot = arena.getPlot(id);

        if (plot == null) {
            MessageManager.getInstance().send(player, ChatColor.RED + "这不是有效的地块");
            return;
        }

        player.getInventory().setItemInMainHand(new ItemBuilder(player, Material.STICK)
            .setDisplayName(ChatColor.LIGHT_PURPLE + "魔杖")
            .addContext("plot", PlotDataType.getInstance(), plot)
            .setClickEvent(ClickEvent.BOUNDS_CLICK)
            .build()
        );
    }

    /**
     * Called whenever a player wants to set the floor of a plot
     *
     * @param player the player
     * @param arena the arena
     * @param id the plot id to set the floor of
     * @since 5.8.0
     */
    @Subcommand("setfloor")
    @Description("设置地块地板（含边界）")
    @CommandPermission("bg.setfloor")
    @CommandCompletion("@arenas @nothing")
    public void onSetFloor(Player player, Arena arena, int id) {
        final var plot = arena.getPlot(id);

        if (plot == null) {
            MessageManager.getInstance().send(player, ChatColor.RED + "这不是有效的地块");
            return;
        }

        player.getInventory().setItemInMainHand(new ItemBuilder(player, Material.STICK)
            .setDisplayName(ChatColor.LIGHT_PURPLE + "魔杖")
            .addContext("plot", PlotDataType.getInstance(), plot)
            .setClickEvent(ClickEvent.FLOOR_CLICK)
            .build()
        );
    }

    /**
     * Called whenever a command sender wants to change the game mode of an arena
     *
     * @param sender the command sender
     * @param arena the arena
     * @param arenaMode the new arena mode
     * @since 5.8.0
     */
    @Subcommand("setgamemode")
    @Description("设置竞技场的游戏模式")
    @CommandPermission("bg.setgamemode")
    @CommandCompletion("@arenas @arenamodes")
    public void onSetGameMode(CommandSender sender, Arena arena, ArenaMode arenaMode) {
        YamlConfiguration arenas = SettingsManager.getInstance().getArenas();

        arenas.set(arena.getName() + ".mode", arenaMode.toString());
        SettingsManager.getInstance().save();

        arena.setMode(arenaMode);

        MessageManager.getInstance().send(sender, ChatColor.GREEN +
            "已将竞技场 " + arena.getName() + " 的游戏模式更改为 " +
            arenaMode.toString().toLowerCase(Locale.getDefault()));
    }

    /**
     * Called whenever a player wants to set the lobby of an arena
     *
     * @param player the player
     * @param arena the arena which lobby will be changed
     * @since 5.8.0
     */
    @Subcommand("setlobby")
    @Description("设置大厅")
    @CommandPermission("bg.setlobby")
    @CommandCompletion("@arenas")
    public void onSetLobby(Player player, Arena arena) {
        YamlConfiguration arenas = SettingsManager.getInstance().getArenas();
        YamlConfiguration messages = SettingsManager.getInstance().getMessages();

        Location location = player.getLocation();
        String worldName = location.getWorld().getName();
        int blockX = location.getBlockX();
        int blockY = location.getBlockY();
        int blockZ = location.getBlockZ();
        float pitch = location.getPitch();
        float yaw = location.getYaw();

        arenas.set(arena.getName() + ".lobby.server", player.getServer().getName());
        arenas.set(arena.getName() + ".lobby.world", worldName);
        arenas.set(arena.getName() + ".lobby.x", blockX);
        arenas.set(arena.getName() + ".lobby.y", blockY);
        arenas.set(arena.getName() + ".lobby.z", blockZ);
        arenas.set(arena.getName() + ".lobby.pitch", pitch);
        arenas.set(arena.getName() + ".lobby.yaw", yaw);
        SettingsManager.getInstance().save();

        arena.setLobby(new PotentialLocation(() -> Bukkit.getWorld(worldName), blockX, blockY, blockZ, yaw, pitch));

        MessageManager.getInstance().send(player, messages.getStringList("commands.setlobby.success"));
    }

    /**
     * Called whenever a command sender wants to set the lobby timer
     *
     * @param sender the command sender
     * @param arena the arena
     * @param seconds the amount of seconds the lobby timer should last
     * @since 5.8.0
     */
    @Subcommand("setlobbytimer")
    @Description("更改大厅计时器")
    @CommandPermission("bg.setlobbytimer")
    @CommandCompletion("@arenas @nothing")
    public void onSetLobbyTimer(CommandSender sender, Arena arena, int seconds) {
        YamlConfiguration arenas = SettingsManager.getInstance().getArenas();

        arenas.set(arena.getName() + ".lobby-timer", seconds);
        SettingsManager.getInstance().save();

        MessageManager.getInstance().send(sender, ChatColor.GREEN + "竞技场 '" + arena.getName() +
            "' 的大厅计时器已设为 " + seconds + " 秒");
    }

    /**
     * Called whenever a player wants to set the main spawn
     *
     * @param player the player
     * @since 5.8.0
     */
    @Subcommand("setmainspawn")
    @Description("设置主出生点")
    @CommandPermission("bg.setmainspawn")
    public void onSetMainSpawn(Player player) {
        YamlConfiguration arenas = SettingsManager.getInstance().getArenas();
        YamlConfiguration config = SettingsManager.getInstance().getConfig();
        YamlConfiguration messages = SettingsManager.getInstance().getMessages();

        List<String> worlds = config.getStringList("scoreboards.main.worlds.enable");

        if (arenas.contains("main-spawn.world"))
            worlds.remove(arenas.getString("main-spawn.world"));

        Location location = player.getLocation();

        worlds.add(location.getWorld().getName());
        config.set("scoreboards.main.worlds.enable", worlds);

        arenas.set("main-spawn.server", player.getServer().getName());
        arenas.set("main-spawn.world", location.getWorld().getName());
        arenas.set("main-spawn.x", location.getBlockX());
        arenas.set("main-spawn.y", location.getBlockY());
        arenas.set("main-spawn.z", location.getBlockZ());
        arenas.set("main-spawn.pitch", location.getPitch());
        arenas.set("main-spawn.yaw", location.getYaw());
        SettingsManager.getInstance().save();

        MainSpawnManager.getInstance().setMainSpawn(new PotentialLocation(location));
        MessageManager.getInstance().send(player, messages.getStringList("commands.setmainspawn.success"));
    }

    /**
     * Called whenever a command sender wants to set the amount of matches
     *
     * @param sender the command sender
     * @param arena the arena
     * @param matches the new amount of matches
     * @since 5.8.0
     */
    @Subcommand("setmatches")
    @Description("设置比赛场数")
    @CommandPermission("bg.setmatches")
    @CommandCompletion("@arenas @nothing")
    public void onSetMatches(CommandSender sender, Arena arena, int matches) {
        YamlConfiguration arenas = SettingsManager.getInstance().getArenas();

        if (!arena.isEmpty()) {
            MessageManager.getInstance().send(sender, ChatColor.RED +
                "竞技场中还有玩家，此时更改比赛场数可能引发问题。请等竞技场空置后再修改。");
            return;
        }

        arenas.set(arena.getName() + ".matches", matches);
        SettingsManager.getInstance().save();

        arena.setMaxMatches(matches);

        MessageManager.getInstance().send(sender, ChatColor.GREEN + "比赛场数已更改！");
    }

    /**
     * Called whenever a command sender wants to set the maximum amount of players
     *
     * @param sender the command sender
     * @param arena the arena
     * @param maxPlayers the maximum amount of players
     * @since 5.8.0
     */
    @Subcommand("setmaxplayers")
    @Description("设置竞技场的最大玩家数")
    @CommandPermission("bg.setmaxplayers")
    @CommandCompletion("@arenas @nothing")
    public void onSetMaxPlayers(CommandSender sender, Arena arena, int maxPlayers) {
        YamlConfiguration arenas = SettingsManager.getInstance().getArenas();
        YamlConfiguration config = SettingsManager.getInstance().getConfig();

        if (arena.getMode() == ArenaMode.SOLO) {
            MessageManager.getInstance().send(sender,
                ChatColor.RED + "只能修改团队模式竞技场的最大玩家数");
            return;
        }

        if (arena.getPlots().isEmpty()) {
            MessageManager.getInstance().send(sender,
                ChatColor.RED + "你需要先创建地块才能设置最大玩家数");
            return;
        }

        if (maxPlayers % arena.getPlots().size() != 0) {
            MessageManager.getInstance().send(sender, ChatColor.RED +
                "你的最大玩家数必须是 " + arena.getPlots().size() + " 的整数倍");
            return;
        }

        arenas.set(arena.getName() + ".maxplayers", maxPlayers);

        //add parts to config
        for (int i = 0; i < maxPlayers; i++) {
            if (config.contains("team-selection.team." + i))
                continue;

            config.set("team-selection.team." + i + ".id", "paper");
        }

        SettingsManager.getInstance().save();

        arena.setMaxPlayers(maxPlayers);

        MessageManager.getInstance().send(sender, ChatColor.GREEN + "最大玩家数已更改！");
    }

    /**
     * Called whenever a command sender wants to set the minimum amount of players
     *
     * @param sender the command sender
     * @param arena the arena
     * @param minPlayers the minimum amount of players
     * @since 5.8.0
     */
    @Subcommand("setminplayers")
    @Description("设置最小玩家数")
    @CommandPermission("bg.setminplayers")
    @CommandCompletion("@arenas @nothing")
    public void onSetMinPlayers(CommandSender sender, Arena arena, int minPlayers) {
        YamlConfiguration arenas = SettingsManager.getInstance().getArenas();
        YamlConfiguration messages = SettingsManager.getInstance().getMessages();

        arenas.set(arena.getName() + ".minplayers", minPlayers);
        SettingsManager.getInstance().save();

        arena.setMinPlayers(minPlayers);

        MessageManager.getInstance().send(sender, messages.getStringList("commands.setminplayers.success"));
    }

    /**
     * Called whenever a player wants to set a new spawn
     *
     * @param player the player
     * @param arena the arena
     * @since 5.8.0
     */
    @Subcommand("setspawn")
    @Description("设置新的出生点")
    @CommandPermission("bg.setspawn")
    @CommandCompletion("@arenas")
    public void onSetSpawn(Player player, Arena arena) {
        YamlConfiguration arenas = SettingsManager.getInstance().getArenas();
        YamlConfiguration messages = SettingsManager.getInstance().getMessages();

        int place = arena.getMaxPlayers() + 1;
        String name = arena.getName();

        Location location = player.getLocation();
        String worldName = location.getWorld().getName();
        int blockX = location.getBlockX();
        int blockY = location.getBlockY();
        int blockZ = location.getBlockZ();
        float pitch = location.getPitch();
        float yaw = location.getYaw();

        arenas.set(name + '.' + place + ".server", player.getServer().getName());
        arenas.set(name + '.' + place + ".world", worldName);
        arenas.set(name + '.' + place + ".x", blockX);
        arenas.set(name + '.' + place + ".y", blockY);
        arenas.set(name + '.' + place + ".z", blockZ);
        arenas.set(name + '.' + place + ".pitch", pitch);
        arenas.set(name + '.' + place + ".yaw", yaw);
        arenas.set(name + ".maxplayers", place);
        SettingsManager.getInstance().save();

        Plot plot = new Plot(arena, place);
        plot.setLocation(new PotentialLocation(() -> Bukkit.getWorld(worldName), blockX, blockY, blockZ, yaw, pitch));

        arena.addPlot(plot);
        arena.setMaxPlayers(place);

        messages.getStringList("commands.setspawn.success").forEach(message ->
            MessageManager.getInstance().send(player, message.replace("%place%", place + "")));
    }

    /**
     * Called whenever a command sender wants to change the timer
     *
     * @param sender the command sender
     * @param arena the arena
     * @param seconds the new amount of seconds
     * @since 5.8.0
     */
    @Subcommand("settimer")
    @Description("更改计时器")
    @CommandPermission("bg.settimer")
    @CommandCompletion("@arenas @nothing")
    public void onSetTimer(CommandSender sender, Arena arena, int seconds) {
        YamlConfiguration arenas = SettingsManager.getInstance().getArenas();

        arenas.set(arena.getName() + ".timer", seconds);
        SettingsManager.getInstance().save();

        MessageManager.getInstance().send(sender, ChatColor.GREEN + "竞技场 '" + arena.getName() +
            "' 的计时器已设为 " + seconds + " 秒");
    }

    /**
     * Called whenever a command sender wants to change a setting
     *
     * @param sender the command sender
     * @param path the path in the config file
     * @param args the arguments
     * @since 5.8.0
     */
    @Subcommand("setting")
    @Description("更改某项设置")
    @CommandPermission("bg.setting")
    public void onSetting(CommandSender sender, String path, @Split(" ") String[] args) {
        YamlConfiguration config = SettingsManager.getInstance().getConfig();

        if (!config.contains(path)) {
            MessageManager.getInstance().send(sender, ChatColor.RED + "该设置不存在");
            return;
        }

        if (config.isList(path)) {
            if (args.length < 2) {
                MessageManager.getInstance().send(sender, ChatColor.RED +
                    "请指定要添加还是移除该项，并给出对应的值");
                return;
            }

            if (args[0].equalsIgnoreCase("add")) {
                //add the value
                List<String> list = config.getStringList(path);
                list.add(args[1]);
                config.set(path, list);

                MessageManager.getInstance().send(sender, ChatColor.GREEN + "已向配置添加值");
            } else if (args[0].equalsIgnoreCase("remove")) {
                //remove the value
                List<String> list = config.getStringList(path);
                list.remove(args[1]);
                config.set(path, list);

                MessageManager.getInstance().send(sender, ChatColor.GREEN + "已从配置移除值");
            }

            SettingsManager.getInstance().save();
            return;
        }

        //whole bunch of checking
        if (config.isBoolean(path))
            config.set(path, Boolean.parseBoolean(args[0]));
        else if (config.isDouble(path)) {
            try {
                config.set(path, Double.parseDouble(args[0]));
            } catch (NumberFormatException e) {
                MessageManager.getInstance().send(sender, ChatColor.RED +
                    "值的类型与配置中不一致，应为小数");
                return;
            }
        } else if (config.isInt(path)) {
            try {
                config.set(path, Integer.parseInt(args[0]));
            } catch (NumberFormatException e) {
                MessageManager.getInstance().send(sender, ChatColor.RED +
                    "值的类型与配置中不一致，应为整数");
                return;
            }
        } else if (config.isLong(path)) {
            try {
                config.set(path, Long.parseLong(args[0]));
            } catch (NumberFormatException e) {
                MessageManager.getInstance().send(sender, ChatColor.RED +
                    "值的类型与配置中不一致，应为长整数");
                return;
            }
        } else if (config.isString(path))
            config.set(path, args[0]);
        else {
            MessageManager.getInstance().send(sender, ChatColor.YELLOW +
                "无法通过命令修改该设置，请手动修改");
            return;
        }

        MessageManager.getInstance().send(sender, ChatColor.GREEN + "已修改配置中的值");
        SettingsManager.getInstance().save();
    }

    /**
     * Called whenever a command sender wants to change the vote timer
     *
     * @param sender the command sender
     * @param arena the arena
     * @param seconds the amount of seconds
     * @since 5.8.0
     */
    @Subcommand("setvotetimer")
    @Description("更改投票计时器")
    @CommandPermission("bg.setvotetimer")
    @CommandCompletion("@arenas @nothing")
    public void onSetVoteTimer(CommandSender sender, Arena arena, int seconds) {
        YamlConfiguration arenas = SettingsManager.getInstance().getArenas();

        arenas.set(arena.getName() + ".vote-timer", seconds);
        SettingsManager.getInstance().save();

        MessageManager.getInstance().send(sender, ChatColor.GREEN + "竞技场 '" + arena.getName() +
            "' 的投票计时器已设为 " + seconds + " 秒");
    }

    /**
     * Called whenever a command sender wants to change the win timer
     *
     * @param sender the command sender
     * @param arena the arena
     * @param seconds the amount of seconds
     * @since 5.8.0
     */
    @Subcommand("setwintimer")
    @Description("更改结算计时器")
    @CommandPermission("bg.setwintimer")
    @CommandCompletion("@arenas @nothing")
    public void onSetWinTimer(CommandSender sender, Arena arena, int seconds) {
        YamlConfiguration arenas = SettingsManager.getInstance().getArenas();

        arenas.set(arena.getName() + ".win-timer", seconds);
        SettingsManager.getInstance().save();

        MessageManager.getInstance().send(sender, ChatColor.GREEN + "竞技场 '" + arena.getName() +
            "' 的结算计时器已设为 " + seconds + " 秒");
    }

    /**
     * Called whenever a player wants to spectate
     *
     * @param player the player
     * @param toSpectate the player to spectate
     * @since 5.8.0
     */
    @Subcommand("spectate")
    @Description("旁观一名玩家")
    @CommandPermission("bg.spectate")
    public void onSpectate(Player player, @Flags("other") Player toSpectate) {
        var arena = ArenaManager.getInstance().getArena(toSpectate);

        if (arena == null) {
            MessageManager.getInstance().send(player, ChatColor.RED + "未找到竞技场");
            return;
        }

        if (!(arena.getCurrentPhase() instanceof BuildingGamePhase)) {
            MessageManager.getInstance().send(player, ChatColor.RED + "你现在不能旁观");
            return;
        }

        GamePlayer toSpectateGamePlayer = arena.getUsedPlots().stream()
            .flatMap(plot -> plot.getAllGamePlayers().stream())
            .filter(gamePlayer -> gamePlayer.getPlayer().equals(toSpectate))
            .findAny()
            .orElse(null);

        if (toSpectateGamePlayer == null) {
            MessageManager.getInstance().send(player, ChatColor.RED + "未找到可旁观的玩家");
            return;
        }

        if (toSpectateGamePlayer.getGamePlayerType() == GamePlayerType.SPECTATOR) {
            MessageManager.getInstance().send(player, ChatColor.RED + "你不能旁观旁观者");
            return;
        }

        //check if the player is playing the game
        if (ArenaManager.getInstance().getArena(player) != null &&
            ArenaManager.getInstance().getArena(player).getPlot(player).getGamePlayer(player)
                .getGamePlayerType() == GamePlayerType.PLAYER) {
            MessageManager.getInstance().send(player,
                ChatColor.RED + "游戏进行中不能旁观");
            return;
        }

        //check if we are already spectating
        Plot spectating = null;
        for (Arena a : ArenaManager.getInstance().getArenas()) {
            for (Plot plot : a.getUsedPlots()) {
                for (GamePlayer gamePlayer : plot.getSpectators()) {
                    if (gamePlayer.getPlayer().equals(player)) {
                        spectating = plot;
                        break;
                    }
                }
            }
        }

        if (spectating != null)
            spectating.removeSpectator(spectating.getGamePlayer(player));

        arena.getPlot(toSpectateGamePlayer.getPlayer()).addSpectator(player, toSpectateGamePlayer);

        MessageManager.getInstance().send(player, ChatColor.GREEN + "正在旁观 " +
            toSpectateGamePlayer.getPlayer().getName() + "！");
    }

    /**
     * Called whenever a player wants to view his/her statistics
     *
     * @param player the player
     * @since 5.8.0
     */
    @Subcommand("stats")
    @Description("查看你的统计数据")
    @CommandPermission("bg.stats")
    public void onStats(Player player) {
        YamlConfiguration messages = SettingsManager.getInstance().getMessages();

        StatManager statManager = StatManager.getInstance();

        var playsStat = statManager.getStat(player, StatType.PLAYS);
        var firstStat = statManager.getStat(player, StatType.FIRST);
        var secondStat = statManager.getStat(player, StatType.SECOND);
        var thirdStat = statManager.getStat(player, StatType.THIRD);
        var placedStat = statManager.getStat(player, StatType.PLACED);
        var brokenStat = statManager.getStat(player, StatType.BROKEN);
        var walkedStat = statManager.getStat(player, StatType.WALKED);
        var pointsGivenStat = statManager.getStat(player, StatType.POINTS_GIVEN);
        var pointsReceivedStat = statManager.getStat(player, StatType.POINTS_RECEIVED);

        MessageManager.translate(messages.getStringList("commands.stats.success")).forEach(message ->
            MessageManager.getInstance().send(player, message
                .replace("%stat_plays%", playsStat == null ? "0" : String.valueOf(playsStat.getValue()))
                .replace("%stat_first%", firstStat == null ? "0" : String.valueOf(firstStat.getValue()))
                .replace("%stat_second%", secondStat == null ? "0" : String.valueOf(secondStat.getValue()))
                .replace("%stat_third%", thirdStat == null ? "0" : String.valueOf(thirdStat.getValue()))
                .replace("%stat_placed%", placedStat == null ? "0" : String.valueOf(placedStat.getValue()))
                .replace("%stat_broken%", brokenStat == null ? "0" : String.valueOf(brokenStat.getValue()))
                .replace("%stat_walked%", walkedStat == null ? "0" : String.valueOf(walkedStat.getValue()))
                .replace("%stat_points_given%", pointsGivenStat == null ? "0" :
                    String.valueOf(pointsGivenStat.getValue()))
                .replace("%stat_points_received%", pointsReceivedStat == null ? "0" :
                    String.valueOf(pointsReceivedStat.getValue()))));
    }

    /**
     * Called whenever a player wants to vote on a plot
     *
     * @param player the player
     * @param points the amount of points to award
     * @since 5.8.0
     */
    @Subcommand("vote")
    @Description("为某人的地块投票")
    @CommandPermission("bg.vote")
    @CommandCompletion("@range:1-10")
    public void onVote(Player player, int points) {
        YamlConfiguration messages = SettingsManager.getInstance().getMessages();

        if (points < 1 || points > 10) {
            MessageManager.getInstance().send(player, ChatColor.RED + "分数只能在 1 到 10 之间");
            return;
        }

        var arena = ArenaManager.getInstance().getArena(player);

        if (arena == null) {
            MessageManager.getInstance().send(player, ChatColor.RED + "你不在竞技场中");
            return;
        }

        if (arena.getPlot(player).getGamePlayer(player).getGamePlayerType() == GamePlayerType.SPECTATOR) {
            MessageManager.getInstance().send(player, ChatColor.RED + "旁观者不能投票");
            return;
        }

        GamePhase phase = arena.getCurrentPhase();

        if (!(phase instanceof VotingGamePhase)) {
            MessageManager.getInstance().send(player, ChatColor.RED + "你现在不能投票");
            return;
        }

        var plot = ((VotingGamePhase) phase).getVotingPlot();
        plot.addVote(new Vote(points, player));

        MessageManager.getInstance().send(player, messages.getString("vote.message")
            .replace("%playerplot%", plot.getPlayerFormat())
            .replace("%points%", points + ""));
    }

    /**
     * Shows an automatically generated help display to the sender
     *
     * @param sender the sender which should see the help menu
     * @param help the command supplied for the help system
     * @since 5.9.0
     */
    @HelpCommand
    public void doHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }
}