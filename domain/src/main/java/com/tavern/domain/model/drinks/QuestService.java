package com.tavern.domain.model.drinks;

import java.util.List;

public interface QuestService {
	Quest getCurrentQuest();

	void contribute(MemberDrinkResult contribution);

	void contribute(List<MemberDrinkResult> contributions);
}
