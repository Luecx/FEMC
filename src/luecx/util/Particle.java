package luecx.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import static java.lang.Math.abs;

public class Particle {

	public static final double PARTICLE_DISTANCE = 0.1;

	public static void drawBox(Location from, Location to, World w) {
		double h = to.getY() - from.getY();

		drawVerticalLine(from.clone(), h, w);
		drawVerticalLine(set(from, to.getX(), to.getZ()), h, w);
		drawVerticalLine(set(from, from.getX(), to.getZ()), h, w);
		drawVerticalLine(set(from, to.getX(), from.getZ()), h, w);

		double a = to.getX() - from.getX();
		double b = to.getZ() - from.getZ();

		drawHorizontalLine(from.clone(), a, b, w);
		drawHorizontalLine(to.clone().subtract(0, h, 0), -a, -b, w);
		drawHorizontalLine(from.clone().add(0, h, 0).clone(), a, b, w);
		drawHorizontalLine(to.clone(), -a, -b, w);
	}

	private static Location set(Location loc, double x, double z) {
		Location location = loc.clone();

		location.setX(x);
		location.setZ(z);

		return location;
	}

	private static void drawHorizontalLine(Location from, double wx, double wz, World p) {
		Location temp = from.clone();
		for (double i = 0; i < abs(wx); i += PARTICLE_DISTANCE) {
			temp.add((wx < 0 ? -1 : 1) * PARTICLE_DISTANCE, 0, 0);
			p.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, temp, 1);
		}

		temp = from.clone();
		for (double i = 0; i < abs(wz); i += PARTICLE_DISTANCE) {
			temp.add(0, 0, (wz < 0 ? -1 : 1) * PARTICLE_DISTANCE);
			p.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, temp, 1);
		}
	}

	private static void drawVerticalLine(Location from, double height, World p) {
		if (height < 0) {
			height *= -1;
			drawVerticalLine(from.subtract(0, height, 0), height, p);
			return;
		}

		for (double i = 0; i < height; i += PARTICLE_DISTANCE) {
			from.add(0, PARTICLE_DISTANCE, 0);
			p.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, from, 1);
		}
	}

}
