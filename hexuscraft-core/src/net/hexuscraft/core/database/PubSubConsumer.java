package net.hexuscraft.core.database;

import java.util.Objects;

@FunctionalInterface
interface TriConsumer<A, B, C> {

    void accept(A a, B b, C c);

    default TriConsumer<A, B, C> andThen(TriConsumer<? super A, ? super B, ? super C> after) {
        Objects.requireNonNull(after);

        return (a, b, c) -> {
            accept(a, b, c);
            after.accept(a, b, c);
        };
    }
}

@FunctionalInterface
public interface PubSubConsumer extends TriConsumer<String, String, String> {
    void accept(final String pattern, final String channelName, final String message);
}
