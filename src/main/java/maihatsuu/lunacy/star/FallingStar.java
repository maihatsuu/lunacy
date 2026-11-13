package maihatsuu.lunacy.star;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

/** A star drop still falling from the sky. Vanilla item physics move it; we draw the trail. */
final class FallingStar {
	final ItemEntity item;
	Vec3 lastPos;
	int age;

	FallingStar(ItemEntity item) {
		this.item = item;
		this.lastPos = item.position();
	}
}
