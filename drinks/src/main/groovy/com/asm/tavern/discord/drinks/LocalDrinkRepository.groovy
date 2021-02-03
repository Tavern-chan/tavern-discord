package com.asm.tavern.discord.drinks

import com.asm.tavern.domain.model.discord.UserId
import com.asm.tavern.domain.model.drinks.DrinkRepository
import groovy.transform.Synchronized

class LocalDrinkRepository implements DrinkRepository {
	private final Map<UserId, Integer> playerDrinks = new HashMap<>()

	@Override
	@Synchronized
	int getDrinks(UserId id) {
		ensureExistence(id)
		playerDrinks.get(id)
	}

	@Override
	@Synchronized
	void addDrinks(UserId id, int drinks) {
		ensureExistence(id)
		playerDrinks.put(id, playerDrinks.get(id) + drinks)
	}

	private void ensureExistence(UserId id) {
		if (!playerDrinks.containsKey(id)) {
			playerDrinks.put(id, 0)
		}
	}

}
