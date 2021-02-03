package com.asm.tavern.domain.model.drinks

import net.dv8tion.jda.api.entities.Member

interface DrinkService {

	/**
	 *
	 * @param member
	 * @param chatMembers in case comrade mode is on (should include person that issued the command)
	 * @return
	 */
	List<MemberDrinkResult> drink(Member member, List<Member> members)

	List<MemberDrinkResult> popPop(List<Member> members)

	DrinkRepository getDrinkRepository()

	ComradeService getComradeService()

	QuestService getQuestService()

}