package net.kyrptonaught.noteblockplayer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class NoteBlockPlayer implements ModInitializer {

    @Override
    public void onInitialize() {
        Commands.onInitialize();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Commands.registerCommands(dispatcher));
    }
}
