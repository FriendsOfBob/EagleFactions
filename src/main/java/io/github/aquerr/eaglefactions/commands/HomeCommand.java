package io.github.aquerr.eaglefactions.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.FactionHome;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.MainLogic;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.concurrent.TimeUnit;

public class HomeCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(source instanceof Player)
        {
            Player player = (Player)source;
            String playerFactionName = FactionLogic.getFactionName(player.getUniqueId());

            if(playerFactionName != null)
            {
                if(FactionLogic.getHome(playerFactionName) != null)
                {
                    //TODO: Wait 5-10 seconds before teleporting.

                    if (MainLogic.shouldBlockHomeAfterDeathInOwnFaction() && EagleFactions.BlockedHome.contains(player.getUniqueId()))
                    {
                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "You can't teleport to faction's home because you died recently died in your faction's land!"));
                        return CommandResult.success();
                    }
                    else
                    {
                        FactionHome factionHome = FactionLogic.getHome(playerFactionName);

                        if(MainLogic.canHomeBetweenWorlds())
                        {
                            source.sendMessage(Text.of(PluginInfo.PluginPrefix, "Stay still for ", TextColors.GOLD, MainLogic.getHomeDelayTime() + " seconds", TextColors.RESET, "!"));
                            teleportHome(player, player.getLocation().getBlockPosition(), factionHome, 0);
                        }
                        else
                        {
                            if(player.getWorld().getUniqueId().equals(factionHome.WorldUUID))
                            {
                                source.sendMessage(Text.of(PluginInfo.PluginPrefix, "Stay still for ", TextColors.GOLD, MainLogic.getHomeDelayTime() + " seconds", TextColors.RESET, "!"));
                                teleportHome(player, player.getLocation().getBlockPosition(), factionHome, 0);
                            }
                            else
                            {
                                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, "Faction's home is not in this world."));
                            }
                        }
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Faction's home is not set!"));
                }

            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You must be in a faction in order to use this command!"));
            }

        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "Only in-game players can use this command!"));
        }

        return CommandResult.success();
    }

    private void teleportHome(Player player, Vector3i lastBlockPosition, FactionHome factionHome, int seconds)
    {
        if (player.getLocation().getBlockPosition().equals(lastBlockPosition))
        {
            if (seconds >= MainLogic.getHomeDelayTime())
            {
                player.setLocation(new Location<World>(Sponge.getServer().getWorld(factionHome.WorldUUID).get(), factionHome.BlockPosition));
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, "You were teleported to faction's home!"));
            }
            else
            {
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RESET, seconds));
                Task.Builder teleportHomeTask = Sponge.getScheduler().createTaskBuilder();
                teleportHomeTask.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (seconds < MainLogic.getHomeDelayTime())
                        {
                            teleportHome(player, lastBlockPosition, factionHome, seconds + 1);
                        }
                    }
                }).delay(1, TimeUnit.SECONDS).submit(EagleFactions.getEagleFactions());
            }
        }
        else
        {
            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You did move! Teleporting has been cancelled!"));
        }
    }
}
