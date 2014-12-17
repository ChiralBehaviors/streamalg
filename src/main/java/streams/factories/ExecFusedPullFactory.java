package streams.factories;

import streams.algebras.FusedPullAlg;
import streams.higher.App;
import streams.higher.Pull;

import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Authors:
 *      Aggelos Biboudis (@biboudis)
 *      Nick Palladinos (@NickPalladinos)
 */
public class ExecFusedPullFactory extends ExecPullFactory implements FusedPullAlg {

    interface FusePull<T> extends Pull<T> {
        <S> Pull<S> Compose(Function<T, S> other);
    }

    //TODO: can pull up to PullFactory.
    class FusibleFilterPull<T> implements Pull<T> {

        private final Pull<T> source;
        private final Predicate<T> predicate;

        public FusibleFilterPull(Pull<T> source, Predicate<T> predicate) {
            this.source = source;
            this.predicate = predicate;
        }

        T next = null;

        @Override
        public boolean hasNext() {
            while (source.hasNext()) {
                T current = source.next();
                if (predicate.test(current)) {
                    next = current;
                    return true;
                }
            }
            return false;
        }

        @Override
        public T next() {
            if (next != null || this.hasNext()) {
                T temp = this.next;
                this.next = null;
                return temp;
            }
            throw new NoSuchElementException();
        }
    }

    class FusibleMapPull<T, R> implements FusePull<R> {

        private final Pull<T> source;
        private final Function<T, R> mapper;
        private R next = null;

        public FusibleMapPull(Pull<T> source, Function<T, R> mapper) {
            this.source = source;
            this.mapper = mapper;
        }

        public Pull<T> getSource() {
            return source;
        }

        @Override
        public <S> FusePull<S> Compose(Function<R, S> other) {
            return new FusibleMapPull<T, S>(source, x -> other.apply(mapper.apply(x)));
        }

        @Override
        public boolean hasNext() {
            while (source.hasNext()) {
                T current = source.next();
                next = mapper.apply(current);
                return true;
            }

            return false;
        }

        @Override
        public R next() {
            if (next != null || this.hasNext()) {
                R temp = this.next;
                this.next = null;
                return temp;
            }
            throw new NoSuchElementException();
        }
    }

    @Override
    public <T> App<Pull.t, T> filter(Predicate<T> filter, App<Pull.t, T> app) {
        Pull<T> self = Pull.prj(app);

        if (self instanceof FusibleFilterPull) {
            FusibleFilterPull<T> previousSelf = (FusibleFilterPull<T>)self;
            return new FusibleFilterPull<T>(previousSelf.source, previousSelf.predicate.and(filter)) {};
        } else {
            return new FusibleFilterPull<T>(self, filter) {};
        }
    }

    @Override
    public <T, R> App<Pull.t, R> map(Function<T, R> mapper, App<Pull.t, T> app) {
        Pull<T> self = Pull.prj(app);

        if (self instanceof FusePull) {
            return ((FusePull<T>)self).Compose(mapper);
        } else {
            return new FusibleMapPull<>(self, mapper);
        }
    }
}
