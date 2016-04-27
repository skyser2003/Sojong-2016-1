package routing.util;

/**
 * Created by frakafra on 2016. 4. 27..
 */
public class Pair<T1, T2> {
    public T1 first;
    public T2 second;

    public Pair(T1 f, T2 s) {
        first = f;
        second = s;
    }

    @Override
    public int hashCode() {
        return first.hashCode() * second.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ((Pair<T1, T2>)obj == null) {
            return false;
        }

        Pair other =(Pair<T1, T2>)obj;
        return (other.first.equals(first) && other.second.equals(second)) ||
                (other.first.equals(second) && other.second.equals(first));
    }
}
