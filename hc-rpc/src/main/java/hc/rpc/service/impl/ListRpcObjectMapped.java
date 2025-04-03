package hc.rpc.service.impl;

import com.alibaba.fastjson2.JSON;
import hc.rpc.service.RpcObjectMapped;
import hc.utils.convert.DataTransformUtil;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class ListRpcObjectMapped extends RpcObjectMapped<List>
{

    public ListRpcObjectMapped()
    {
        super(List.class);
    }

    private Object[] cache;

    @Override
    public void write(List list, ByteBuf buf)
    {
        buf.writeInt(list.size());
        int ic = 0;
        for (Object o : list)
        {
            String className = (o instanceof List) ? List.class.getCanonicalName() : o.getClass().getCanonicalName();
            byte[] classNameBytes = (byte[]) cache[ic];
            ic++;
            buf.writeInt(classNameBytes.length);
            buf.writeBytes(classNameBytes);
            RpcObjectMapped mapped = RpcObjectMapped.find(className);
            if (mapped != null)
            {
                buf.writeInt(mapped.len(o));
                mapped.write(o, buf);
                ic++;
            }else {
                byte[] valueBytes = (byte[]) cache[ic];
                ic++;
                buf.writeInt(valueBytes.length);
                buf.writeBytes(valueBytes);
            }
        }
    }

    @Override
    public List read(ByteBuf buf, int len)
    {
        int listSize = buf.readInt();
        List v = new LinkedList();
        for (int i = 0; i < listSize; i++){
            int classByteLen = buf.readInt();
            byte[] classBytes = new byte[classByteLen];
            buf.readBytes(classBytes);
            int valueLen = buf.readInt();
            String className = new String(classBytes, StandardCharsets.UTF_8);
            RpcObjectMapped mapped = RpcObjectMapped.find(className);
            if (mapped != null){
                Object read = mapped.read(buf, valueLen);
                v.add(read);
            }else {
                byte[] dst = new byte[valueLen];
                buf.readBytes(dst);
                try
                {
                    Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
                    Object o = JSON.parseObject(new String(dst, StandardCharsets.UTF_8), clazz);
                    v.add(o);
                }
                catch (ClassNotFoundException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
        return v;
    }

    @Override
    public int len(List list)
    {
        this.cache = new Object[list.size() * 2];
        int ic = 0;
        int len = 0;
        len += DataTransformUtil.INT_SIZE;
        for (Object o : list)
        {
            String className = (o instanceof List) ? List.class.getCanonicalName() : o.getClass().getCanonicalName();
            byte[] classNameBytes = className.getBytes(StandardCharsets.UTF_8);
            len += DataTransformUtil.INT_SIZE;
            len += classNameBytes.length;
            cache[ic] = classNameBytes;
            ic++;
            RpcObjectMapped mapped = RpcObjectMapped.find(className);
            if (mapped != null)
            {
                len += DataTransformUtil.INT_SIZE;
                len += mapped.len(o);
                ic++;
            }else {
                byte[] valueBytes = JSON.toJSONString(o).getBytes(StandardCharsets.UTF_8);
                len += DataTransformUtil.INT_SIZE;
                len += valueBytes.length;
                cache[ic] = valueBytes;
                ic++;
            }
        }
        return len;
    }
}
