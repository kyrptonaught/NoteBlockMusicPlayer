package net.kyrptonaught.noteblockplayer.players;

import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;
import java.util.function.Supplier;

public class DynamicPositionedPlayer extends PositionedPlayer {

    private final Supplier<Vec3d> posGetter;

    public DynamicPositionedPlayer(String name, Supplier<Vec3d> posGetter, Collection<ServerPlayerEntity> players) {
        super(name, players);
        this.posGetter = posGetter;
    }

    @Override
    public void playNote(RegistryEntry<SoundEvent> soundEvent, float pitch, float volume) {
        Vec3d pos = posGetter.get();
        for (ServerPlayerEntity player : players) {
            if (isInRange(player, pos)) {
                Vec3d playPos = pos;
                if (!fade) playPos = player.getPos();
                player.networkHandler.sendPacket(new PlaySoundS2CPacket(soundEvent, SoundCategory.RECORDS, playPos.getX(), playPos.getY(), playPos.getZ(), volume, pitch, player.getRandom().nextLong()));
            }
        }
    }
}