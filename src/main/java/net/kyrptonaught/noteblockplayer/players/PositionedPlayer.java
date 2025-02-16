package net.kyrptonaught.noteblockplayer.players;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;

public class PositionedPlayer extends Player {

    protected int distance;
    protected boolean fade;

    public PositionedPlayer(String name, Collection<ServerPlayerEntity> players) {
        super(name, players);

    }

    public void setDistanceFade(int distance, boolean fade) {
        this.distance = distance;
        this.fade = fade;
    }

    public boolean isInRange(PlayerEntity player, Vec3d pos) {
        return player.getPos().isInRange(pos, distance);
    }
}
