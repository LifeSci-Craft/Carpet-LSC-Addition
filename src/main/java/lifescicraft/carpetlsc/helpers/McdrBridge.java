package lifescicraft.carpetlsc.helpers;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

/**
 * Bridges {@code /!!} to MCDReforged without any client-side component.
 *
 * <h2>Why a log line</h2>
 *
 * <p>MCDR never talks to the game; it wraps the server process and reads its stdout. Its server
 * handler parses each line as
 *
 * <pre>[HH:MM:SS] [&lt;thread&gt;/&lt;level&gt;]: &lt;content&gt;</pre>
 *
 * and then tries to read {@code <content>} as a player message with
 * {@code (\ [Not Secure] )?<(?P<name>[^>]+)> (?P<message>.*)} (a {@code fullmatch}). Anything that
 * matches becomes a player chat event, and MCDR dispatches messages starting with its command
 * prefix to plugins instead of the server.
 *
 * <p>So emitting one ordinary INFO line in that exact shape — from the server thread, which is
 * where commands run — makes MCDR see a player command. Nothing is sent to any client, so the
 * command text never shows up in chat, which is the whole point of the rule.
 *
 * <p>It also means the rule is genuinely server-side only: no client mod, no extra packet, no
 * MCDR plugin. The caveat is that it depends on MCDR's default {@code vanilla} handler and on
 * {@code command_prefix} still being {@code !!}.
 */
public final class McdrBridge
{
    /** MCDR's default command prefix; also the name of the command this rule enables. */
    public static final String COMMAND_PREFIX = "!!";

    private static final Logger LOGGER = LogUtils.getLogger();

    private McdrBridge()
    {
    }

    /**
     * Emits the log line MCDR parses as {@code <player> !!<arguments>}.
     */
    public static void forward(ServerPlayer player, String arguments)
    {
        // getScoreboardName() is the raw profile name, unaffected by team prefixes or display names,
        // which is what MCDR's name validation expects.
        LOGGER.info("<{}> {}", player.getScoreboardName(), COMMAND_PREFIX + arguments);
    }
}
