package lifescicraft.carpetlsc.helpers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tunable numbers behind the two boost rules, changed with {@code /lsc}.
 *
 * <p>Every vehicle has three values: the per-tick acceleration at the start of a burst, how long the
 * burst lasts in seconds, and the decay exponent (1 is linear, 2 fades fast at first, larger values
 * fade later). They are stored in {@code config/carpet-lsc-addition.properties} so a restart keeps
 * whatever the server owner set.
 */
public final class BoostConfig
{
    public static final String[] VEHICLES = {"boat", "minecart", "ghast"};
    public static final String[] VARIABLES = {"acceleration", "duration", "decay"};

    private static final Path FILE = Path.of("config", "carpet-lsc-addition.properties");
    private static final Map<String, Double> VALUES = new LinkedHashMap<>();
    private static boolean loaded;

    static
    {
        defaults();
    }

    private BoostConfig()
    {
    }

    public static void defaults()
    {
        put("boat", 0.45, 5.0, 2.0);
        put("minecart", 0.24, 5.0, 2.0);
        put("ghast", 0.65, 5.0, 2.0);
    }

    private static void put(String vehicle, double acceleration, double duration, double decay)
    {
        VALUES.put(vehicle + ".acceleration", acceleration);
        VALUES.put(vehicle + ".duration", duration);
        VALUES.put(vehicle + ".decay", decay);
    }

    public static double get(String vehicle, String variable)
    {
        load();
        return VALUES.getOrDefault(vehicle + "." + variable, 0.0);
    }

    public static boolean set(String vehicle, String variable, double value)
    {
        load();

        String key = vehicle + "." + variable;

        if (!VALUES.containsKey(key))
        {
            return false;
        }

        boolean inRange = switch (variable)
        {
            case "acceleration" -> value > 0.0 && value <= 5.0;
            case "duration" -> value >= 0.5 && value <= 60.0;
            default -> value >= 0.5 && value <= 6.0;
        };

        if (!inRange)
        {
            return false;
        }

        VALUES.put(key, value);
        save();
        return true;
    }

    public static double acceleration(String vehicle)
    {
        return get(vehicle, "acceleration");
    }

    public static int durationTicks(String vehicle)
    {
        return (int) Math.round(get(vehicle, "duration") * 20.0);
    }

    public static double decay(String vehicle)
    {
        return get(vehicle, "decay");
    }

    private static void load()
    {
        if (loaded)
        {
            return;
        }

        loaded = true;

        if (!Files.isRegularFile(FILE))
        {
            return;
        }

        try
        {
            for (String line : Files.readAllLines(FILE, StandardCharsets.UTF_8))
            {
                String trimmed = line.trim();

                if (trimmed.isEmpty() || trimmed.startsWith("#"))
                {
                    continue;
                }

                int equals = trimmed.indexOf('=');

                if (equals <= 0)
                {
                    continue;
                }

                String key = trimmed.substring(0, equals).trim();

                if (VALUES.containsKey(key))
                {
                    VALUES.put(key, Double.parseDouble(trimmed.substring(equals + 1).trim()));
                }
            }
        }
        catch (IOException | NumberFormatException brokenFile)
        {
            // A hand-edited file with a typo must not stop the server; the defaults stay.
            defaults();
        }
    }

    private static void save()
    {
        StringBuilder text = new StringBuilder("# carpet-lsc-addition boost values\n");

        VALUES.forEach((key, value) -> text.append(key).append('=').append(value).append('\n'));

        try
        {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, text.toString(), StandardCharsets.UTF_8);
        }
        catch (IOException ignored)
        {
            // Nothing useful to do if the folder is not writable; the values still apply in memory.
        }
    }
}
