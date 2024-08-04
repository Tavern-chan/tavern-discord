package com.tavern.domain.model.drinks;

import net.dv8tion.jda.api.entities.Member;

import java.util.List;

public interface DrinkService {
	List<MemberDrinkResult> drink(Member member, List<Member> members);

	List<MemberDrinkResult> popPop(List<Member> members);

	DrinkRepository getDrinkRepository();

	ComradeService getComradeService();

	QuestService getQuestService();
}
