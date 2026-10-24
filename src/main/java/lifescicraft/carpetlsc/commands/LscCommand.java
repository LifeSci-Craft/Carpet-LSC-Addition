package lifescicraft.carpetlsc.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import lifescicraft.carpetlsc.helpers.BoostConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** {@code /lsc <vehicle> <value> <number>}: tunes the boost values at runtime. */
public final class LscCommand
{
    private static final SuggestionProvider<CommandSourceStack> VEHICLES =
            (context, builder) -> SharedSuggestionProvider.suggest(BoostConfig.VEHICLES, builder);

    private static final SuggestionProvider<CommandSourceStack> VARIABLES =
            (context, builder) -> SharedSuggestionProvider.suggest(BoostConfig.VARIABLES, builder);

    private LscCommand()
    {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal("lsc")
                
                .executes(context -> list(context.getSource()))
                .then(Commands.argument("vehicle", StringArgumentType.word())
                        .suggests(VEHICLES)
                        .then(Commands.argument("value", StringArgumentType.word())
                                .suggests(VARIABLES)
                                .then(Commands.argument("number", DoubleArgumentType.doubleArg())
                                        .executes(LscCommand::set)))));
    }

    private static int set(CommandContext<CommandSourceStack> context)
    {
        String vehicle = StringArgumentType.getString(context, "vehicle");
        String value = StringArgumentType.getString(context, "value");
        double number = DoubleArgumentType.getDouble(context, "number");

        if (!BoostConfig.set(vehicle, value, number))
        {
            context.getSource().sendFailure(Component.translatable("carpet-lsc-addition.lsc.invalid",
                    vehicle, value, number));
            return 0;
        }

        context.getSource().sendSuccess(() -> Component.translatable("carpet-lsc-addition.lsc.set",
                vehicle, value, number), false);
        return 1;
    }

    private static int list(CommandSourceStack source)
    {
        for (String vehicle : BoostConfig.VEHICLES)
        {
            source.sendSuccess(() -> Component.literal(vehicle
                    + ": acceleration=" + BoostConfig.acceleration(vehicle)
                    + " duration=" + BoostConfig.get(vehicle, "duration")
                    + " decay=" + BoostConfig.decay(vehicle)), false);
        }

        return 1;
    }
}
