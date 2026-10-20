package lifescicraft.carpetlsc.mixins;

import carpet.utils.Translations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Removes the doubled colon from the header of a filtered {@code /carpet} listing.
 *
 * <p>Carpet's own {@code zh_cn} / {@code zh_tw} translations for
 * {@code carpet.settings.command.mod_settings_matching} already end with a full-width colon
 * ({@code …的选项：}), while the command renderer appends a colon of its own. Every other
 * language leaves that spot blank, so only Chinese shows {@code 选项：:}. Verified against
 * carpet 26.3's own {@code assets/carpet/lang/*.json}.
 *
 * <p>It cannot be fixed from {@link carpet.CarpetExtension#canHasTranslations}:
 * {@code Translations.updateLanguage()} merges extension entries — and its own fallbacks — with
 * {@code Map.putIfAbsent}, so an extension can only fill in keys Carpet does not already have.
 * Correcting the lookup is the remaining option.
 *
 * <p>The handler is defensive: {@code require = 0} means an unexpected Carpet refactor turns this
 * into a no-op instead of a crash, and only the exact offending key is touched. {@link Pseudo}
 * lets the mixin be skipped entirely when the class is installed on a client without Carpet.
 */
@Pseudo
@Mixin(value = Translations.class, remap = false)
public class CarpetTranslationColonFixMixin
{
    @Inject(method = "tr(Ljava/lang/String;)Ljava/lang/String;", at = @At("RETURN"), cancellable = true, require = 0)
    private static void carpetLsc$stripDuplicateColon(String key, CallbackInfoReturnable<String> cir)
    {
        if (!"carpet.settings.command.mod_settings_matching".equals(key))
        {
            return;
        }

        String value = cir.getReturnValue();

        // U+FF1A FULLWIDTH COLON — only present in the Chinese translations.
        if (value.endsWith("\uFF1A"))
        {
            cir.setReturnValue(value.substring(0, value.length() - 1));
        }
    }
}
