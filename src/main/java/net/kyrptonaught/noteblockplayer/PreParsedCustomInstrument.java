package net.kyrptonaught.noteblockplayer;

import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.raphimc.noteblocklib.format.nbs.model.NbsCustomInstrument;

public class PreParsedCustomInstrument extends NbsCustomInstrument {
    private final float parsedPitch;
    private final RegistryEntry<SoundEvent> soundEvent;

    public PreParsedCustomInstrument(String name, float parsedPitch) {
        super();
        this.soundEvent = Registries.SOUND_EVENT.getEntry(SoundEvent.of(Identifier.of(name)));
        this.parsedPitch = parsedPitch;
    }

    public float getParsedPitch() {
        return parsedPitch;
    }

    public RegistryEntry<SoundEvent> getInstrument() {
        return soundEvent;
    }
}
