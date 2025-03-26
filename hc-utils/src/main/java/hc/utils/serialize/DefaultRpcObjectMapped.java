package hc.utils.serialize;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class DefaultRpcObjectMapped<T> implements RpcObjectMapped<T>
{

    private final Class<T> clazz;

    private final List<Method> getMethod;

    private final List<Method> setMethod;

    public DefaultRpcObjectMapped(String name) throws ClassNotFoundException, NoSuchMethodException
    {
        this.clazz = (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(name);
        Field[] declaredFields = this.clazz.getDeclaredFields();
        this.getMethod = new ArrayList<>();
        this.setMethod = new ArrayList<>();
        for (Field declaredField : declaredFields)
        {
            if (!Modifier.isStatic(declaredField.getModifiers())){
                String name1 = declaredField.getName().substring(0, 1).toUpperCase() + declaredField.getName().substring(1);
                Method getMethod = this.clazz.getMethod("get" + name1);
                Method setMethod = this.clazz.getMethod("set" + name1, getMethod.getReturnType());
                this.getMethod.add(getMethod);
                this.setMethod.add(setMethod);
            }
        }
    }

    @Override
    public Class<T> cla()
    {
        return clazz;
    }

    @Override
    public T newInstance()
    {
        try
        {
            return clazz.getConstructor().newInstance();
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Object> fieldValues(T value)
    {

        List<Object> val = new ArrayList<>();
        for (Method method : getMethod)
        {
            Object invoke;
            try
            {
                invoke = method.invoke(value);
            }
            catch (IllegalAccessException | InvocationTargetException e)
            {
                throw new RuntimeException(e);
            }
            val.add(invoke);
        }
        return val;
    }

    @Override
    public void setField(T value, List<Object> values)
    {
        int i = 0;
        for (Method method : this.setMethod)
        {
            try
            {
                method.invoke(value, values.get(i));
            }
            catch (IllegalAccessException | InvocationTargetException e)
            {
                throw new RuntimeException(e);
            }
            i++;
        }
    }
}
