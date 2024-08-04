package com.tavern.utilities;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class CollectionUtils {

    /**
     * Get the first element from a collection
     * @param collection The collection to get the first element from
     * @return The first element from the collection, empty() if there is no element
     * @param <T> The collection element type
     */
    public static <T> Optional<T> first(Collection<T> collection) {
        if (null == collection || collection.isEmpty()) {
            return Optional.empty();
        }

        return collection.stream().findFirst();
    }

    /**
     * Get the last element from a List
     * @param list The list to get the last element from
     * @return The last element from the collection, empty() if there are no elements
     * @param <T> The collection element type
     */
    public static <T> Optional<T> last(List<T> list) {
        if (null == list || list.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(list.get(list.size() - 1));
    }

    private CollectionUtils() {}
}
