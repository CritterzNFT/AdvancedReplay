package me.jumper251.replay.commands.replay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.github.puregero.multilib.MultiLib;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.jumper251.replay.ReplaySystem;
import me.jumper251.replay.api.ReplayAPI;
import me.jumper251.replay.commands.AbstractCommand;
import me.jumper251.replay.commands.SubCommand;
import me.jumper251.replay.filesystem.saving.ReplaySaver;
import me.jumper251.replay.utils.MathUtils;
import me.jumper251.replay.utils.ReplayManager;
import me.jumper251.replay.utils.StringUtils;

public class ReplayStartCommand extends SubCommand {

	public ReplayStartCommand(AbstractCommand parent) {
		super(parent, "start", "Records a new replay", "start [Name][:Duration] [<Players ...>]", false); 
	}

	@Override
	public boolean execute(CommandSender cs, Command cmd, String label, String[] args) {
		if (args.length < 1) return false;
		
		Player targetPlayer;
		String name;
		int duration;
		
		if (args.length == 2) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM.dd-HH:mm");
			targetPlayer = Bukkit.getPlayer(args[1]);
			name = args[1] + "-" + simpleDateFormat.format(new Date());
			duration = 0;
		} else {
			targetPlayer = Bukkit.getPlayer(args[2]);
			name = parseName(args);
			duration = parseDuration(args);
		}
		
		if (name == null || duration < 0) {
			return false;
		}
		if (name.length() > 40) {
			cs.sendMessage(ReplaySystem.PREFIX + "§cReplay name is too long.");
			return true;
		}
		if (ReplaySaver.exists(name) || ReplayManager.activeReplays.containsKey(name)) {
			cs.sendMessage(ReplaySystem.PREFIX + "§cReplay already exists.");
			return true;
		}
		
		List<Player> toRecord = new ArrayList<>();

		if (args.length > 3) {
			cs.sendMessage(ReplaySystem.PREFIX + "§cReplay can only specify 1 username.");
			return true;
		}

		Player csPlayer = (Player) cs;

		if (MultiLib.isExternalPlayer(targetPlayer)) {
			if (MultiLib.isLocalPlayer(csPlayer)) {
				MultiLib.chatOnOtherServers(csPlayer, "/" + label + " " + args[0]  + " " + args[1] + " " + args[2]);
			}
			return false;
		} else {
			toRecord.add(targetPlayer);
		}
		
		ReplayAPI.getInstance().recordReplay(name, cs, toRecord);

		if (duration <= 0) {
			cs.sendMessage(ReplaySystem.PREFIX + "§aSuccessfully started recording §e" + name + "§7.\n§7Use §6/Replay stop " + name + "§7 to save it.");
		} else {
			cs.sendMessage(ReplaySystem.PREFIX + "§aSuccessfully started recording §e" + name + "§7.\n§7The Replay will be saved after §6" + duration + "§7 seconds");
			
			new BukkitRunnable() {
				
				@Override
				public void run() {
					ReplayAPI.getInstance().stopReplay(name, true, true);
				}
			}.runTaskLater(ReplaySystem.getInstance(), duration * 20);
		}
		
		return true;
	}
	
	
	private String parseName(String[] args) {
		if (args.length >= 2) {			
			String[] split = args[1].split(":");	
			
			if (args[1].contains(":")) {
				if (split.length == 2 && split[0].length() > 0) return split[0];
			} else {
				return args[1];
			}
		}

		
		return StringUtils.getRandomString(6);
	}
	
	private int parseDuration(String[] args) {
		if (args.length < 2 || !args[1].contains(":")) return 0;
		String[] split = args[1].split(":");

		if (split.length == 2 && MathUtils.isInt(split[1])) {
			return Integer.parseInt(split[1]);
		}
		
		if (split.length == 1) {
			if (!split[0].startsWith(":") || !MathUtils.isInt(split[0])) return -1;
			
			return Integer.parseInt(split[0]);
		}
		
		return 0;
	}

	
}
