package com.asm.tavern.discord.utilities.stream

import spock.lang.Specification

import java.util.stream.Collectors

class StreamChunkCollectorSpecification extends Specification {

	def "Test chunk items even"() {
		given:
		List<Integer> items = [1, 2, 3, 4]

		when:
		List<List<Integer>> output = items.stream()
				.collect(StreamChunkCollector.take(2))
				.collect(Collectors.toList())

		then:
		output.size() == 2
		output[0].size() == 2
		output[0][0] == 1
		output[0][1] == 2
		output[1].size() == 2
		output[1][0] == 3
		output[1][1] == 4
	}

	def "Test chunk items odd"() {
		given:
		List<Integer> items = [1, 2, 3, 4, 5]

		when:
		List<List<Integer>> output = items.stream()
				.collect(StreamChunkCollector.take(2))
				.collect(Collectors.toList())

		then:
		output.size() == 3
		output[0].size() == 2
		output[0][0] == 1
		output[0][1] == 2
		output[1].size() == 2
		output[1][0] == 3
		output[1][1] == 4
		output[2].size() == 1
		output[2][0] == 5
	}

}
