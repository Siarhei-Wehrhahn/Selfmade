package de.jackson.creatoros.commands;

import de.jackson.creatoros.CreatorOSPlugin;
import de.jackson.creatoros.domain.Season;
import de.jackson.creatoros.domain.SeasonStatus;
import de.jackson.creatoros.service.SeasonService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CreatorOSCommand implements CommandExecutor, TabCompleter {

    private final CreatorOSPlugin plugin;
    private final SeasonService seasonService;

    public CreatorOSCommand(CreatorOSPlugin plugin, SeasonService seasonService) {
        this.plugin = plugin;
        this.seasonService = seasonService;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!sender.hasPermission("creatoros.admin")) {
            sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung für dieses Kommando");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "seasons":
                handleSeasonsList(sender);
                return true;
            case "season":
                handleSeasonSubcommand(sender, label, args);
                return true;
            case "reload":
                seasonService.loadSeasons();
                sender.sendMessage(ChatColor.GREEN + "Seasons aus seasons.yml neu geladen");
                return true;
            case "save":
                seasonService.saveSeasons();
                sender.sendMessage(ChatColor.GREEN + "Seasons in seasons.yml gespeichert");
                return true;
            default:
                sendHelp(sender, label);
                return true;
        }
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(ChatColor.GOLD + "CreatorOS Admin Befehle");
        sender.sendMessage(ChatColor.YELLOW + "/" + label + " seasons "
                + ChatColor.GRAY + "– listet alle Seasons");
        sender.sendMessage(ChatColor.YELLOW + "/" + label + " season create <Name...> "
                + ChatColor.GRAY + "– neue Season anlegen");
        sender.sendMessage(ChatColor.YELLOW + "/" + label + " season delete <id> "
                + ChatColor.GRAY + "– Season löschen");
        sender.sendMessage(ChatColor.YELLOW + "/" + label + " reload "
                + ChatColor.GRAY + "– Seasons aus Datei neu laden");
        sender.sendMessage(ChatColor.YELLOW + "/" + label + " save "
                + ChatColor.GRAY + "– Seasons in Datei speichern");
    }

    private void handleSeasonsList(CommandSender sender) {
        List<Season> seasons = seasonService.getSeasons();
        if (seasons.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "Es sind aktuell keine Seasons definiert");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "Seasons (" + seasons.size() + ")");
        for (Season season : seasons) {
            sender.sendMessage(
                    ChatColor.AQUA + season.getId() + ChatColor.GRAY + " | "
                            + ChatColor.YELLOW + season.getName() + ChatColor.GRAY + " | "
                            + ChatColor.GREEN + season.getStatus().name() + ChatColor.GRAY + " | "
                            + ChatColor.WHITE + season.getStartDate() + " - " + season.getEndDate()
            );
        }
    }

    private void handleSeasonSubcommand(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Nutze /" + label + " season <create|delete|setstatus>");
            return;
        }

        String action = args[1].toLowerCase(Locale.ROOT);

        switch (action) {
            case "create":
                handleSeasonCreate(sender, args);
                break;
            case "delete":
                handleSeasonDelete(sender, args);
                break;
            case "setstatus":
                handleSeasonSetStatus(sender, args);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unbekannte Aktion "
                        + action + " nutze create, delete oder setstatus");
        }
    }

    private void handleSeasonCreate(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Nutze /creatoros season create <Name...>");
            return;
        }

        String name = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        Season season = new Season(
                name,
                "",
                "",
                ""
        );
        season.setStatus(SeasonStatus.PLANNED);

        seasonService.addSeason(season);
        seasonService.saveSeasons();

        sender.sendMessage(ChatColor.GREEN + "Season erstellt mit ID "
                + ChatColor.AQUA + season.getId()
                + ChatColor.GREEN + " und Namen "
                + ChatColor.YELLOW + season.getName());
    }

    private void handleSeasonDelete(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Nutze /creatoros season delete <id>");
            return;
        }

        String id = args[2];

        boolean existed = seasonService.getSeasonById(id).isPresent();
        seasonService.removeSeason(id);

        if (existed) {
            seasonService.saveSeasons();
            sender.sendMessage(ChatColor.GREEN + "Season mit ID "
                    + ChatColor.AQUA + id + ChatColor.GREEN + " wurde gelöscht");
        } else {
            sender.sendMessage(ChatColor.RED + "Keine Season mit ID "
                    + ChatColor.AQUA + id + ChatColor.RED + " gefunden");
        }
    }

    private void handleSeasonSetStatus(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Nutze /creatoros season setstatus <id> <PLANNED|ACTIVE|ENDED>");
            return;
        }

        String id = args[2];
        String statusStr = args[3];

        SeasonStatus status;
        try {
            status = SeasonStatus.valueOf(statusStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            sender.sendMessage(ChatColor.RED + "Ungültiger Status "
                    + statusStr + " nutze PLANNED, ACTIVE oder ENDED");
            return;
        }

        seasonService.getSeasonById(id).ifPresentOrElse(season -> {
            season.setStatus(status);
            seasonService.saveSeasons();
            sender.sendMessage(ChatColor.GREEN + "Season "
                    + ChatColor.AQUA + id + ChatColor.GREEN
                    + " Status gesetzt auf "
                    + ChatColor.YELLOW + status.name());
        }, () -> sender.sendMessage(ChatColor.RED + "Keine Season mit ID "
                + ChatColor.AQUA + id + ChatColor.RED + " gefunden"));
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args
    ) {
        List<String> result = new ArrayList<>();

        if (!sender.hasPermission("creatoros.admin")) {
            return result;
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            List<String> base = Arrays.asList("help", "seasons", "season", "reload", "save");
            for (String s : base) {
                if (s.startsWith(prefix)) {
                    result.add(s);
                }
            }
            return result;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("season")) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            List<String> base = Arrays.asList("create", "delete", "setstatus");
            for (String s : base) {
                if (s.startsWith(prefix)) {
                    result.add(s);
                }
            }
            return result;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("season")
                && (args[1].equalsIgnoreCase("delete") || args[1].equalsIgnoreCase("setstatus"))) {

            String prefix = args[2].toLowerCase(Locale.ROOT);
            for (Season season : seasonService.getSeasons()) {
                if (season.getId().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    result.add(season.getId());
                }
            }
            return result;
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("season")
                && args[1].equalsIgnoreCase("setstatus")) {

            String prefix = args[3].toLowerCase(Locale.ROOT);
            for (SeasonStatus status : SeasonStatus.values()) {
                String name = status.name();
                if (name.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    result.add(name);
                }
            }
            return result;
        }

        return result;
    }
}
