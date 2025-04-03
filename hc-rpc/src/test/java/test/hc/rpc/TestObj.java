package test.hc.rpc;

import lombok.Data;

import java.util.UUID;

@Data
public class TestObj{

    private final String id = UUID.randomUUID().toString();

    private final long time = System.currentTimeMillis();
}