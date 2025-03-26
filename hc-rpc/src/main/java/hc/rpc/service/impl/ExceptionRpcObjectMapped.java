package hc.rpc.service.impl;

import hc.rpc.service.RpcObjectMapped;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class ExceptionRpcObjectMapped extends RpcObjectMapped<Throwable>
{

    public ExceptionRpcObjectMapped()
    {
        super(Throwable.class);
    }

    @Override
    public void write(Throwable exception, ByteBuf buf)
    {
        byte[] bytes = exception.toString().getBytes(StandardCharsets.UTF_8);
        buf.writeBytes(bytes);
    }

    @Override
    public Exception read(ByteBuf buf, int len)
    {
        byte[] bytes = new byte[len];
        buf.readBytes(bytes);
        return new Exception(new String(bytes, StandardCharsets.UTF_8));
    }

    @Override
    public int len(Throwable exception)
    {
        return exception.toString().getBytes(StandardCharsets.UTF_8).length;
    }
}
