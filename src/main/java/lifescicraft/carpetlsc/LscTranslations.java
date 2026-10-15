package lifescicraft.carpetlsc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

/** Loads the language file for a given language id from this mod's own assets. */
public final class LscTranslations
{
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    private LscTranslations()
    {
    }

    public static Map<String, String> get(String lang)
    {
        String path = "assets/" + CarpetLscAddition.MOD_ID + "/lang/" + lang + ".json";

        try (InputStream stream = LscTranslations.class.getClassLoader().getResourceAsStream(path))
        {
            if (stream == null)
            {
                return Collections.emptyMap();
            }

            Map<String, String> translations = GSON.fromJson(
                    new InputStreamReader(stream, StandardCharsets.UTF_8),
                    new TypeToken<Map<String, String>>() { }.getType());

            return translations == null ? Collections.emptyMap() : translations;
        }
        catch (Exception e)
        {
            return Collections.emptyMap();
        }
    }
}
