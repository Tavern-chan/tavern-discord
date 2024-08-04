package com.tavern.drinks;

import com.tavern.domain.model.drinks.MemberDrinkResult;
import com.tavern.domain.model.drinks.Quest;
import com.tavern.domain.model.drinks.QuestService;

import java.util.List;

public class LocalQuestService implements QuestService {
	@Override
	public Quest getCurrentQuest() {
		return new Quest() {
			@Override
			public String getName() {
				return "REEE";
			}

			@Override
			public boolean isComplete() {
				return false;
			}

		};
	}

	@Override
	public void contribute(MemberDrinkResult contribution) {
		// TODO
	}

	@Override
	public void contribute(List<MemberDrinkResult> contributions) {
		// TODO
	}

}
