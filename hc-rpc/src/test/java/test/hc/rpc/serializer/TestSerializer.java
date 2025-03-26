package test.hc.rpc.serializer;

import com.alibaba.fastjson2.JSON;
import hc.utils.serialize.base.ObjectSerializer;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class TestSerializer
{

    @Test
    public void run(){
        Test1 test1 = new Test1();
        long time = System.currentTimeMillis();

        for (int i = 0; i < 100; i++)
        {
            String s = JSON.toJSONString(test1);
            Test1 test11 = JSON.parseObject(s, Test1.class);
            if (i % 10000 == 0){
                long l = System.currentTimeMillis();
                System.out.println("json " + i + " " + (l - time));
                time = l;
            }
        }

        ObjectSerializer objectSerializer = new ObjectSerializer();

        time = System.currentTimeMillis();
        for (int i = 0; i < 100; i++)
        {
            byte[] bytes = objectSerializer.realSerializer(test1);
            Test1 o = (Test1) objectSerializer.realInstance(bytes, 0);
            if (i % 10000 == 0){
                long l = System.currentTimeMillis();
                System.out.println("hc " + i + " " + (l - time));
                time = l;
            }
            o.check(test1);
        }
    }

    @Data
    public static class Test2{

        private String name = UUID.randomUUID().toString();

        private byte code = 34;

        private Test1 test = new Test1();

        public void check(Test2 check){
            Assert.assertEquals(this.name, check.name);
            Assert.assertEquals(this.code, check.code);
            test.check(check.test);
        }

    }

//    @Data
//    public static class Test1{
//
//        private String name = UUID.randomUUID().toString();
//
//        private byte code = 65;
//
//        private byte[] bytes = "shangyoure".getBytes(StandardCharsets.UTF_8);
//
//        private Double value = new Random().nextDouble();
//
//        private int len = 8765;
//
//        private Long time = System.currentTimeMillis();
//
//        public void check(Test1 check){
//            Assert.assertEquals(this.name, check.name);
//            Assert.assertEquals(this.code, check.code);
//            Assert.assertArrayEquals(this.bytes, check.bytes);
//            Assert.assertEquals(this.value, check.value);
//            Assert.assertEquals(this.len, check.len);
//            Assert.assertEquals(this.time, check.time);
//        }
//
//    }

//    public static class Test2Mapped implements RpcObjectMapped<Test2>
//    {
//
//        @Override
//        public Class<Test2> cla()
//        {
//            return Test2.class;
//        }
//
//        @Override
//        public Test2 newInstance()
//        {
//            return new Test2();
//        }
//
//        @Override
//        public List<Object> fieldValues(Test2 value)
//        {
//            ArrayList<Object> objects = new ArrayList<>();
//            objects.add(value.name);
//            objects.add(value.code);
//            objects.add(value.test);
//            return objects;
//        }
//
//        @Override
//        public void setField(Test2 value, List<Object> values)
//        {
//            value.setName((String) values.get(0));
//            value.setCode((Byte) values.get(1));
//            value.setTest((Test1) values.get(2));
//        }
//    }
//
//    public static class Test1Mapped implements RpcObjectMapped<Test1>
//    {
//        @Override
//        public Class<Test1> cla()
//        {
//            return Test1.class;
//        }
//
//        @Override
//        public Test1 newInstance()
//        {
//            return new Test1();
//        }
//
//        @Override
//        public List<Object> fieldValues(Test1 value)
//        {
//            ArrayList<Object> objects = new ArrayList<>();
//            objects.add(value.name);
//            objects.add(value.code);
//            objects.add(value.bytes);
//            objects.add(value.value);
//            objects.add(value.len);
//            objects.add(value.time);
//            return objects;
//        }
//
//        @Override
//        public void setField(Test1 value, List<Object> values)
//        {
//            value.setName((String) values.get(0));
//            value.setCode((Byte) values.get(1));
//            value.setBytes((byte[]) values.get(2));
//            value.setValue((Double) values.get(3));
//            value.setLen((Integer) values.get(4));
//            value.setTime((Long) values.get(5));
//        }
//    }

}
