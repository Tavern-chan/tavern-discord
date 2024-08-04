package com.tavern.domain.model.drinks;

import net.dv8tion.jda.api.entities.Member;

public class MemberDrinkResult {
	private final Member member;
	private final int drinks;
	private final boolean shot;

	public MemberDrinkResult(Member member, int drinks, boolean shot) {
		this.member = member;
		this.drinks = drinks;
		this.shot = shot;
	}

	public static MemberDrinkResult shot(Member member) {
		return new MemberDrinkResult(member, 0, true);
	}

	public static MemberDrinkResult drinks(Member member, int drinks) {
		return new MemberDrinkResult(member, drinks, false);
	}

	public final Member getMember() {
		return member;
	}

	public final int getDrinks() {
		return drinks;
	}

	public final boolean getShot() {
		return shot;
	}

	public final boolean isShot() {
		return shot;
	}

}
