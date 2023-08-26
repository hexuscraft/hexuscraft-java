package net.hexuscraft.core.player;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.F;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
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

public class PlayerSearch {

    public static Player[] onlinePlayerSearch(final Collection<? extends Player> onlinePlayers, final String searchName) {
        if (searchName.equals("*") || searchName.equals("**")) {
            return onlinePlayers.toArray(Player[]::new);
        }

        List<Player> matches = new ArrayList<>();
        for (Player target : onlinePlayers) {
            String targetName = target.getName();
            if (targetName.equals(searchName)) {
                return new Player[]{target};
            }
            if (!targetName.contains(searchName)) {
                continue;
            }
            matches.add(target);
        }
        return matches.toArray(Player[]::new);
    }

    public static Player[] onlinePlayerSearch(final Collection<? extends Player> onlinePlayers, final String searchName, final CommandSender sender) {
        List<? extends Player> onlinePlayersList = new ArrayList<>(onlinePlayers);

        if (searchName.equals("**") && sender instanceof Player player) {
            onlinePlayersList.remove(player);
        }

        final Player[] matches = onlinePlayerSearch(onlinePlayersList, searchName);

        sender.sendMessage(F.fMain("Player Search") + F.fItem(matches.length + (matches.length == 1 ? " Match" : " Matches")) + " for " + F.fItem(searchName) + (matches.length == 0 ? "." : ":\n" +
                F.fMain() + F.fList(Arrays.stream(matches).map(Player::getName).toArray(String[]::new))));

        return matches;
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

    public static MojangProfile fetchMojangProfile(String name, CommandSender sender) {
        sender.sendMessage(F.fMain("Profile Fetcher") + "Fetching profile of " + F.fItem(name) + "...");
        try {
            return fetchMojangProfile(name);
        } catch (IOException | URISyntaxException ex) {
            sender.sendMessage(F.fMain("Profile Fetcher") + F.fError("Error while fetching profile of ") + F.fItem(name) + F.fError(":") + "\n"
                    + F.fMain() + ex.getMessage());
            return null;
        }
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

    @SuppressWarnings("unused")
    public static MojangSession fetchMojangSession(UUID uuid, CommandSender sender) {
        sender.sendMessage(F.fMain("Session Fetcher") + "Fetching session of " + F.fItem(uuid.toString()) + "...");
        try {
            return fetchMojangSession(uuid);
        } catch (IOException ex) {
            sender.sendMessage(F.fMain("Session Fetcher") + F.fError("Error while fetching session of ") + F.fItem(uuid.toString()) + F.fError(":") + "\n"
                    + F.fMain() + ex.getMessage());
            return null;
        }
    }

    private static String addUUIDDashes(String idNoDashes) {
        return new StringBuilder(idNoDashes)
                .insert(20, '-')
                .insert(16, '-')
                .insert(12, '-')
                .insert(8, '-')
                .toString();
    }

}
