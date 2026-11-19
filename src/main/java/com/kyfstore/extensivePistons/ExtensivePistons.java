package com.kyfstore.extensivePistons;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.Command;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ExtensivePistons extends JavaPlugin {

    private static ExtensivePistons instance;

    private static boolean pushableTileEntities = true;
    private static int pushLimit = 64;
    private static int maxTileEntitiesPerPush = 16;
    private static boolean disableQc = false;
    private static boolean whitelistOn = false;
    private static boolean blacklistOn = true;
    private static Set<Identifier> whitelist = Set.of();
    private static Set<Identifier> blacklist = Set.of();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        loadSettings();
        registerCommands();
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    private void registerCommands() {
        PaperCommandManager<Source> manager = PaperCommandManager.builder(PaperSimpleSenderMapper.simpleSenderMapper())
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(this);

        Command.Builder<Source> base = manager.commandBuilder("extensivepistons", "ep")
                .permission("extensivepistons.admin");

        manager.command(base.literal("reload")
                .handler(ctx -> {
                    loadSettings();
                    ctx.sender().source().sendMessage("§aSuccessfully reloaded ExtensivePistons!");
                }));

        manager.command(base.literal("info")
                .handler(ctx -> {
                    CommandSender sender = ctx.sender().source();
                    sender.sendMessage("§6ExtensivePistons Settings:");
                    sender.sendMessage("§7- push-limit: §e" + pushLimit);
                    sender.sendMessage("§7- pushable-tile-entities: §e" + pushableTileEntities);
                    sender.sendMessage("§7- max-tile-entities-per-push: §e" + maxTileEntitiesPerPush);
                    sender.sendMessage("§7- disable-qc: §e" + disableQc);
                    sender.sendMessage("§7- whitelist-on: §e" + whitelistOn + " §7(" + whitelist.size() + " blocks)");
                    sender.sendMessage("§7- blacklist-on: §e" + blacklistOn + " §7(" + blacklist.size() + " blocks)");
                }));

        manager.command(base.literal("set")
                .literal("push-limit")
                .required("value", IntegerParser.integerParser(1))
                .handler(ctx -> {
                    int val = ctx.get("value");
                    getConfig().set("push-limit", val);
                    saveConfig();
                    loadSettings();
                    ctx.sender().source().sendMessage("§aSet push-limit to §e" + pushLimit);
                }));

        manager.command(base.literal("set")
                .literal("max-tile-entities-per-push")
                .required("value", IntegerParser.integerParser(0))
                .handler(ctx -> {
                    int val = ctx.get("value");
                    getConfig().set("max-tile-entities-per-push", val);
                    saveConfig();
                    loadSettings();
                    ctx.sender().source().sendMessage("§aSet max-tile-entities-per-push to §e" + maxTileEntitiesPerPush);
                }));

        List<String> toggleableKeys = List.of("pushable-tile-entities", "disable-qc", "whitelist-on", "blacklist-on");
        manager.command(base.literal("toggle")
                .required("setting", StringParser.stringParser(), SuggestionProvider.suggestingStrings(toggleableKeys))
                .handler(ctx -> {
                    String key = ctx.<String>get("setting").toLowerCase();
                    if (!toggleableKeys.contains(key)) {
                        ctx.sender().source().sendMessage("§cUnknown setting: " + key);
                        return;
                    }
                    boolean updated = !getConfig().getBoolean(key);
                    getConfig().set(key, updated);
                    saveConfig();
                    loadSettings();
                    ctx.sender().source().sendMessage("§aSet " + key + " to §e" + updated);
                }));
    }

    public void loadSettings() {
        reloadConfig();
        pushableTileEntities = getConfig().getBoolean("pushable-tile-entities", true);
        pushLimit = Math.max(1, getConfig().getInt("push-limit", 64));
        maxTileEntitiesPerPush = Math.max(0, getConfig().getInt("max-tile-entities-per-push", 16));
        disableQc = getConfig().getBoolean("disable-qc", false);
        whitelistOn = getConfig().getBoolean("whitelist-on", false);
        blacklistOn = getConfig().getBoolean("blacklist-on", true);

        whitelist = parseBlockIds(getConfig().getStringList("whitelist"));
        blacklist = parseBlockIds(getConfig().getStringList("blacklist"));
    }

    private Set<Identifier> parseBlockIds(List<String> rawList) {
        Set<Identifier> ids = new HashSet<>();
        for (String entry : rawList) {
            if (entry != null && !entry.isBlank()) {
                Identifier id = Identifier.tryParse(entry.trim().toLowerCase());
                if (id != null) {
                    ids.add(id);
                }
            }
        }
        return Set.copyOf(ids);
    }

    public static ExtensivePistons getInstance() {
        return instance;
    }

    public static boolean isPushableTileEntities() {
        return pushableTileEntities;
    }

    public static int getPushLimit() {
        return pushLimit;
    }

    public static int getMaxTileEntitiesPerPush() {
        return maxTileEntitiesPerPush;
    }

    public static boolean isDisableQc() {
        return disableQc;
    }

    public static boolean isWhitelisted(Block block) {
        if (!whitelistOn) {
            return false;
        }
        Identifier key = BuiltInRegistries.BLOCK.getKey(block);
        return whitelist.contains(key);
    }

    public static boolean isBlacklisted(Block block) {
        if (!blacklistOn) {
            return false;
        }
        Identifier key = BuiltInRegistries.BLOCK.getKey(block);
        return blacklist.contains(key);
    }
}
