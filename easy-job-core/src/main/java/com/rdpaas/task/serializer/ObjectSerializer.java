package com.rdpaas.task.serializer;

/**
 * jdk序列化抽象接口
 * @author rongdi
 * @date 2019-03-12 19:09
 */
public interface ObjectSerializer<T> {

    byte[] serialize(T t);

    T deserialize(byte[] bytes);
}
