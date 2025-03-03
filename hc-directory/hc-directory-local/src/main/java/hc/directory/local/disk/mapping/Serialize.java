package hc.directory.local.disk.mapping;

public interface Serialize
{

    byte[] toBytes();

    int len();

    void toBytes(byte[] array, int offset);

}
