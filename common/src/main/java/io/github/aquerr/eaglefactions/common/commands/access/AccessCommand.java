package io.github.aquerr.eaglefactions.common.commands.access;

import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AccessCommand extends AbstractCommand
{
    public AccessCommand(EagleFactionsPlugin plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext args) throws CommandException
    {
        if(!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player)source;

        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction playerFaction = optionalPlayerFaction.get();

        // Access can be run only by leader and officers
        final Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition());
        if (!optionalChunkFaction.isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.concat(Text.of(Messages.THIS_PLACE_DOES_NOT_BELONG_TO_ANYONE)));

        final Faction chunkFaction = optionalChunkFaction.get();
        if (!playerFaction.getName().equals(chunkFaction.getName()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.concat(Text.of(Messages.THIS_PLACE_DOES_NOT_BELONG_TO_YOUR_FACTION)));

        if (!playerFaction.getLeader().equals(player.getUniqueId()) && !playerFaction.getOfficers().contains(player.getUniqueId()) && !super.getPlugin().getPlayerManager().hasAdminMode(player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS));

        // Get claim at player's location
        final Optional<Claim> optionalClaim = chunkFaction.getClaimAt(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition());
        final Claim claim = optionalClaim.get();
        return showAccess(player, claim);
    }

    private CommandResult showAccess(final Player player, final Claim claim)
    {
        final Text claimLocation = Text.of(TextColors.AQUA, "Location: ", TextColors.GOLD, claim.getChunkPosition());
        final Text text = Text.of(TextColors.AQUA, "Accessible by faction: ", TextColors.GOLD, claim.isAccessibleByFaction());
        final List<String> ownersNames = claim.getOwners().stream()
                .map(owner -> super.getPlugin().getPlayerManager().getFactionPlayer(owner))
                .filter(Optional::isPresent)
                .map(factionPlayer -> factionPlayer.get().getName())
                .collect(Collectors.toList());
        final Text text1 = Text.of(TextColors.AQUA, "Owners: ", TextColors.GOLD, String.join(", ", ownersNames));
        final List<Text> contents = new ArrayList<>();
        contents.add(claimLocation);
        contents.add(text);
        contents.add(text1);
        final PaginationList paginationList = PaginationList.builder().contents(contents).padding(Text.of("=")).title(Text.of(TextColors.GREEN, "Claim Access")).build();
        paginationList.sendTo(player);
        return CommandResult.success();
    }
}
