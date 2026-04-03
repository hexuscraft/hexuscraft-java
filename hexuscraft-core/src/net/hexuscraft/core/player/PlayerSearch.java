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
import java.net.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PlayerSearch
{

    public static Player[] onlinePlayerSearch(Collection<? extends Player> onlinePlayers, String searchName)
    {
        if (searchName.equals("*") || searchName.equals("**"))
        {
            return onlinePlayers.toArray(Player[]::new);
        }

        List<Player> matches = new ArrayList<>();
        for (Player target : onlinePlayers)
        {
            String targetName = target.getDisplayName();
            if (targetName.equalsIgnoreCase(searchName))
            {
                return new Player[]{target};
            }
            if (!targetName.toLowerCase().contains(searchName.toLowerCase()))
            {
                continue;
            }
            matches.add(target);
        }

        return matches.toArray(Player[]::new);
    }

    public static Player[] onlinePlayerSearch(Collection<? extends Player> onlinePlayers,
            String searchName,
            CommandSender sender,
            Predicate<Player[]> shouldSendMatches)
    {
        if (searchName.equals(".") && sender instanceof Player player)
        {
            return new Player[]{player};
        }

        List<? extends Player> onlinePlayersList = new ArrayList<>(onlinePlayers);
        if (searchName.equals("**") && sender instanceof Player player)
        {
            onlinePlayersList.remove(player);
        }

        Player[] matches = onlinePlayerSearch(onlinePlayersList, searchName);
        if (shouldSendMatches.test(matches))
        {
            sender.sendMessage(F.fMain("Online Player Search",
                    F.fMatches(Arrays.stream(matches).map(Player::getDisplayName).toArray(String[]::new), searchName)));
        }
        return matches;
    }

    public static List<String> onlinePlayerCompletions(Collection<? extends Player> onlinePlayers,
            CommandSender sender,
            boolean showSelectors)
    {
        List<String> completions = new ArrayList<>();
        if (sender instanceof Player player)
        {
            completions.add(".");
            if (showSelectors)
            {
                completions.addAll(List.of("*", "**"));
            }
            completions.addAll(onlinePlayers.stream().filter(player::canSee).map(Player::getDisplayName).toList());
        }
        else
        {
            if (showSelectors)
            {
                completions.add("*");
            }
            completions.addAll(onlinePlayers.stream().map(Player::getDisplayName).toList());
        }
        return completions;
    }

    @Deprecated(since = "2026-01-17")
    public static OfflinePlayer offlinePlayerSearch(String searchName)
    {
        //noinspection deprecation
        return Bukkit.getOfflinePlayer(searchName);
    }

    public static OfflinePlayer offlinePlayerSearch(String searchName, CommandSender sender)
    {
        if (searchName.equals(".") && sender instanceof Player player)
        {
            return player;
        }
        OfflinePlayer targetOfflinePlayer = offlinePlayerSearch(searchName);
        if (targetOfflinePlayer == null)
        {
            sender.sendMessage(F.fMain("Offline Player Search", F.fMatches(new String[0], searchName)));
        }
        return targetOfflinePlayer;
    }

    public static OfflinePlayer offlinePlayerSearch(UUID uniqueId)
    {
        if (uniqueId.equals(UtilUniqueId.EMPTY_UUID))
        {
            return null;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uniqueId);
        if (offlinePlayer.getName() != null && !offlinePlayer.getName().equals(uniqueId.toString()))
        {
            return offlinePlayer;
        }

        MojangSession mojangSession = fetchMojangSession(uniqueId);
        if (mojangSession == null)
        {
            return null;
        }

        return offlinePlayerSearch(mojangSession._name());
    }

    public static OfflinePlayer offlinePlayerSearch(UUID uniqueId, CommandSender sender)
    {
        OfflinePlayer targetOfflinePlayer = offlinePlayerSearch(uniqueId);
        if (targetOfflinePlayer == null)
        {
            sender.sendMessage(F.fMain("Offline Player Search", F.fMatches(new String[0], uniqueId.toString())));
        }
        return targetOfflinePlayer;
    }

    public static MojangSession fetchMojangSession(UUID uuid)
    {
        URL url;
        try
        {
            url = new URI("https://sessionserver.mojang.com/session/minecraft/profile/" +
                    uuid.toString().replaceAll("-", "")).toURL();
        }
        catch (URISyntaxException | MalformedURLException ex)
        {
            throw new RuntimeException(ex);
        }

        try
        {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setInstanceFollowRedirects(false);

            String content;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream())))
            {
                content = in.lines().collect(Collectors.joining());
            }

            JSONObject jsonObject = new JSONObject(content);
            if (connection.getResponseCode() != 200)
            {
                throw new IOException(jsonObject.getString("errorMessage"));
            }

            JSONArray propertiesArray = jsonObject.getJSONArray("properties");
            return new MojangSession(UUID.fromString(new StringBuilder(jsonObject.getString("id")).insert(20, '-')
                    .insert(16, '-')
                    .insert(12, '-')
                    .insert(8, '-')
                    .toString()),
                    jsonObject.getString("name"),
                    IntStream.range(0, propertiesArray.length())
                            .mapToObj(propertiesArray::getJSONObject)
                            .collect(Collectors.toMap(obj -> obj.getString("name"),
                                    obj -> obj.getString("value"),
                                    (_, replacement) -> replacement,
                                    HashMap::new)));
        }
        catch (IOException ex)
        {
            return null;
        }
    }

}
