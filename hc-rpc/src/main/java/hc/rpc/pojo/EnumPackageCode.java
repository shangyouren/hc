package hc.rpc.pojo;

public enum EnumPackageCode
{

    /**
     * 枚举package的类型，仅有两种，request、response
     */
    ERROR(-1),
    SUCCESS(1);

    private final byte code;

    EnumPackageCode(byte code){
        this.code = code;
    }

    EnumPackageCode(int i)
    {
        this.code = (byte) i;
    }

    public byte getCode()
    {
        return code;
    }
}
