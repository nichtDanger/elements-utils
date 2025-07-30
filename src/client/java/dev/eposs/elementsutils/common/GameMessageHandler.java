package dev.eposs.elementsutils.common;

import dev.eposs.elementsutils.ElementsUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Handler for sending and filtering modlist commands and related chat messages.
 */
public class GameMessageHandler {
	/**
	 * Queue for pending modlist commands.
	 */
	private static final Queue<String> pendingCommands = new LinkedList<>();

	/**
	 * Timestamp until which chat messages are suppressed.
	 */
	private static long suppressUntil = 0;

	/**
	 * Splits the mod list into multiple commands and adds them to the queue.
	 *
	 * @param client    The current Minecraft client instance.
	 * @param modIds    List of mod IDs to be sent.
	 * @param prefix    Prefix for each command (e.g. "/modlist ").
	 * @param maxLength Maximum length of a command.
	 */
	public static void queueModlistCommands(MinecraftClient client, List<String> modIds, String prefix, int maxLength) {
		List<String> messages = splitModList(modIds, prefix, maxLength);
		pendingCommands.addAll(messages);
		suppressUntil = System.currentTimeMillis() + 10000;
	}

	/**
	 * Filters chat messages to hide unwanted responses to modlist commands.
	 *
	 * @param text The received chat message.
	 * @return true if the message should be shown, false otherwise.
	 */
	public static boolean onGameMessage(Text text) {
		String msg = text.getString();
		boolean suppress = System.currentTimeMillis() < suppressUntil;
		return !suppress || !msg.startsWith("Unknown or incomplete command") || !msg.contains("modlist");
	}

	/**
	 * Sends all pending modlist commands to the server.
	 *
	 * @param client The current Minecraft client instance.
	 */
	public static void processPendingCommands(MinecraftClient client) {
		if (!pendingCommands.isEmpty() && client.player != null) {
			while (!pendingCommands.isEmpty()) {
				String cmd = pendingCommands.poll();
				client.player.networkHandler.sendChatCommand(cmd.startsWith("/") ? cmd.substring(1) : cmd);
			}
			ElementsUtils.LOGGER.info("Mod List sent to Server-Console.");
		}
	}

	/**
	 * Splits a list of mods into multiple strings so that each command does not exceed the maximum length.
	 *
	 * @param mods      The mod IDs.
	 * @param prefix    Prefix for each command.
	 * @param maxLength Maximum length of a command.
	 * @return List of split commands.
	 */
	private static List<String> splitModList(List<String> mods, String prefix, int maxLength) {
		List<String> result = new LinkedList<>();
		StringBuilder current = new StringBuilder(prefix);

		for (String mod : mods) {
			if (current.length() + mod.length() + 1 > maxLength) {
				result.add(current.toString().trim());
				current = new StringBuilder(prefix);
			}
			current.append(mod).append(" ");
		}
		if (current.length() > prefix.length()) {
			result.add(current.toString().trim());
		}
		return result;
	}
}