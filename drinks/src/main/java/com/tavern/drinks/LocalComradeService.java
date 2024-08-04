package com.tavern.drinks;

import com.tavern.domain.model.drinks.ComradeService;

public class LocalComradeService implements ComradeService {
	private boolean comradeModeEnabled;

	@Override
	public void enable() {
		comradeModeEnabled = true;
	}

	@Override
	public void disable() {
		comradeModeEnabled = false;
	}

	@Override
	public boolean isEnabled() {
		return comradeModeEnabled;
	}
}
