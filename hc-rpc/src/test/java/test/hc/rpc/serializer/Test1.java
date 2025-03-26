package test.hc.rpc.serializer;

import lombok.Data;
import org.junit.Assert;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;

@Data
public class Test1
{
    private String name = UUID.randomUUID().toString();

    private byte code = 65;

    private byte[] bytes = "shangyoure".getBytes(StandardCharsets.UTF_8);

    private Double value = new Random().nextDouble();

    private int len = 8765;

    private Long time = System.currentTimeMillis();

    public void check(Test1 check){
        Assert.assertEquals(this.name, check.name);
        Assert.assertEquals(this.code, check.code);
        Assert.assertArrayEquals(this.bytes, check.bytes);
        Assert.assertEquals(this.value, check.value);
        Assert.assertEquals(this.len, check.len);
        Assert.assertEquals(this.time, check.time);
    }
}
