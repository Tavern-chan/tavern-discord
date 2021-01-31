package com.asm.tavern.domain.model

import com.asm.tavern.domain.model.audio.AudioService
import com.asm.tavern.domain.model.comrade.ComradeService
import com.asm.tavern.domain.model.roll.RollService
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

class DomainRegistry implements ApplicationContextAware {
	private static ApplicationContext context

	static RollService rollService() {
		context.getBean(RollService.class)
	}

	static AudioService audioService() {
		context.getBean(AudioService.class)
	}

	static ComradeService comradeService() {
		context.getBean(ComradeService.class)
	}

	@Override
	synchronized void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		if (context == null) {
			context = applicationContext
		}
	}

}
