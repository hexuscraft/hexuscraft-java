package net.hexuscraft.core.scoreboard;

import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

public class CustomSidebar {

	CustomScoreboard _customScoreboard;
	String _title;
	Objective _objective;
	String[] _lines;

	CustomSidebar(CustomScoreboard customScoreboard) {
		_customScoreboard = customScoreboard;
		_title = "§6§lHEXUSCRAFT";
		_objective = customScoreboard._bukitScoreboard.registerNewObjective("sidebar", "dummy");
		_objective.setDisplayName(_title);
		_objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		_lines = new String[16];
	}

	public void setTitle(String title) {
		_title = title;
		_objective.setDisplayName("§f§l" + title.substring(0, Math.min(16, title.length())));

		// TODO: title scrolling
//		if (sidebarTitle.length() > SIDEBAR_TITLE_MAX_CHARS) {
//			sidebarTasks.add(_hexusPlugin.runSyncTimer(() ->
//			{
//				int index = sidebarTitleIndex.getAndUpdate(operand -> (operand + 1) % sidebarTitle.length());
//
//				sidebarObjective.setDisplayName(C.cWhite +
//					C.fBold +
//					(index + SIDEBAR_TITLE_MAX_CHARS > sidebarTitle.length() ?
//						sidebarTitle.substring(index) +
//							sidebarTitle.substring(0,
//								SIDEBAR_TITLE_MAX_CHARS - (sidebarTitle.length() - index)) :
//						sidebarTitle.substring(index, index + SIDEBAR_TITLE_MAX_CHARS)));
//			}, 0, 4));
//		}
	}

	public void setLines(String... lines) {
		if (lines.length > 15) throw new RuntimeException("lines.length > 15");

		for (int i = 1; i < 16; i++) {
			setLine(i, i <= lines.length ? lines[lines.length - i] : null);
		}
	}

	public void setLine(int line, String text) {
		if (line < 1) throw new RuntimeException("line < 1");
		if (line > 15) throw new RuntimeException("line > 15");

		if (text == null) {
			if (_lines[line] == null) return;
			_customScoreboard._bukitScoreboard.resetScores(_lines[line]);
			_lines[line] = null;
			return;
		}

		text = "§" + Integer.toHexString(line) + "§r" + text;

		if (_lines[line] != null) {
			if (_lines[line].equals(text)) return;
			_customScoreboard._bukitScoreboard.resetScores(_lines[line]);
		}

		_lines[line] = text;
		_objective.getScore(text).setScore(line);
	}

}
