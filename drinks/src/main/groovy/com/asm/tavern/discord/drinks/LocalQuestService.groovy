package com.asm.tavern.discord.drinks

import com.asm.tavern.domain.model.drinks.MemberDrinkResult
import com.asm.tavern.domain.model.drinks.Quest
import com.asm.tavern.domain.model.drinks.QuestService

class LocalQuestService implements QuestService {

	@Override
	Quest getCurrentQuest() {
		return new Quest() {
			@Override
			String getName() {
				"REEE"
			}

			@Override
			boolean isComplete() {
				false
			}
		}
	}

	@Override
	void contribute(MemberDrinkResult contribution) {
		// TODO
	}

	@Override
	void contribute(List<MemberDrinkResult> contributions) {
		// TODO
	}
}
