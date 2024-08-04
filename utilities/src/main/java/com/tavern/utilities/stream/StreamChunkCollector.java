package com.tavern.utilities.stream;

import com.google.common.collect.Iterables;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class StreamChunkCollector<T> implements Collector<T, List<T>, Stream<List<T>>> {
	private final int chunkSize;

	private StreamChunkCollector(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	@Override
	public Supplier<List<T>> supplier() {
		return LinkedList::new;
	}

	@Override
	public BiConsumer<List<T>, T> accumulator() {
		return List::add;
	}

	@Override
	public BinaryOperator<List<T>> combiner() {
		return (list1, list2) -> {
			List<T> combination = new LinkedList<>();
			combination.addAll(list1);
			combination.addAll(list2);
			return combination;
		};
	}

	@Override
	public Function<List<T>, Stream<List<T>>> finisher() {
		return (List<T> items) -> {
			List<List<T>> output = new LinkedList<>();
			output.add(new LinkedList<>());
			for (T item: items) {
				if (Iterables.getLast(output).size() == chunkSize) {
					output.add(new LinkedList<>());
				}
				Iterables.getLast(output).add(item);
			}
			return output.stream();
		};
	}

	@Override
	public Set<Characteristics> characteristics() {
		return new HashSet<>(List.of(
            Characteristics.UNORDERED
        ));
	}

	public static <T> Collector<T, List<T>, Stream<List<T>>> take(int chunkSize) {
		return new StreamChunkCollector<>(chunkSize);
	}

}
