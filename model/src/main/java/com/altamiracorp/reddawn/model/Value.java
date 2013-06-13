package com.altamiracorp.reddawn.model;

import org.apache.commons.codec.binary.Hex;

import java.nio.ByteBuffer;

public class Value {
    private final byte[] value;

    public Value(Object value) {
        if (value == null) {
            throw new NullPointerException("Value cannot be null");
        }
        this.value = toBytes(value);
    }

    private byte[] toBytes(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return stringToBytes((String) value);
        }

        if (value instanceof Long) {
            return longToBytes((Long) value);
        }

        if (value instanceof byte[]) {
            return (byte[]) value;
        }

        throw new RuntimeException("Unhandled type to convert: " + value.getClass().getName());
    }

    private byte[] stringToBytes(String value) {
        return value.getBytes();
    }

    private byte[] longToBytes(Long value) {
        return ByteBuffer.allocate(8).putLong(value).array();
    }

    public byte[] toBytes() {
        return this.value;
    }

    public Long toLong() {
        return ByteBuffer.wrap(this.value).getLong();
    }

    @Override
    public String toString() {
        return new String(this.value);
    }

    public static byte[] toBytes(Value value) {
        if (value == null) {
            return null;
        }
        return value.toBytes();
    }

    public static String toString(Value value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static Long toLong(Value value) {
        if (value == null) {
            return null;
        }
        return value.toLong();
    }
}
