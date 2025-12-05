package de.jackson.creatoros.listeners;

import de.jackson.creatoros.service.PlayerProgressService;
import de.jackson.creatoros.service.QuestService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class QuestProgressListener implements Listener {

    private final QuestService questService;
    private final PlayerProgressService progressService;

    public QuestProgressListener(QuestService questService,
                                 PlayerProgressService progressService) {
        this.questService = questService;
        this.progressService = progressService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Sorgt dafür, dass es einen Eintrag gibt
        progressService.getOrCreate(player.getUniqueId());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        questService.handleBlockBreak(player, event.getBlock().getType());
    }
}
