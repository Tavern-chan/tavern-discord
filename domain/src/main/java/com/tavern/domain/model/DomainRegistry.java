package com.tavern.domain.model;

import com.tavern.domain.model.audio.AudioService;
import com.tavern.domain.model.audio.SongService;
import com.tavern.domain.model.drinks.DrinkService;
import com.tavern.domain.model.roll.RollService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DomainRegistry implements ApplicationContextAware {
	private static ApplicationContext context;

	public static RollService rollService() {
		return context.getBean(RollService.class);
	}

	public static AudioService audioService() {
		return context.getBean(AudioService.class);
	}

	public static DrinkService drinkService() {
		return context.getBean(DrinkService.class);
	}

	public static SongService songService() {
		return context.getBean(SongService.class);
	}

	@Override
	public synchronized void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (context == null) {
			context = applicationContext;
		}
	}

}
