package net.hexuscraft.core.player;

import net.hexuscraft.core.chat.F;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;

public final class PlayerSearch {

    public static Player[] onlinePlayerSearch(final Collection<? extends Player> onlinePlayers, final String searchName) {
        if (searchName.equals("*") || searchName.equals("**")) return onlinePlayers.toArray(Player[]::new);

        final List<Player> matches = new ArrayList<>();
        for (Player target : onlinePlayers) {
            final String targetName = target.getName();
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
            sender.sendMessage(F.fMain("Online Player Search", F.fMatches(Arrays.stream(matches).map(Player::getName).toArray(String[]::new), searchName)));
        return matches;
    }

    public static List<String> onlinePlayerCompletions(final Collection<? extends Player> onlinePlayers, final CommandSender sender, final boolean showSelectors) {
        final List<String> completions = new ArrayList<>();
        if (sender instanceof final Player player) {
            if (showSelectors) completions.addAll(List.of(".", "*", "**"));
            completions.addAll(onlinePlayers.stream().filter(player::canSee).map(Player::getName).toList());
        } else {
            if (showSelectors) completions.add("*");
            completions.addAll(onlinePlayers.stream().map(Player::getName).toList());
        }
        return completions;
    }

    public static OfflinePlayer offlinePlayerSearch(final String searchName) {
        //noinspection deprecation
        return Bukkit.getOfflinePlayer(searchName);
    }

    public static OfflinePlayer offlinePlayerSearch(final String searchName, final CommandSender sender) {
        if (searchName.equals(".") && sender instanceof final Player player) return player;
        return offlinePlayerSearch(searchName);
    }

    public static OfflinePlayer offlinePlayerSearch(final UUID uniqueId) {
        try {
            return offlinePlayerSearch(getNameFromUniqueId(uniqueId));
        } catch (final IOException | InterruptedException exception) {
            System.out.println("[PlayerSearch] Exception while conducting offline player search for UUID '" + uniqueId.toString() + "':");
            //noinspection CallToPrintStackTrace
            exception.printStackTrace();
            return null;
        }
    }

    public static String getNameFromUniqueId(final UUID uniqueId) throws UncheckedIOException, IOException, InterruptedException {
        return fetchMojangSession(uniqueId)._name();
    }

    public static MojangProfile fetchMojangProfile(final String name) throws IOException, URISyntaxException {
        final URL url = new URI("https://api.mojang.com/users/profiles/minecraft/" + name).toURL();

        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setInstanceFollowRedirects(false);

        final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        final JSONObject jsonObject = new JSONObject(content.toString());

        Bukkit.getLogger().info(connection.getResponseCode() + ":" + connection.getResponseMessage());

        switch (connection.getResponseCode()) {
            case 200 -> {
                UUID mojangId = UUID.fromString(addUUIDDashes(jsonObject.getString("id")));
                String mojangName = jsonObject.getString("name");

                // TODO: Caching

                return new MojangProfile(mojangId, mojangName);
            }
            case 404 -> throw new IOException("Could not locate a user with that name.");
            default -> throw new IOException(jsonObject.getString("errorMessage"));
        }
    }

    private static HttpURLConnection getMojangSessionUrlConnection(UUID uuid) throws IOException {
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
        return connection;
    }

    public static MojangSession fetchMojangSession(UUID uuid) throws IOException {
        final HttpURLConnection connection = getMojangSessionUrlConnection(uuid);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        final StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        JSONObject jsonObject = new JSONObject(content.toString());
        if (connection.getResponseCode() != 200) {
            throw new IOException(jsonObject.getString("errorMessage"));
        }

        UUID mojangId = UUID.fromString(addUUIDDashes(jsonObject.getString("id")));
        String mojangName = jsonObject.getString("name");

        HashMap<String, String> properties = new HashMap<>();

        JSONArray propertiesArray = jsonObject.getJSONArray("properties");
        for (Object propertyObject : propertiesArray) {
            JSONObject propertyJSONObject = (JSONObject) propertyObject;
            properties.put("name", propertyJSONObject.getString("name"));
            properties.put("value", propertyJSONObject.getString("value"));
        }

        // TODO: Caching
        return new MojangSession(mojangId, mojangName, properties);
    }

    private static String addUUIDDashes(String idNoDashes) {
        return new StringBuilder(idNoDashes).insert(20, '-').insert(16, '-').insert(12, '-').insert(8, '-').toString();
    }

}
