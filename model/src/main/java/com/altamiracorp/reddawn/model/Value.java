package com.altamiracorp.reddawn.model;

public class Value {
    private final byte[] value;

    public Value(Object value) {
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
        byte[] b = new byte[8];
        for (int i = 0; i < 8; ++i) {
            b[i] = (byte) (value >> (8 - i - 1 << 3));
        }
        return b;
    }

    public byte[] toBytes() {
        return this.value;
    }

    public Long toLong() {
        long result = 0;
        for (int i = 0; i < this.value.length; i++) {
            result = (result << 8) + (this.value[i] & 0xff);
        }
        return result;
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
