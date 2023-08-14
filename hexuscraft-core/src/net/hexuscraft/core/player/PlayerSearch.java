package net.hexuscraft.core.player;

import net.hexuscraft.core.chat.F;
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

public class PlayerSearch {

    public static Player[] onlinePlayerSearch(Collection<? extends Player> onlinePlayers, String searchName) {
        List<Player> matches = new ArrayList<>();
        for (Player target : onlinePlayers) {
            String targetName = target.getName();
            if (targetName.equals(searchName)) {
                return new Player[]{target};
            }
            if (targetName.contains(searchName)) {
                matches.add(target);
            }
        }
        return matches.toArray(new Player[0]);
    }

    public static Player[] onlinePlayerSearch(final Collection<? extends Player> onlinePlayers, final String searchName, final CommandSender executor) {
        final Player[] matches = onlinePlayerSearch(onlinePlayers, searchName);
        final List<String> names = new ArrayList<>();
        Arrays.stream(matches).toList().forEach(match -> names.add(match.getName()));
        if (matches.length != 1) {
            if (matches.length == 0) {
                executor.sendMessage(F.fMain("Player Search") + F.fItem(matches.length + " Matches") + " for " + F.fItem(searchName) + ".");
            } else {
                executor.sendMessage(F.fMain("Player Search") + F.fItem(matches.length + " Matches") + " for " + F.fItem(searchName) + ". Matches:\n"
                        + F.fMain() + F.fList(names.toArray(String[]::new)));
            }
        }
        return matches;
    }

    public static MojangProfile fetchMojangProfile(final String name) throws IOException {
        final URL url;
        try {
            url = new URI("https://api.mojang.com/users/profiles/minecraft/" + name).toURL();
        } catch (final URISyntaxException ex) {
            throw new RuntimeException(ex.getMessage());
        }

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
        switch (connection.getResponseCode()) {
            case 200 -> {
            }
            case 404 -> throw new IOException("Could not locate a user with that name.");
            default -> throw new IOException(jsonObject.getString("errorMessage"));
        }

        UUID mojangId = UUID.fromString(addUUIDDashes(jsonObject.getString("id")));
        String mojangName = jsonObject.getString("name");

        // TODO: Caching
        return new MojangProfile(mojangId, mojangName);
    }

    public static MojangProfile fetchMojangProfile(String name, CommandSender sender) {
        try {
            return fetchMojangProfile(name);
        } catch (IOException ex) {
            sender.sendMessage(F.fMain("Profile Fetcher") + F.fError("Error while fetching profile of ") + F.fItem(name) + F.fError(":") + "\n"
                    + F.fMain() + ex.getMessage());
            return null;
        }
    }

    public static MojangSession fetchMojangSession(UUID uuid) throws IOException {
        HttpURLConnection connection = getHttpURLConnection(uuid);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
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

    public static MojangSession fetchMojangSession(UUID uuid, CommandSender sender) {
        try {
            return fetchMojangSession(uuid);
        } catch (IOException ex) {
            sender.sendMessage(F.fMain("Session Fetcher") + F.fError("Error while fetching session of ") + F.fItem(uuid.toString()) + F.fError(":") + "\n"
                    + F.fMain() + ex.getMessage());
            return null;
        }
    }

    private static HttpURLConnection getHttpURLConnection(UUID uuid) throws IOException {
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

    private static String addUUIDDashes(String idNoDashes) {
        return new StringBuilder(idNoDashes)
                .insert(20, '-')
                .insert(16, '-')
                .insert(12, '-')
                .insert(8, '-')
                .toString();
    }

}
