package net.hexuscraft.core.store;

import net.hexuscraft.common.database.messages.SalesProcessedMessage;
import net.hexuscraft.common.utils.F;
import net.hexuscraft.core.HexusPlugin;
import net.hexuscraft.core.MiniPlugin;
import net.hexuscraft.core.database.CoreDatabase;
import org.bukkit.Sound;

import java.util.Map;

public class CoreStore extends MiniPlugin<HexusPlugin> {
	CoreDatabase _coreDatabase;

	public CoreStore(HexusPlugin plugin) {
		super(plugin, "Store");
	}

	@Override
	public void onLoad(Map<Class<? extends MiniPlugin<? extends HexusPlugin>>, MiniPlugin<? extends HexusPlugin>> dependencies) {
		_coreDatabase = (CoreDatabase) dependencies.get(CoreDatabase.class);
	}

	@Override
	public void onEnable() {
		_coreDatabase._database.registerConsumer(SalesProcessedMessage.CHANNEL_NAME, (_, _, rawMessage) -> {
			SalesProcessedMessage message = SalesProcessedMessage.fromString(rawMessage);
			_hexusPlugin.getServer().getOnlinePlayers().stream().filter(player -> player.getUniqueId().equals(message._playerUUID())).forEach(player -> {
				player.sendMessage(F.fMain(this, F.fSuccess("Your purchase of ", F.fItem(message._packageName()), " has been successfully processed! You may need to rejoin the server in order to receive your perks. Thank you for supporting our community. <3")));
				player.playSound(player.getLocation(), Sound.LEVEL_UP, Float.MAX_VALUE, 1);
			});
		});
	}
}
