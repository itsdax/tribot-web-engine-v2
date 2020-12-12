package scripts.dax.common;

public interface Action<T> {
    boolean perform(T t);
}
