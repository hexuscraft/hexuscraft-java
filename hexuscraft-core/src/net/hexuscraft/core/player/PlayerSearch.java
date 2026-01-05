package net.hexuscraft.core.player;

import net.hexuscraft.common.utils.F;
import net.hexuscraft.common.utils.UtilUniqueId;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class PlayerSearch {

    public static Player[] onlinePlayerSearch(final Collection<? extends Player> onlinePlayers, final String searchName) {
        if (searchName.equals("*") || searchName.equals("**")) return onlinePlayers.toArray(Player[]::new);

        final List<Player> matches = new ArrayList<>();
        for (Player target : onlinePlayers) {
            final String targetName = target.getDisplayName();
            if (targetName.equals(searchName)) return new Player[]{target};
            if (!targetName.contains(searchName)) continue;
            matches.add(target);
        }

        return matches.toArray(Player[]::new);
    }

    public static Player[] onlinePlayerSearch(final Collection<? extends Player> onlinePlayers, final String searchName, final CommandSender sender, final Predicate<Player[]> shouldSendMatches) {
        if (searchName.equals(".") && sender instanceof final Player player) return new Player[]{player};

        final List<? extends Player> onlinePlayersList = new ArrayList<>(onlinePlayers);
        if (searchName.equals("**") && sender instanceof final Player player) onlinePlayersList.remove(player);

        final Player[] matches = onlinePlayerSearch(onlinePlayersList, searchName);
        if (shouldSendMatches.test(matches))
            sender.sendMessage(F.fMain("Online Player Search", F.fMatches(Arrays.stream(matches).map(Player::getDisplayName).toArray(String[]::new), searchName)));
        return matches;
    }

    public static List<String> onlinePlayerCompletions(final Collection<? extends Player> onlinePlayers, final CommandSender sender, final boolean showSelectors) {
        final List<String> completions = new ArrayList<>();
        if (sender instanceof final Player player) {
            completions.add(".");
            if (showSelectors) completions.addAll(List.of("*", "**"));
            completions.addAll(onlinePlayers.stream().filter(player::canSee).map(Player::getDisplayName).toList());
        } else {
            if (showSelectors) completions.add("*");
            completions.addAll(onlinePlayers.stream().map(Player::getDisplayName).toList());
        }
        return completions;
    }

    public static OfflinePlayer offlinePlayerSearch(final String searchName) {
        //noinspection deprecation
        return Bukkit.getOfflinePlayer(searchName);
    }

    public static OfflinePlayer offlinePlayerSearch(final String searchName, final CommandSender sender) {
        if (searchName.equals(".") && sender instanceof final Player player) return player;
        final OfflinePlayer targetOfflinePlayer = offlinePlayerSearch(searchName);
        if (targetOfflinePlayer == null)
            sender.sendMessage(F.fMain("Offline Player Search", F.fMatches(new String[0], searchName)));
        return targetOfflinePlayer;
    }

    public static OfflinePlayer offlinePlayerSearch(final UUID uniqueId) throws IOException {
        if (uniqueId.equals(UtilUniqueId.EMPTY_UUID)) return null;

        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uniqueId);
        if (offlinePlayer.getName() != null && !offlinePlayer.getName().equals(uniqueId.toString()))
            return offlinePlayer;

        return offlinePlayerSearch(fetchMojangSession(uniqueId)._name());
    }

    public static OfflinePlayer offlinePlayerSearch(final UUID uniqueId, final CommandSender sender) throws IOException {
        final OfflinePlayer targetOfflinePlayer = offlinePlayerSearch(uniqueId);
        if (targetOfflinePlayer == null)
            sender.sendMessage(F.fMain("Offline Player Search", F.fMatches(new String[0], uniqueId.toString())));
        return targetOfflinePlayer;
    }

    public static MojangSession fetchMojangSession(UUID uuid) throws IOException {
        final URL url;
        try {
            url = new URI("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replaceAll("-", "")).toURL();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setInstanceFollowRedirects(false);

        final String content;
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            content = in.lines().collect(Collectors.joining());
        }

        final JSONObject jsonObject = new JSONObject(content);
        if (connection.getResponseCode() != 200) throw new IOException(jsonObject.getString("errorMessage"));

        final JSONArray propertiesArray = jsonObject.getJSONArray("properties");
        return new MojangSession(UUID.fromString(new StringBuilder(jsonObject.getString("id")).insert(20, '-').insert(16, '-').insert(12, '-').insert(8, '-').toString()), jsonObject.getString("name"), IntStream.range(0, propertiesArray.length()).mapToObj(propertiesArray::getJSONObject).collect(Collectors.toMap(obj -> obj.getString("name"), obj -> obj.getString("value"), (_, replacement) -> replacement, HashMap::new)));
    }

}
