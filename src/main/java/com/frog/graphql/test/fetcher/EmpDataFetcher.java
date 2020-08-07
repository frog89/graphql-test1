package com.frog.graphql.test.fetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EmpDataFetcher {
	public final int BUFFER_SIZE = 1000;
	
	public static <T> List<T> getAsList(Consumer<Consumer<T>> consumerAction) {
		List<T> list = new ArrayList<T>();
		Consumer<T> consumer = item -> list.add(item);
		consumerAction.accept(consumer);
		return list;
	}
}
