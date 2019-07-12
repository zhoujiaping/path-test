package dbclient;

/**
 * 写在一个类中的好处是，代码移动的时候，不需要逐个修改package，只要改一次就行。
 */
public class Callback {
    /**
     * 0个入参，没有返回值
     */
    @FunctionalInterface
    public interface Callback00{
        public void apply() throws Exception;
    }

    /**
     * 0个入参，1个返回值
     */
    @FunctionalInterface
    public interface Callback01<T>{
        public T apply() throws Exception;
    }
    /**
     * 1个入参，没有返回值
     */
    @FunctionalInterface
    public interface Callback10<T>{
        public void apply(T t) throws Exception;
    }
    @FunctionalInterface
    public interface Callback11<R,T>{
        public R apply(T t) throws Exception;
    }
    /**
     * 2个入参，没有返回值
     */
    @FunctionalInterface
    public interface Callback20<T1,T2>{
        public void apply(T1 t1,T2 t2) throws Exception;
    }
    @FunctionalInterface
    public interface Callback21<R,T1,T2>{
        public R apply(T1 t1,T2 t2) throws Exception;
    }
    /**
     * 3个入参，没有返回值
     */
    @FunctionalInterface
    public interface Callback30<T1,T2,T3>{
        public void apply(T1 t1,T2 t2,T3 t3) throws Exception;
    }
    /**
     * 3个入参，1个返回值
     */
    @FunctionalInterface
    public interface Callback31<R,T1,T2,T3>{
        public R apply(T1 t1,T2 t2,T3 t3) throws Exception;
    }
    /**
     * 3个入参，1个返回值
     */
    @FunctionalInterface
    public interface Callback41<R,T1,T2,T3,T4>{
        public R apply(T1 t1,T2 t2,T3 t3,T4 t4) throws Exception;
    }

}
