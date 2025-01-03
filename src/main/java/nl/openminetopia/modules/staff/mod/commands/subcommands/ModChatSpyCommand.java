package nl.openminetopia.modules.staff.mod.commands.subcommands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import nl.openminetopia.api.player.objects.MinetopiaPlayer;
import nl.openminetopia.modules.chat.utils.SpyUtils;
import nl.openminetopia.utils.ChatUtils;
import org.bukkit.entity.Player;

import java.util.Optional;

@CommandAlias("mod")
public class ModChatSpyCommand extends BaseCommand {

    @Subcommand("chatspy")
    @CommandPermission("openminetopia.mod.chatspy")
    @Description("Enables or disables ChatSpy")
    public void chatSpy(Player player) {
        Optional<MinetopiaPlayer> optional = SpyUtils.obtainPlayer(player);
        if (optional.isEmpty()) return;

        MinetopiaPlayer mPlayer = optional.get();

        mPlayer.setChatSpyEnabled(!mPlayer.isChatSpyEnabled());
        player.sendMessage(ChatUtils.color("<gold>Je hebt <yellow>ChatSpy <gold>" + (mPlayer.isChatSpyEnabled() ? "aangezet" : "uitgezet") + "!"));
    }
}
