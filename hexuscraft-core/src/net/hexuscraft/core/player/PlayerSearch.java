package net.hexuscraft.core.player;

import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.chat.F;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
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

public class PlayerSearch extends MiniPlugin {

    public PlayerSearch(JavaPlugin plugin) {
        super(plugin, "Player Search");
    }

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

    public static Player[] onlinePlayerSearch(Collection<? extends Player> onlinePlayers, String searchName, CommandSender executor) {
        Player[] matches = onlinePlayerSearch(onlinePlayers, searchName);
        List<String> names = new ArrayList<>();
        Arrays.stream(matches).toList().forEach(match -> names.add(match.getName()));
        if (matches.length != 1) {
            if (matches.length == 0) {
                executor.sendMessage(F.fMain("Online Player Search") + F.fItem(matches.length + " Matches") + " for " + F.fItem(searchName) + ".");
            } else {
                executor.sendMessage(F.fMain("Online Player Search") + F.fItem(matches.length + " Matches") + " for " + F.fItem(searchName) + ".");
                executor.sendMessage(F.fMain() + "Matches: " + F.fList(names.toArray(String[]::new)));
            }
        }
        return matches;
    }

    // TODO: Profile caching
    public static MojangProfile fetchMojangProfile(String name) throws IOException {
        URL url;
        try {
            url = new URI("https://api.mojang.com/users/profiles/minecraft/" + name).toURL();
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex.getMessage());
        }

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setInstanceFollowRedirects(false);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder content = new StringBuilder();
        String inputLine;
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

        //noinspection UnnecessaryLocalVariable
        MojangProfile profile = new MojangProfile(mojangId, mojangName);
        return profile;
    }

    public static MojangProfile fetchMojangProfile(String name, Player sender) {
        try {
            return fetchMojangProfile(name);
        } catch (IOException ex) {
            sender.sendMessage(F.fMain("Profile Search") + F.fBoolean("Error while fetching profile of ", false) + F.fItem(name) + "\n" + F.fMain() + ex.getMessage());
            return null;
        }
    }

    // TODO: Session caching
    public static MojangSession fetchMojangSession(UUID uuid) throws IOException {
        URL url;
        try {
            url = new URI("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replaceAll("-", "")).toURL();
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex.getMessage());
        }

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setInstanceFollowRedirects(false);

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

        //noinspection UnnecessaryLocalVariable
        MojangSession session = new MojangSession(mojangId, mojangName, properties);
        return session;
    }

    static String addUUIDDashes(String idNoDashes) {
        StringBuilder idBuilder = new StringBuilder(idNoDashes)
                .insert(20, '-')
                .insert(16, '-')
                .insert(12, '-')
                .insert(8, '-');
        return idBuilder.toString();
    }

//    public static String[] getVisiblePlayers(MiniPlugin miniPlugin)

}
