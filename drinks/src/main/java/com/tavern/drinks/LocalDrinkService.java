package com.tavern.drinks;

import com.tavern.domain.model.discord.UserId;
import com.tavern.domain.model.drinks.*;
import net.dv8tion.jda.api.entities.Member;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LocalDrinkService implements DrinkService {
	private final DrinkRepository drinkRepository;
	private final QuestService questService;
	private final ComradeService comradeService;
	private final Random random = new Random(Instant.now().toEpochMilli());

	public LocalDrinkService(DrinkRepository drinkRepository, QuestService questService, ComradeService comradeService) {
		this.drinkRepository = drinkRepository;
		this.questService = questService;
		this.comradeService = comradeService;
	}

	@Override
	public DrinkRepository getDrinkRepository() {
		return drinkRepository;
	}

	@Override
	public ComradeService getComradeService() {
		return comradeService;
	}

	@Override
	public QuestService getQuestService() {
		return questService;
	}


	@Override
	public List<MemberDrinkResult> drink(Member member, List<Member> members) {
		float chanceOfShot = (float) 0.0005;
		int drinks = random.nextInt(5) + 1;
		boolean isShot = random.nextFloat() < chanceOfShot;
		Function<Member, MemberDrinkResult> drinkHandler = (Member m) -> isShot ? MemberDrinkResult.shot(m) : addDrinks(m, drinks);
		if (comradeService.isEnabled()) {
			return members.stream().map(drinkHandler).collect(Collectors.toList());
		} else {
			return new ArrayList<>(Collections.singletonList(drinkHandler.apply(member)));
		}
	}

	@Override
	public List<MemberDrinkResult> popPop(List<Member> members) {
		if (random.nextInt(100) < 5) {
			return members.stream()
				.map(MemberDrinkResult::shot)
				.collect(Collectors.toList());
		} else if (comradeService.isEnabled()) {
			int roll = members.stream()
				.map(__ -> random.nextInt(5))
				.reduce(0, (a, b) -> a > b ? a : b);
			return members.stream()
				.map(member -> addDrinks(member, roll))
				.collect(Collectors.toList());
		} else {
			return members.stream()
				.map(member -> addDrinks(member, random.nextInt(5)))
				.collect(Collectors.toList());
		}
	}

	private MemberDrinkResult addDrinks(Member member, int drinks) {
		getDrinkRepository().addDrinks(new UserId(member.getId()), drinks);
		return MemberDrinkResult.drinks(member, drinks);
	}

}
