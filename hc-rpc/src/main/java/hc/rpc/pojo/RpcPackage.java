package hc.rpc.pojo;

import hc.utils.convert.DataTransformUtil;
import lombok.Data;

import java.util.List;

@Data
public class RpcPackage
{

    public static final int HEAD_LEN = DataTransformUtil.BYTE_SIZE + DataTransformUtil.LONG_SIZE + DataTransformUtil.BYTE_SIZE;

    protected byte code;

    protected long id;

    protected byte request;

    protected String className;

    protected Object value;

    public void setValue(Object v){
        if (v instanceof List<?>){
            this.className = List.class.getCanonicalName();
        }else {
            this.className = v.getClass().getCanonicalName();
        }
        this.value = v;
    }

    public void setException(Throwable e){
        this.className = Throwable.class.getCanonicalName();
        this.value = e;
    }

    public static RpcPackage response(RpcPackage request){
        RpcPackage response = new RpcPackage();
        response.setId(request.getId());
        response.setRequest(EnumPackageType.RESPONSE.getType());
        response.setCode(EnumPackageCode.SUCCESS.getCode());
        return response;
    }

    public static RpcPackage exceptionResponse(RpcPackage request, Exception e){
        RpcPackage response = new RpcPackage();
        response.setId(request.getId());
        response.setRequest(EnumPackageType.RESPONSE.getType());
        response.setCode(EnumPackageCode.ERROR.getCode());
        response.setException(e);
        return response;
    }

    public static RpcPackage exceptionResponse(long requestId, Exception e){
        RpcPackage response = new RpcPackage();
        response.setId(requestId);
        response.setRequest(EnumPackageType.RESPONSE.getType());
        response.setCode(EnumPackageCode.ERROR.getCode());
        response.setException(e);
        return response;
    }
}
