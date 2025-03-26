package hc.utils.serialize;

import java.util.List;

public interface RpcObjectMapped<T>
{

    Class<T> cla();

    T newInstance();

    List<Object> fieldValues(T value);

    void setField(T value, List<Object> values);

}
