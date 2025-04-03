package hc.rpc.netty.encoder;

import com.alibaba.fastjson2.JSON;
import hc.rpc.pojo.RpcPackage;
import hc.rpc.service.RpcObjectMapped;
import hc.utils.convert.DataTransformUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class NetworkPackageEncoder<T> extends MessageToByteEncoder<RpcPackage>
{

    public NetworkPackageEncoder(){
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcPackage rpcPackage, ByteBuf byteBuf) throws Exception
    {
        byte[] classNameBytes = rpcPackage.getClassName().getBytes(StandardCharsets.UTF_8);
        RpcObjectMapped mapped = RpcObjectMapped.find(rpcPackage.getClassName());
        byte[] valueBytes = null;
        int valueLen;
        if (mapped == null){
            valueBytes = JSON.toJSONString(rpcPackage.getValue()).getBytes(StandardCharsets.UTF_8);
            valueLen = valueBytes.length;
        }else {
            valueLen = mapped.len(rpcPackage.getValue());
        }
        byteBuf.writeInt(RpcPackage.HEAD_LEN + classNameBytes.length + valueLen + DataTransformUtil.INT_SIZE);
        byteBuf.writeLong(rpcPackage.getId());
        byteBuf.writeByte(rpcPackage.getCode());
        byteBuf.writeByte(rpcPackage.getRequest());
        byteBuf.writeInt(classNameBytes.length);
        byteBuf.writeBytes(classNameBytes);
        if (mapped != null)
        {
            mapped.write(rpcPackage.getValue(), byteBuf);
        }else {
            byteBuf.writeBytes(valueBytes);
        }
    }
}
