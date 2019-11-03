package com.yyy.facedemo.net;

import com.alibaba.fastjson.JSON;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import okio.BufferedSource;
import okio.Okio;
import java.lang.reflect.Type;
import retrofit2.Response;

public class FastJsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final Type type;

    public FastJsonResponseBodyConverter(Type type) {
        this.type = type;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        BufferedSource bufferedSource = Okio.buffer(value.source());
        String tempStr = bufferedSource.readUtf8();
        bufferedSource.close();
        return JSON.parseObject(tempStr, type);
    }

}
