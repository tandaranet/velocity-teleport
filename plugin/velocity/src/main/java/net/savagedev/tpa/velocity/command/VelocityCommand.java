package net.savagedev.tpa.velocity.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.savagedev.tpa.plugin.BungeeTpPlugin;
import net.savagedev.tpa.plugin.command.BungeeTpCommand;
import net.savagedev.tpa.plugin.model.player.ProxyPlayer;
import net.savagedev.tpa.velocity.model.player.VelocityPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VelocityCommand implements SimpleCommand {
    private final BungeeTpCommand command;

    private final BungeeTpPlugin plugin;

    private final String permission;

    public VelocityCommand(BungeeTpCommand command, String permission, BungeeTpPlugin plugin) {
        this.command = command;
        this.plugin = plugin;
        this.permission = permission;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player)) {
            return;
        }

        Player playerSource = (Player) invocation.source();
        if ("Mining".equals(playerSource.getCurrentServer().get().getServerInfo().getName())) {
            TextComponent message = Component.text()
                .append(Component.text("Minen ").color(TextColor.color(118, 65, 11))
                    .decorate(TextDecoration.BOLD))
                .append(Component.text("›› ").color(TextColor.color(169, 107, 41)))
                .append(Component.text("Auf mysteriöse Weise wurde dein Teleport abgebrochen..")
                    .color(TextColor.color(169, 169, 169)))
                .build();

            playerSource.sendMessage(message);
            return;
        }

        final ProxyPlayer<?, ?> player = this.plugin.getPlayer(((Player) invocation.source()).getUniqueId())
                .orElse(new VelocityPlayer((Player) invocation.source(), this.plugin));

        this.command.execute(player, invocation.arguments());
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        if (!(invocation.source() instanceof Player)) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        Player player = (Player) invocation.source();

        if (!player.hasPermission("tp.tab")) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        final ProxyPlayer<?, ?> proxyPlayer = this.plugin.getPlayer(((Player) invocation.source()).getUniqueId())
                .orElse(new VelocityPlayer((Player) invocation.source(), this.plugin));

        final Collection<String> completions = this.command.complete(proxyPlayer, invocation.arguments());
        if (completions == null) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        return CompletableFuture.completedFuture(new ArrayList<>(completions));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(this.permission);
    }
}
