package net.hexuscraft.core.scoreboard;

import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.concurrent.atomic.AtomicInteger;

public class CustomSidebar {

	CustomScoreboard _customScoreboard;
	String _title;
	BukkitTask _titleScroller;
	Objective _objective;
	String[] _lines;

	CustomSidebar(CustomScoreboard customScoreboard) {
		_customScoreboard = customScoreboard;
		_title = "§6§lHEXUSCRAFT";
		_titleScroller = null;
		_objective = customScoreboard._bukitScoreboard.registerNewObjective("sidebar", "dummy");
		_objective.setDisplayName(_title);
		_objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		_lines = new String[16];
	}

	public void setTitle(String title) {
		if (title.length() > 16) title = "        " + title;
		if (_title.equals(title)) return;
		if (_titleScroller != null) {
			_titleScroller.cancel();
			_titleScroller = null;
		}

		_objective.setDisplayName("§f§l" + title.substring(0, Math.min(16, title.length())));
		_title = title;

		if (title.length() <= 16) return;

		AtomicInteger atomicIndex = new AtomicInteger();
		String finalTitle = title;
		_titleScroller = _customScoreboard._coreScoreboard._hexusPlugin.runAsyncTimer(() -> {
			int index = atomicIndex.updateAndGet(operand -> (operand + 1) % finalTitle.length());

			_objective.setDisplayName("§f§l" + (index + 16 > finalTitle.length() ?
				finalTitle.substring(index) +
					finalTitle.substring(0, 16 - (finalTitle.length() - index)) :
				finalTitle.substring(index, index + 16)));
		}, 4, 4);
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
