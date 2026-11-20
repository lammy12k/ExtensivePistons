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
import org.incendo.cloud.parser.standard.DoubleParser;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class ExtensivePistons extends JavaPlugin {

    private static ExtensivePistons instance;

    private static boolean pushableTileEntities = true;
    private static int pushLimit = 64;
    private static int maxTileEntitiesPerPush = 16;
    private static boolean disableQc = false;
    private static boolean spitBlock = true;
    private static float pistonSpeed = 1.0f;
    private static boolean respectClaimBorders = true;
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
                    sender.sendMessage("§7- spit-block: §e" + spitBlock);
                    sender.sendMessage("§7- piston-speed: §e" + pistonSpeed);
                    sender.sendMessage("§7- respect-claim-borders: §e" + respectClaimBorders);
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

        manager.command(base.literal("set")
                .literal("piston-speed")
                .required("value", DoubleParser.doubleParser(0.05, 2.0))
                .handler(ctx -> {
                    double val = ctx.get("value");
                    getConfig().set("piston-speed", val);
                    saveConfig();
                    loadSettings();
                    ctx.sender().source().sendMessage("§aSet piston-speed to §e" + pistonSpeed);
                }));

        List<String> toggleableKeys = List.of("pushable-tile-entities", "disable-qc", "spit-block", "respect-claim-borders", "whitelist-on", "blacklist-on");
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

        registerListCommands(manager, base, "whitelist");
        registerListCommands(manager, base, "blacklist");
    }

    private void registerListCommands(PaperCommandManager<Source> manager, Command.Builder<Source> base, String listKey) {
        SuggestionProvider<Source> allBlocksProvider = (ctx, input) -> {
            String remaining = input.remainingInput().toLowerCase();
            List<Suggestion> suggestions = BuiltInRegistries.BLOCK.keySet().stream()
                    .map(Identifier::toString)
                    .filter(id -> id.startsWith(remaining) || id.substring(id.indexOf(':') + 1).startsWith(remaining))
                    .limit(50)
                    .map(Suggestion::suggestion)
                    .toList();
            return CompletableFuture.completedFuture(suggestions);
        };

        SuggestionProvider<Source> configuredBlocksProvider = (ctx, input) -> {
            String remaining = input.remainingInput().toLowerCase();
            List<Suggestion> suggestions = getConfig().getStringList(listKey).stream()
                    .filter(id -> id.toLowerCase().startsWith(remaining))
                    .map(Suggestion::suggestion)
                    .toList();
            return CompletableFuture.completedFuture(suggestions);
        };

        manager.command(base.literal(listKey)
                .literal("info")
                .handler(ctx -> {
                    CommandSender sender = ctx.sender().source();
                    boolean enabled = getConfig().getBoolean(listKey + "-on");
                    List<String> entries = getConfig().getStringList(listKey);
                    sender.sendMessage("§6ExtensivePistons " + listKey + " §7(enabled: §e" + enabled + "§7, count: §e" + entries.size() + "§7):");
                    if (entries.isEmpty()) {
                        sender.sendMessage("§7  (empty)");
                    } else {
                        for (String entry : entries) {
                            sender.sendMessage("§7  - §e" + entry);
                        }
                    }
                }));

        manager.command(base.literal(listKey)
                .literal("add")
                .required("block", StringParser.greedyStringParser(), allBlocksProvider)
                .handler(ctx -> {
                    CommandSender sender = ctx.sender().source();
                    String raw = ctx.<String>get("block").trim().toLowerCase();
                    Identifier parsed = Identifier.tryParse(raw);
                    if (parsed == null || !BuiltInRegistries.BLOCK.containsKey(parsed)) {
                        sender.sendMessage("§cUnknown block ID: §e" + raw);
                        return;
                    }

                    String normalized = parsed.toString();
                    List<String> current = new ArrayList<>(getConfig().getStringList(listKey));
                    if (current.stream().anyMatch(s -> s.equalsIgnoreCase(normalized))) {
                        sender.sendMessage("§e" + normalized + " §7is already in the " + listKey + ".");
                        return;
                    }

                    current.add(normalized);
                    getConfig().set(listKey, current);
                    saveConfig();
                    loadSettings();
                    sender.sendMessage("§aAdded §e" + normalized + " §ato the " + listKey + ".");
                }));

        manager.command(base.literal(listKey)
                .literal("remove")
                .required("block", StringParser.greedyStringParser(), configuredBlocksProvider)
                .handler(ctx -> {
                    CommandSender sender = ctx.sender().source();
                    String raw = ctx.<String>get("block").trim().toLowerCase();
                    Identifier parsed = Identifier.tryParse(raw);
                    String normalized = parsed != null ? parsed.toString() : raw;

                    List<String> current = new ArrayList<>(getConfig().getStringList(listKey));
                    if (!current.removeIf(s -> s.equalsIgnoreCase(normalized))) {
                        sender.sendMessage("§e" + normalized + " §7was not found in the " + listKey + ".");
                        return;
                    }
                    getConfig().set(listKey, current);
                    saveConfig();
                    loadSettings();
                    sender.sendMessage("§aRemoved §e" + normalized + " §afrom the " + listKey + ".");
                }));
    }

    public void loadSettings() {
        reloadConfig();
        pushableTileEntities = getConfig().getBoolean("pushable-tile-entities", true);
        pushLimit = Math.max(1, getConfig().getInt("push-limit", 64));
        maxTileEntitiesPerPush = Math.max(0, getConfig().getInt("max-tile-entities-per-push", 16));
        disableQc = getConfig().getBoolean("disable-qc", false);
        spitBlock = getConfig().getBoolean("spit-block", true);
        pistonSpeed = (float) Math.clamp(getConfig().getDouble("piston-speed", 1.0), 0.05, 2.0);
        respectClaimBorders = getConfig().getBoolean("respect-claim-borders", true);
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
    public static boolean isSpitBlock() {
        return spitBlock;
    }
    public static float getPistonSpeed() {
        return pistonSpeed;
    }
    public static boolean isRespectClaimBorders() {
        return respectClaimBorders;
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