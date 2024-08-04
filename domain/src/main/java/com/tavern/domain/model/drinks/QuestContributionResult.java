package com.tavern.domain.model.drinks;

public interface QuestContributionResult {
	/**
	 * Quest that was contributed to
	 *
	 * @return
	 */
	Quest quest();

	/**
	 * Total contributions this round of drinks
	 *
	 * @return
	 */
	int totalContributions();
}
