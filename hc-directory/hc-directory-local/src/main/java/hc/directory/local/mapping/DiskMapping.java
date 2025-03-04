package hc.directory.local.mapping;

public interface DiskMapping
{

    void force();

    void put(byte[] bytes);

    void position(int newPosition);

    void get(byte[] bytes);

}
