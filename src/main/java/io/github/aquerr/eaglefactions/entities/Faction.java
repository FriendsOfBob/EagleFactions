package io.github.aquerr.eaglefactions.entities;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Aquerr on 2017-07-13.
 */
public class Faction
{
    public String Name;
    public int Power;
    public List<String> Members;
    public List<String> Alliances;
    public List<String> Enemies;
    public UUID Leader;
    public List<String> Officers;
    public List<Chunk> Claims;

    public Faction(String factionName, UUID factionLeader)
    {
        this.Name = factionName;
        this.Leader = factionLeader;
        this.Power = 0;
        this.Members = new ArrayList<>();
        this.Claims = new ArrayList<>();
        this.Officers = new ArrayList<>();
        this.Alliances = new ArrayList<>();
        this.Enemies = new ArrayList<>();
    }
}
