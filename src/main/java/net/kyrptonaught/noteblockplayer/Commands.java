package net.kyrptonaught.noteblockplayer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.kyrptonaught.noteblockplayer.players.DynamicPositionedPlayer;
import net.kyrptonaught.noteblockplayer.players.GlobalPlayer;
import net.kyrptonaught.noteblockplayer.players.StaticPositionedPlayer;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.raphimc.noteblocklib.NoteBlockLib;
import net.raphimc.noteblocklib.format.nbs.NbsSong;
import net.raphimc.noteblocklib.player.SongPlayer;
import net.raphimc.noteblocklib.player.SongPlayerCallback;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class Commands {

    private static final HashMap<String, SongPlayer> songPlayers = new HashMap<>();

    public static final HashMap<String, NbsSong> songCache = new HashMap<>();

    public static void
    onInitialize() {
        createDir(FabricLoader.getInstance().getGameDir().resolve("nbs"));
    }

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("playnbs")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("song", StringArgumentType.string())
                        .suggests(Commands::getAvailableNBSFiles)
                        .then(CommandManager.argument("looping", BoolArgumentType.bool())
                                .then(CommandManager.argument("listeners", EntityArgumentType.players())
                                        .then(CommandManager.literal("GLOBAL")
                                                .executes(context -> {
                                                    String songFile = StringArgumentType.getString(context, "song");
                                                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "listeners");
                                                    boolean looping = BoolArgumentType.getBool(context, "looping");

                                                    try {
                                                        NbsSong song = getOrLoadSong(songFile);
                                                        GlobalPlayer player = new GlobalPlayer(songFile, players);
                                                        player.setLoop(looping, song.getHeader().getLoopStartTick());
                                                        SongPlayer songPlayer = prepareSong(songFile, song, player);

                                                        songPlayer.play();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    return 1;
                                                }))
                                        .then(CommandManager.literal("BLOCKPOS")
                                                .then(CommandManager.argument("blockpos", BlockPosArgumentType.blockPos())
                                                        .then(CommandManager.argument("distance", IntegerArgumentType.integer(1))
                                                                .then(CommandManager.argument("fade", BoolArgumentType.bool())
                                                                        .executes(context -> {
                                                                            String songFile = StringArgumentType.getString(context, "song");
                                                                            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "listeners");
                                                                            boolean looping = BoolArgumentType.getBool(context, "looping");

                                                                            BlockPos blockPos = BlockPosArgumentType.getBlockPos(context, "blockpos");
                                                                            int distance = IntegerArgumentType.getInteger(context, "distance");
                                                                            boolean fade = BoolArgumentType.getBool(context, "fade");

                                                                            try {
                                                                                NbsSong song = getOrLoadSong(songFile);
                                                                                StaticPositionedPlayer player = new StaticPositionedPlayer(songFile, blockPos.toCenterPos(), players);
                                                                                player.setDistanceFade(distance, fade);
                                                                                player.setLoop(looping, song.getHeader().getLoopStartTick());
                                                                                SongPlayer songPlayer = prepareSong(songFile, song, player);

                                                                                songPlayer.play();
                                                                            } catch (Exception e) {
                                                                                e.printStackTrace();
                                                                            }

                                                                            return 1;
                                                                        })))))
                                        .then(CommandManager.literal("ENTITY")
                                                .then(CommandManager.argument("entity", EntityArgumentType.entity())
                                                        .then(CommandManager.argument("distance", IntegerArgumentType.integer(1))
                                                                .then(CommandManager.argument("fade", BoolArgumentType.bool())
                                                                        .executes(context -> {
                                                                            String songFile = StringArgumentType.getString(context, "song");
                                                                            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "listeners");
                                                                            boolean looping = BoolArgumentType.getBool(context, "looping");

                                                                            Entity entity = EntityArgumentType.getEntity(context, "entity");
                                                                            int distance = IntegerArgumentType.getInteger(context, "distance");
                                                                            boolean fade = BoolArgumentType.getBool(context, "fade");

                                                                            try {
                                                                                NbsSong song = getOrLoadSong(songFile);
                                                                                DynamicPositionedPlayer player = new DynamicPositionedPlayer(songFile, entity::getPos, players);
                                                                                player.setDistanceFade(distance, fade);
                                                                                player.setLoop(looping, song.getHeader().getLoopStartTick());
                                                                                SongPlayer songPlayer = prepareSong(songFile, song, player);

                                                                                songPlayer.play();
                                                                            } catch (Exception e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                            return 1;
                                                                        })))))))));
        dispatcher.register(CommandManager.literal("stopnbs")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("ALL")
                        .executes(context -> {
                            songPlayers.values().forEach(SongPlayer::stop);
                            songPlayers.clear();
                            return 1;
                        }))
                .then(CommandManager.argument("song", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            songPlayers.keySet().forEach(string -> builder.suggest("\"" + string + "\""));
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            String songFile = StringArgumentType.getString(context, "song");
                            songPlayers.remove(songFile).stop();
                            return 1;
                        })));

        dispatcher.register(CommandManager.literal("cachenbs")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("clear")
                        .executes(context -> {
                            songCache.clear();
                            return 1;
                        }))
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("song", StringArgumentType.string())
                                .suggests(Commands::getAvailableNBSFiles)
                                .executes(context -> {
                                    String songFile = StringArgumentType.getString(context, "song");
                                    songCache.put(songFile, loadSong(songFile));
                                    return 1;
                                })))
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("song", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    songCache.keySet().forEach(string -> builder.suggest("\"" + string + "\""));
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String songFile = StringArgumentType.getString(context, "song");
                                    songCache.remove(songFile);
                                    return 1;
                                }))));
    }

    public static void stopSong(String songFile) {
        songPlayers.remove(songFile).stop();
    }

    public static NbsSong getOrLoadSong(String song) {
        if (songCache.containsKey(song))
            return songCache.get(song);

        return loadSong(song);
    }

    public static NbsSong loadSong(String song) {
        return loadSong(FabricLoader.getInstance().getGameDir().resolve("nbs").resolve(song + ".nbs"));
    }

    public static NbsSong loadSong(Path path) {
        try {
            NbsSong song = (NbsSong) NoteBlockLib.readSong(path);
            Utils.preProcessAllNotes(song);
            return song;
        } catch (Exception e) {
            System.out.println("Error loading song: " + path);
            e.printStackTrace();
        }
        return null;
    }

    private static SongPlayer prepareSong(String songFile, NbsSong song, SongPlayerCallback player) {
        SongPlayer songPlayer = new SongPlayer(song.getView(), player);
        if (songPlayers.containsKey(songFile)) songPlayers.remove(songFile).stop();
        songPlayers.put(songFile, songPlayer);
        return songPlayer;
    }

    private static CompletableFuture<Suggestions> getAvailableNBSFiles(CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) {
        Path input = FabricLoader.getInstance().getGameDir().resolve("nbs");

        try (Stream<Path> files = Files.walk(input)) {
            files.forEach(path1 -> {
                if (path1.getFileName().toString().endsWith(".nbs"))
                    builder.suggest("\"" + path1.getFileName().toString().replace(".nbs", "") + "\"");
            });
        } catch (Exception ignored) {
        }

        songCache.keySet().forEach(song -> builder.suggest("\"" + song + "\""));

        return builder.buildFuture();
    }

    public static void createDir(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException exception) {
            System.out.println("Failed to create directory: " + directory);
            exception.printStackTrace();
        }
    }
}