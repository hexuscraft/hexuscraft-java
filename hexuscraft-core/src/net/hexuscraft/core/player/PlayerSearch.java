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
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PlayerSearch {

	public static Player[] onlinePlayerSearch(Collection<? extends Player> onlinePlayers, String searchName) {
		if (searchName.equals("*") || searchName.equals("**")) return onlinePlayers.toArray(Player[]::new);

		List<Player> matches = new ArrayList<>();
		for (Player target : onlinePlayers) {
			String targetName = target.getDisplayName();
			if (targetName.equalsIgnoreCase(searchName)) return new Player[]{target};
			if (!targetName.toLowerCase().contains(searchName.toLowerCase())) continue;
			matches.add(target);
		}

		return matches.toArray(Player[]::new);
	}

	public static Player[] onlinePlayerSearch(Collection<? extends Player> onlinePlayers, String searchName, CommandSender sender, Predicate<Player[]> shouldSendMatches) {
		if (searchName.equals(".") && sender instanceof Player player) return new Player[]{player};

		List<? extends Player> onlinePlayersList = new ArrayList<>(onlinePlayers);
		if (searchName.equals("**") && sender instanceof Player player) onlinePlayersList.remove(player);

		Player[] matches = onlinePlayerSearch(onlinePlayersList, searchName);
		if (shouldSendMatches.test(matches))
			sender.sendMessage(F.fMain("Online Player Search", F.fMatches(Arrays.stream(matches).map(Player::getDisplayName).toArray(String[]::new), searchName)));
		return matches;
	}

	public static List<String> onlinePlayerCompletions(Collection<? extends Player> onlinePlayers, CommandSender sender, boolean showSelectors) {
		List<String> completions = new ArrayList<>();
		if (sender instanceof Player player) {
			completions.add(".");
			if (showSelectors) completions.addAll(List.of("*", "**"));
			completions.addAll(onlinePlayers.stream().filter(player::canSee).map(Player::getDisplayName).toList());
		} else {
			if (showSelectors) completions.add("*");
			completions.addAll(onlinePlayers.stream().map(Player::getDisplayName).toList());
		}
		return completions;
	}

	public static OfflinePlayer offlinePlayerSearch(String searchName) {
		//noinspection deprecation
		return Bukkit.getOfflinePlayer(searchName);
	}

	public static OfflinePlayer offlinePlayerSearch(String name, CommandSender sender) {
		if (name.equals(".") && sender instanceof Player player) return player;

		OfflinePlayer target = offlinePlayerSearch(name);
		if (target == null)
			sender.sendMessage(F.fMain("Offline Player Search", F.fMatches(new String[0], name)));

		return target;
	}

	public static OfflinePlayer offlinePlayerSearch(UUID uuid) throws URISyntaxException, IOException {
		if (uuid == null) return null;
		if (uuid.equals(UtilUniqueId.EMPTY_UUID)) return null;

		OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
		if (target.getName() != null && !target.getName().equals(uuid.toString()))
			return target;

		MojangSession session = fetchMojangSession(uuid);
		if (session == null)
			return null;

		return offlinePlayerSearch(session._name());
	}

	public static OfflinePlayer offlinePlayerSearch(UUID uniqueId, CommandSender sender) {
		OfflinePlayer target;
		try {
			target = offlinePlayerSearch(uniqueId);
		} catch (final Exception ex) {
			sender.sendMessage(F.fMain("Offline Player Search", "An error occurred while searching for ", F.fItem(uniqueId.toString()), ". Please try again later or contact an administrator if this issue persists."));
			return null;
		}

		if (target == null)
			sender.sendMessage(F.fMain("Offline Player Search", F.fMatches(new String[0], uniqueId.toString())));

		return target;
	}

	public static MojangSession fetchMojangSession(UUID uuid) throws URISyntaxException, IOException {
		HttpURLConnection connection = (HttpURLConnection) new URI("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replaceAll("-", "")).toURL().openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setInstanceFollowRedirects(false);

		String content;
		try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			content = in.lines().collect(Collectors.joining());
		}

		JSONObject jsonObject = new JSONObject(content);
		switch (connection.getResponseCode()) {
			case 200 -> {
			}
			case 204 -> {
				return null;
			}
			default -> throw new IOException(jsonObject.getString("errorMessage"));
		}

		JSONArray propertiesArray = jsonObject.getJSONArray("properties");
		return new MojangSession(UUID.fromString(new StringBuilder(jsonObject.getString("id")).insert(20, '-').insert(16, '-').insert(12, '-').insert(8, '-').toString()), jsonObject.getString("name"), IntStream.range(0, propertiesArray.length()).mapToObj(propertiesArray::getJSONObject).collect(Collectors.toMap(obj -> obj.getString("name"), obj -> obj.getString("value"), (_, replacement) -> replacement, HashMap::new)));
	}

}
