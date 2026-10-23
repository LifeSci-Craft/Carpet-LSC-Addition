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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Firework rockets as a sustained boost for boats, minecarts and happy ghasts.
 *
 * <p>Vanilla only lets a firework push an elytra flight. Here one rocket starts a five second burst:
 * the thrust is strongest in the first tick and fades to nothing, the same shape as an elytra
 * firework, and the level picks the starting thrust. The burst follows the vehicle it was started
 * on and ends early if the player gets off.
 */
public final class VehicleBoost
{
    /** Five seconds, in ticks. */
    private static final int BOOST_TICKS = 100;

    /** Per-tick starting thrust, indexed by level 1..3. */
    private static final double[] BOAT = {0.045, 0.065, 0.085};

    /** Lower than boats: a minecart already runs on rails and the push adds on top. */
    private static final double[] MINECART = {0.015, 0.022, 0.030};

    private static final double[] GHAST = {0.055, 0.080, 0.105};

    private static final Map<UUID, Burst> BURSTS = new HashMap<>();

    private VehicleBoost()
    {
    }

    /**
     * @return true when the rocket was spent starting a boost.
     */
    public static boolean tryBoost(ServerPlayer player, ItemStack stack)
    {
        Entity vehicle = player.getVehicle();

        if (vehicle == null)
        {
            return false;
        }

        double initial = startingThrust(vehicle);

        if (initial <= 0.0)
        {
            return false;
        }

        BURSTS.put(player.getUUID(), new Burst(vehicle.getId(), BOOST_TICKS, initial));
        stack.shrink(1);

        player.level().playSound(null, player.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private static double startingThrust(Entity vehicle)
    {
        if (vehicle instanceof HappyGhast)
        {
            return pick(GHAST, CarpetLscSettings.HappyGhastBoost);
        }

        if (vehicle instanceof AbstractMinecart)
        {
            return pick(MINECART, CarpetLscSettings.VehicleFireworkBoost);
        }

        if (vehicle instanceof AbstractBoat)
        {
            return pick(BOAT, CarpetLscSettings.VehicleFireworkBoost);
        }

        return 0.0;
    }

    private static double pick(double[] table, String setting)
    {
        int level = switch (setting)
        {
            case "1" -> 1;
            case "2" -> 2;
            case "3" -> 3;
            default -> 0;
        };

        return level == 0 ? 0.0 : table[level - 1];
    }

    /** Runs every tick for every player; does nothing unless that player has a burst going. */
    public static void tick(ServerPlayer player)
    {
        Burst burst = BURSTS.get(player.getUUID());

        if (burst == null)
        {
            return;
        }

        Entity vehicle = player.getVehicle();

        if (vehicle == null || vehicle.getId() != burst.vehicleId)
        {
            BURSTS.remove(player.getUUID());
            return;
        }

        // 1.0 at the start, 0.0 at the end, squared so it falls away quickly at first.
        double remaining = (double) burst.ticksLeft / burst.total;
        double power = burst.initial * remaining * remaining;

        if (power > 0.0)
        {
            Vec3 direction = direction(vehicle, player);
            vehicle.push(direction.x * power, Math.max(direction.y * power, 0.0), direction.z * power);
        }

        if (--burst.ticksLeft <= 0)
        {
            BURSTS.remove(player.getUUID());
        }
    }

    /** The ghast dashes where it is already going; riding vehicles follow the rider's aim. */
    private static Vec3 direction(Entity vehicle, ServerPlayer player)
    {
        if (vehicle instanceof HappyGhast)
        {
            Vec3 motion = vehicle.getDeltaMovement();

            if (motion.lengthSqr() > 1.0E-4)
            {
                return motion.normalize();
            }
        }

        return player.getLookAngle();
    }

    private static final class Burst
    {
        private final int vehicleId;
        private final int total;
        private final double initial;
        private int ticksLeft;

        private Burst(int vehicleId, int total, double initial)
        {
            this.vehicleId = vehicleId;
            this.total = total;
            this.initial = initial;
            this.ticksLeft = total;
        }
    }
}
