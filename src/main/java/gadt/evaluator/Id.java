package gadt.evaluator;

import gadt.App;

/**
 * Authors:
 * Aggelos Biboudis (@biboudis)
 * Nick Palladinos (@NickPalladinos)
 */
public class Id<T> implements App<Id.t, T> {

    public final T value;

    public Id(T value) {
        this.value = value;
    }

    public static <A> Id<A> prj(App<Id.t, A> app) {
        return (Id<A>) app;
    }

    public static <A> App<Id.t, A> newA(A value) {
        return new Id<>(value);
    }

    static class t {
    }
}