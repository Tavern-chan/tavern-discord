package com.asm.tavern.domain.model.drinks

interface QuestService {

	Quest getCurrentQuest()

	void contribute(MemberDrinkResult contribution) // TODO: Domain Event pub/sub to figure out when a Quest completes?

	void contribute(List<MemberDrinkResult> contributions)

}
