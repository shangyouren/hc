package hc.directory.local.constants;


/**
 * @author lenovo
 */

public enum EnumIndexType
{

    //0为占位，1为直接索引，2为排序索引，3为二级索引，4位小文件节点索引（500M以内）
    DIRECT(1),

    SORT(2),

    SECONDARY_INDEX(3),

    FILE_NODES(4)
    ;

    private final byte type;

    EnumIndexType(byte type){
        this.type = type;
    }

    EnumIndexType(int i)
    {
        this.type = (byte) i;
    }

    public byte getType()
    {
        return type;
    }
}
