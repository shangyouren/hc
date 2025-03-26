package hc.rpc.pojo;

import hc.utils.convert.DataTransformUtil;
import lombok.Data;

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
        this.value = v;
        this.className = v.getClass().getCanonicalName();
    }

    public void setException(Throwable e){
        this.className = Exception.class.getCanonicalName();
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
        response.setCode(EnumPackageCode.SUCCESS.getCode());
        response.setException(e);
        return response;
    }

}
