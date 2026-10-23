package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Firework rockets as a boost for boats, minecarts and happy ghasts.
 *
 * <p>Vanilla only lets a firework push an elytra flight; here the same rocket is spent to shove the
 * vehicle the player is riding. Both rules are a level rather than a switch, so how hard the shove is
 * stays a server decision.
 */
public final class VehicleBoost
{
    private VehicleBoost()
    {
    }

    /**
     * @return true when the rocket was spent on a boost, so the caller can report the interaction.
     */
    public static boolean tryBoost(ServerPlayer player, ItemStack stack)
    {
        Entity vehicle = player.getVehicle();

        if (vehicle == null)
        {
            return false;
        }

        if (vehicle instanceof HappyGhast ghast)
        {
            int level = level(CarpetLscSettings.HappyGhastBoost);
            return level > 0 && shove(ghast, dashDirection(ghast, player), level, player, stack);
        }

        if (vehicle instanceof AbstractBoat || vehicle instanceof AbstractMinecart)
        {
            int level = level(CarpetLscSettings.VehicleFireworkBoost);
            return level > 0 && shove(vehicle, player.getLookAngle(), level, player, stack);
        }

        return false;
    }

    /** The happy ghast dashes where it is already going, falling back to where the rider looks. */
    private static Vec3 dashDirection(HappyGhast ghast, ServerPlayer player)
    {
        Vec3 motion = ghast.getDeltaMovement();

        return motion.lengthSqr() > 1.0E-4 ? motion.normalize() : player.getLookAngle();
    }

    private static boolean shove(Entity vehicle, Vec3 direction, int level, ServerPlayer player, ItemStack stack)
    {
        double power = 0.35 + 0.3 * level;

        vehicle.push(direction.x * power, Math.max(direction.y * power, 0.05), direction.z * power);

        stack.shrink(1);

        player.level().playSound(null, player.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private static int level(String setting)
    {
        return switch (setting)
        {
            case "1" -> 1;
            case "2" -> 2;
            case "3" -> 3;
            default -> 0;
        };
    }
}
