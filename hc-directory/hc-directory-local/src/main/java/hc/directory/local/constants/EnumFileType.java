package hc.directory.local.constants;


/**
 * @author lenovo
 */

public enum EnumFileType
{

    // 1为文件夹，2为文件，3为软链接 4为非叶子节点
    DIRECTORY(1),

    FILE(2),

    LINK(3),

    NON_LEAF(4);
    ;

    private final byte type;

    EnumFileType(byte type){
        this.type = type;
    }

    EnumFileType(int i)
    {
        this.type = (byte) i;
    }

    public byte getType()
    {
        return type;
    }
}
