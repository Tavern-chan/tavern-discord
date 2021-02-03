package com.asm.tavern.discord.drinks

import com.asm.tavern.domain.model.drinks.ComradeService

class LocalComradeService implements ComradeService {
	private boolean comradeModeEnabled

	@Override
	void enable() {
		comradeModeEnabled = true
	}

	@Override
	void disable() {
		comradeModeEnabled = false
	}

	@Override
	boolean isEnabled() {
		comradeModeEnabled
	}

}
