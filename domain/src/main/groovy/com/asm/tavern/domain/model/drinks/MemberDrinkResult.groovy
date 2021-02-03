package com.asm.tavern.domain.model.drinks


import net.dv8tion.jda.api.entities.Member

class MemberDrinkResult {
	final Member member
	final int drinks
	final boolean shot

	MemberDrinkResult(Member member, int drinks, boolean shot) {
		this.member = member
		this.drinks = drinks
		this.shot = shot
	}

	static MemberDrinkResult shot(Member member) {
		new MemberDrinkResult(member, 0, true)
	}

	static MemberDrinkResult drinks(Member member, int drinks) {
		new MemberDrinkResult(member, drinks, false)
	}

}
