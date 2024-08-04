package com.tavern.domain.model.roll;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RollService {
	private static final Random RANDOM = new Random(Instant.now().getNano());

	public List<Integer> roll(int dice, int sides) throws IllegalArgumentException {
		if (dice <= 0) {
			throw new IllegalArgumentException("Dice must be 1 or greater");
		} else if (sides <= 0) {
			throw new IllegalArgumentException("Sides must be 1 or greater");
		}

		List<Integer> rolls = new ArrayList<Integer>();
		for (int i = 0; i < dice; i++) {
			rolls.add(RANDOM.nextInt(sides) + 1);
		}

		return rolls;
	}

	public int rollSingle(int sides) {
		return roll(1, sides).get(0);
	}

}
