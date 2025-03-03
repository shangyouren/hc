package hc.directory.local.disk.mapping;

public interface Deserialize<H extends SerializeHeader, S extends Serialize>
{

    S create(byte[] data, int offset);

    H newHeaderInstance(byte[] data, int offset);

    H createHeader(byte[] data, int offset, H h);

}
