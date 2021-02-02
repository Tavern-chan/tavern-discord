package com.asm.tavern.discord.utilities.stream


import java.util.function.BiConsumer
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collector
import java.util.stream.Stream

/*
* @param <T> the type of input elements to the reduction operation
* @param <A> the mutable accumulation type of the reduction operation (often
*            hidden as an implementation detail)
* @param <R> the result type of the reduction operation
 */
class StreamChunkCollector<T> implements Collector<T, List<T>, Stream<List<T>>> {
	private final int chunkSize

	private StreamChunkCollector(int chunkSize) {
		this.chunkSize = chunkSize
	}

	@Override
	Supplier<List<T>> supplier() {
		LinkedList::new
	}

	@Override
	BiConsumer<List<T>, T> accumulator() {
		List::add
	}

	@Override
	BinaryOperator<List<T>> combiner() {
		(List<T> list1, List<T> list2) -> {
			List<T> combination = new LinkedList<>()
			combination.addAll(list1)
			combination.addAll(list2)
			combination
		}
	}

	@Override
	Function<List<T>, Stream<List<T>>> finisher() {
		(List<T> items) -> {
			List<List<T>> output = new LinkedList<>()
			output.add(new LinkedList<>())
			for (T item: items) {
				if (output.last().size() == chunkSize) {
					output.add(new LinkedList<>())
				}
				output.last().add(item)
			}
			output.stream()
		}
	}

	@Override
	Set<Characteristics> characteristics() {
		new HashSet<Characteristics>([
		        Characteristics.UNORDERED
		])
	}

	static <T> Collector<T, List<T>, Stream<List<T>>> take(int chunkSize) {
		new StreamChunkCollector<T>(chunkSize)
	}

}
