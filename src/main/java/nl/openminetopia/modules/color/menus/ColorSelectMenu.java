package nl.openminetopia.modules.color.menus;

import com.jazzkuh.inventorylib.objects.PaginatedMenu;
import com.jazzkuh.inventorylib.objects.icon.Icon;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import nl.openminetopia.api.player.PlayerManager;
import nl.openminetopia.api.player.objects.MinetopiaPlayer;
import nl.openminetopia.configuration.MessageConfiguration;
import nl.openminetopia.modules.color.enums.OwnableColorType;
import nl.openminetopia.modules.color.objects.OwnableColor;
import nl.openminetopia.utils.ChatUtils;
import nl.openminetopia.utils.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

@Getter
public class ColorSelectMenu extends PaginatedMenu {

    private List<OwnableColor> colors;
    private final OwnableColorType type;

    public ColorSelectMenu(Player player, OfflinePlayer target, OwnableColorType type) {
        super(ChatUtils.color(type.getDisplayName() + "<reset><dark_gray> menu"), 2);
        this.registerPageSlotsBetween(0, 8);

        this.type = type;

        MinetopiaPlayer minetopiaPlayer = PlayerManager.getInstance().getMinetopiaPlayer(target);
        if (minetopiaPlayer == null) {
            inventory.close();
            return;
        }

        colors = minetopiaPlayer.getColors().stream()
                .filter(color -> color.getClass().equals(type.correspondingClass()))
                .toList();


        ItemBuilder defaultIcon = new ItemBuilder(Material.IRON_INGOT)
                .addLoreLine("")
                .addLoreLine("<gold>Deze kleur vervalt <yellow>nooit<gold>.")
                .setName(type.getDefaultColor() + "Standaard");
        this.addItem(new Icon(defaultIcon.toItemStack(), (e) -> {
            minetopiaPlayer.setActiveColor(null, type);
            player.sendMessage(ChatUtils.color(type.getDisplayName() + " <reset><gray>veranderd naar: "
                    + type.getDefaultColor() + "Standaard"));
        }));

        colors.forEach(color -> {
            ItemBuilder icon = new ItemBuilder(Material.IRON_INGOT)
                    .addLoreLine("")
                    .setName(color.displayName());

            if (color.getExpiresAt() != -1 && color.getExpiresAt() - System.currentTimeMillis() < -1) icon.addLoreLine(MessageConfiguration.component("color_expired"));
            if (color.getExpiresAt() != -1 && color.getExpiresAt() - System.currentTimeMillis() > -1) icon.addLoreLine(MessageConfiguration.message("color_expires_in")
                    .replace("<time>", millisToTime(color.getExpiresAt() - System.currentTimeMillis())));
            if (color.getExpiresAt() == -1) icon.addLoreLine(MessageConfiguration.component("color_never_expires"));

            this.addItem(new Icon(icon.toItemStack(), (e) -> {
                minetopiaPlayer.setActiveColor(color, type);
                player.sendMessage(ChatUtils.color(type.getDisplayName() + " <reset><gray>veranderd naar: "
                        + color.displayName()));
            }));
        });

        this.addSpecialIcon(new Icon(13, new ItemBuilder(Material.LADDER).setName(MessageConfiguration.message("go_back")).toItemStack(),
                (e) -> new ColorTypeMenu(player, target).open(player)));

        this.addSpecialIcon(new Icon(14, new ItemBuilder(Material.BARRIER).setName("<red>Locked").toItemStack(),
                (e) -> new ColorLockedMenu(player, this).open(player)));
    }

    private String millisToTime(long millis) {
        long hours = millisToHours(millis);
        long minutes = millisToMinutes(millis) - (hours * 60);

        return MessageConfiguration.message("color_time_format")
                .replace("<hours>", String.valueOf(hours))
                .replace("<minutes>", String.valueOf(minutes))
                .replace("<seconds>", String.valueOf(millisToSeconds(millis)));
    }

    private int millisToHours(long millis) {
        return (int) (millis / 1000 / 60 / 60);
    }

    private int millisToMinutes(long millis) {
        return (int) (millis / 1000 / 60);
    }

    private int millisToSeconds(long millis) {
        return (int) (millis / 1000);
    }

    @Override
    public Icon getPreviousPageItem() {
        return new Icon(9, new ItemBuilder(Material.ARROW)
                .setName(MessageConfiguration.message("previous_page"))
                .toItemStack(), event -> event.setCancelled(true));
    }

    @Override
    public Icon getNextPageItem() {
        return new Icon(17, new ItemBuilder(Material.ARROW)
                .setName(MessageConfiguration.message("next_page"))
                .toItemStack(), event -> event.setCancelled(true));
    }

}
