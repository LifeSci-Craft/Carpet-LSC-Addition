package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Makes a {@code /carpet} toggle permanent (rule {@code PersistentRules}).
 *
 * <p>Vanilla Carpet keeps a rule change in memory only: it is lost on restart unless the value is
 * also written into {@code config/carpet.conf}. With this rule on, every change is written back, so
 * one command is enough and no manual file editing is needed.
 */
public final class PersistentRules
{
    private static final Path FILE = Path.of("config", "carpet.conf");

    private PersistentRules()
    {
    }

    public static void remember(String rule, String value)
    {
        if (!CarpetLscSettings.PersistentRules || "PersistentRules".equals(rule))
        {
            return;
        }

        List<String> lines;

        try
        {
            lines = Files.isRegularFile(FILE)
                    ? new ArrayList<>(Files.readAllLines(FILE, StandardCharsets.UTF_8))
                    : new ArrayList<>();
        }
        catch (IOException unreadable)
        {
            return;
        }

        String entry = rule + "=" + value;
        boolean replaced = false;

        for (int i = 0; i < lines.size(); i++)
        {
            if (lines.get(i).startsWith(rule + "="))
            {
                lines.set(i, entry);
                replaced = true;
                break;
            }
        }

        if (!replaced)
        {
            lines.add(entry);
        }

        try
        {
            Files.createDirectories(FILE.getParent());
            Files.write(FILE, lines, StandardCharsets.UTF_8);
        }
        catch (IOException unwritable)
        {
            // Nothing to do: the toggle still applies for this session.
        }
    }
}
