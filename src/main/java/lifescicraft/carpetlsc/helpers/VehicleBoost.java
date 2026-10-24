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
 * <p>Vanilla only lets a firework push an elytra flight. Here one rocket starts a timed burst: the
 * thrust is strongest on the first tick and fades to nothing, and its strength, length and fade are
 * read from {@link BoostConfig}. The burst follows the vehicle it was started on and ends early if
 * the player gets off.
 */
public final class VehicleBoost
{
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
        String kind = kindOf(vehicle);

        if (kind == null || !enabled(kind))
        {
            return false;
        }

        BURSTS.put(player.getUUID(),
                new Burst(vehicle.getId(), BoostConfig.durationTicks(kind), BoostConfig.acceleration(kind),
                        BoostConfig.decay(kind)));
        stack.shrink(1);

        player.level().playSound(null, player.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private static String kindOf(Entity vehicle)
    {
        if (vehicle instanceof HappyGhast)
        {
            return "ghast";
        }

        if (vehicle instanceof AbstractMinecart)
        {
            return "minecart";
        }

        if (vehicle instanceof AbstractBoat)
        {
            return "boat";
        }

        return null;
    }

    private static boolean enabled(String kind)
    {
        return "ghast".equals(kind)
                ? CarpetLscSettings.HappyGhastBoost
                : CarpetLscSettings.VehicleFireworkBoost;
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

        // 1.0 at the start, 0.0 at the end, raised to the configured exponent.
        double remaining = (double) burst.ticksLeft / burst.total;
        double power = burst.initial * Math.pow(remaining, burst.decay);

        if (power > 0.0)
        {
            Vec3 direction = direction(vehicle, burst, player);
            Vec3 motion = vehicle.getDeltaMovement();

            // Boats and ghasts recompute their own motion, so the impulse has to be written into
            // the velocity rather than added through push(), which they immediately overwrite.
            vehicle.setDeltaMovement(
                    motion.x + direction.x * power,
                    motion.y + Math.max(direction.y * power, 0.0),
                    motion.z + direction.z * power);
        }

        if (--burst.ticksLeft <= 0)
        {
            BURSTS.remove(player.getUUID());
        }
    }

    /**
     * Where the push goes: a minecart is pushed along the rails it is already running on, a happy
     * ghast where it is already flying, and a boat where its rider is looking.
     */
    private static Vec3 direction(Entity vehicle, Burst burst, ServerPlayer player)
    {
        if (vehicle instanceof AbstractMinecart || vehicle instanceof HappyGhast)
        {
            Vec3 motion = vehicle.getDeltaMovement();

            if (motion.lengthSqr() > 1.0E-4)
            {
                burst.direction = motion.normalize();
                return burst.direction;
            }

            // A hovering ghast has no real motion yet, so the first pushes follow the rider.
            if (burst.direction == Vec3.ZERO || vehicle instanceof HappyGhast)
            {
                burst.direction = player.getLookAngle();
            }

            return burst.direction;
        }

        burst.direction = player.getLookAngle();
        return burst.direction;
    }

    private static final class Burst
    {
        private final int vehicleId;
        private final int total;
        private final double initial;
        private final double decay;
        private int ticksLeft;
        private Vec3 direction = Vec3.ZERO;

        private Burst(int vehicleId, int total, double initial, double decay)
        {
            this.vehicleId = vehicleId;
            this.total = Math.max(1, total);
            this.initial = initial;
            this.decay = decay;
            this.ticksLeft = this.total;
        }
    }
}
