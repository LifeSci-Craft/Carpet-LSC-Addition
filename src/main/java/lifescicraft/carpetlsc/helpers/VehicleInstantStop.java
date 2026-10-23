package lifescicraft.carpetlsc.helpers;

import lifescicraft.carpetlsc.CarpetLscSettings;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;

/**
 * Stops a boat or minecart the moment its rider gets off, instead of letting it coast.
 *
 * <p>{@code VehicleInstantStop} selects which vehicles are covered: everything, minecarts only or
 * boats only.
 */
public final class VehicleInstantStop
{
    private VehicleInstantStop()
    {
    }

    public static boolean applies(Entity vehicle)
    {
        boolean minecart = vehicle instanceof AbstractMinecart;
        boolean boat = vehicle instanceof AbstractBoat;

        return switch (CarpetLscSettings.VehicleInstantStop)
        {
            case "all" -> minecart || boat;
            case "minecart" -> minecart;
            case "boat" -> boat;
            default -> false;
        };
    }

    public static void stop(Entity vehicle)
    {
        // push goes through the public API, so the new (zero) velocity is synced to the clients.
        vehicle.push(vehicle.getDeltaMovement().scale(-1.0));
    }
}
