package com.asm.tavern.discord.drinks

import com.asm.tavern.domain.model.discord.UserId
import com.asm.tavern.domain.model.drinks.*
import net.dv8tion.jda.api.entities.Member

import java.time.Instant
import java.util.function.Function
import java.util.stream.Collectors

class LocalDrinkService implements DrinkService {
	private final DrinkRepository drinkRepository
	private final QuestService questService
	private final ComradeService comradeService

	private final Random random = new Random(Instant.now().toEpochMilli())

	LocalDrinkService(DrinkRepository drinkRepository, QuestService questService, ComradeService comradeService) {
		this.drinkRepository = drinkRepository
		this.questService = questService
		this.comradeService = comradeService
	}

	@Override
	List<MemberDrinkResult> drink(Member member, List<Member> members) {
		float chanceOfShot = 0.0005
		int drinks = random.nextInt(5) + 1
		boolean isShot = random.nextFloat() < chanceOfShot
		Function<Member, MemberDrinkResult> drinkHandler = (Member m) -> isShot ? MemberDrinkResult.shot(m) : addDrinks(m, drinks)
		if (comradeService.enabled) {
			return members.stream()
					.map(drinkHandler)
					.collect(Collectors.toList())
		} else {
			return [drinkHandler.apply(member)]
		}
	}

	@Override
	List<MemberDrinkResult> popPop(List<Member> members) {
		if (random.nextInt(100) < 5) {
			return members.stream().map({ member -> MemberDrinkResult.shot(member)}).collect(Collectors.toList())
		} else if (comradeService.enabled) {
			int roll = members.stream().map(_ -> random.nextInt(5)).reduce(0, (a, b) -> a > b ? a : b)
			return members.stream()
					.map({member -> addDrinks(member, roll)})
					.collect(Collectors.toList())
		} else {
			return members.stream()
					.map({member -> addDrinks(member, random.nextInt(5))})
					.collect(Collectors.toList())
		}
	}

	private MemberDrinkResult addDrinks(Member member, int drinks) {
		getDrinkRepository().addDrinks(new UserId(member.id), drinks)
		MemberDrinkResult.drinks(member, drinks)
	}

	@Override
	DrinkRepository getDrinkRepository() {
		drinkRepository
	}

	@Override
	ComradeService getComradeService() {
		comradeService
	}

	@Override
	QuestService getQuestService() {
		questService
	}

}
