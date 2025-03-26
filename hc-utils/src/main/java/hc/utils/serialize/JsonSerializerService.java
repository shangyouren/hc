package hc.utils.serialize;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class JsonSerializerService implements SerializerService
{
    @Override
    public byte[] toBytes(Object o)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("className", o.getClass().getCanonicalName());
        map.put("value", o);
        return JSON.toJSONString(map).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Object instance(byte[] bytes)
    {
        JSONObject jsonObject = JSON.parseObject(bytes);
        String className = (String) jsonObject.get("className");
        try
        {
            Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass(className);
            return jsonObject.getObject("value", aClass);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }
}
