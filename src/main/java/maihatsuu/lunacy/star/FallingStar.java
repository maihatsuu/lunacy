package maihatsuu.lunacy.star;

import net.minecraft.world.phys.Vec3;

/** A shooting star in flight: a straight line from high in the sky to its landing spot. */
final class FallingStar {
	static final int FALL_TICKS = 40;

	final Vec3 start;
	final Vec3 end;
	int age;

	FallingStar(Vec3 start, Vec3 end) {
		this.start = start;
		this.end = end;
	}

	Vec3 at(double progress) {
		return start.lerp(end, progress);
	}

	boolean landed() {
		return age >= FALL_TICKS;
	}
}
