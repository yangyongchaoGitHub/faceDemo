package com.yyy.facedemo.net;

import com.yyy.facedemo.been.Tokens;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GetRequest_Interface {
//    @GET("market/?game=dota2#tab=selling&page_num=1&search=忘却之纪")
//    //Call<ResponseBody> getCall(@Path("user") String user);
//    Call<ResponseBody> getCall();
    @POST("token?grant_type=client_credentials")
    Call<Tokens> getToken(@Query("client_id") String client_id,
                          @Query("client_secret") String client_secret);
}
