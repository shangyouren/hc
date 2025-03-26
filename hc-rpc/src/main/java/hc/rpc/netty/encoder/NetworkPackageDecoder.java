package hc.rpc.netty.encoder;

import com.alibaba.fastjson2.JSON;
import hc.rpc.pojo.RpcPackage;
import hc.rpc.service.RpcObjectMapped;
import hc.utils.convert.DataTransformUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class NetworkPackageDecoder<T> extends ByteToMessageDecoder
{

    public NetworkPackageDecoder(){
    }

    @Override
    protected void decode(
            ChannelHandlerContext channelHandlerContext,
            ByteBuf byteBuf,
            List<Object> list
    ) throws ClassNotFoundException
    {
        int len = byteBuf.readInt();
        long id = byteBuf.readLong();
        byte code = byteBuf.readByte();
        byte request = byteBuf.readByte();
        int classNameLen = byteBuf.readInt();
        byte[] bytes = new byte[classNameLen];
        byteBuf.readBytes(bytes);
        String className = new String(bytes, StandardCharsets.UTF_8);
        int valueLen = len - classNameLen - RpcPackage.HEAD_LEN - DataTransformUtil.INT_SIZE;
        RpcObjectMapped<?> mapped = RpcObjectMapped.find(className);
        Object read;
        if (mapped != null){
            read = mapped.read(byteBuf, valueLen);
        }else {
            byte[] valueBytes = new byte[valueLen];
            byteBuf.readBytes(valueBytes);
            read = JSON.parseObject(new String(valueBytes, StandardCharsets.UTF_8), Thread.currentThread().getContextClassLoader().loadClass(className));
        }
        RpcPackage rpcPackage = new RpcPackage();
        rpcPackage.setCode(code);
        rpcPackage.setId(id);
        rpcPackage.setValue(read);
        rpcPackage.setRequest(request);
        list.add(rpcPackage);
    }
}
