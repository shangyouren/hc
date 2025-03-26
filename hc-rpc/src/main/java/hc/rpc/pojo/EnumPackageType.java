package hc.rpc.pojo;

public enum EnumPackageType
{

    /**
     * 枚举package的类型，仅有两种，request、response
     */
    REQUEST(1),
    RESPONSE(0);

    private final byte type;

    EnumPackageType(byte type){
        this.type = type;
    }

    EnumPackageType(int i)
    {
        this.type = (byte) i;
    }

    public byte getType()
    {
        return type;
    }
}
