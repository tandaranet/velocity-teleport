package net.savagedev.tpa.plugin.commands;

import net.savagedev.tpa.plugin.BungeeTpPlugin;
import net.savagedev.tpa.plugin.config.Lang;
import net.savagedev.tpa.plugin.config.Lang.Placeholder;
import net.savagedev.tpa.plugin.config.Setting;
import net.savagedev.tpa.plugin.model.player.ProxyPlayer;
import net.savagedev.tpa.plugin.model.request.TeleportRequest;
import net.savagedev.tpa.plugin.utils.TimeUtils;

public class TpaCommand extends AbstractTeleportCommand {
    public TpaCommand(BungeeTpPlugin plugin) {
        super(plugin, "tpa");
    }

    @Override
    protected void teleport(ProxyPlayer<?, ?> player, ProxyPlayer<?, ?> other, boolean paid) {
        final boolean sent = this.plugin.getTeleportManager().addRequest(other, new TeleportRequest(player, other, TeleportRequest.Direction.TO_RECEIVER, paid));
        if (sent) {
            Lang.TPA_REQUEST_RECEIVED.send(other, new Lang.Placeholder("%player%", player.getName()),
                    new Placeholder("%expires%", TimeUtils.formatTime(Setting.TP_REQUEST_EXPIRE.asLong() * 1000L, Setting.TIME_FORMAT.asTimeLengthFormat())));
        }
        Lang.TPA_REQUEST_SENT.send(player, new Lang.Placeholder("%player%", other.getName()),
                new Placeholder("%expires%", TimeUtils.formatTime(Setting.TP_REQUEST_EXPIRE.asLong() * 1000L, Setting.TIME_FORMAT.asTimeLengthFormat())));
    }
}
