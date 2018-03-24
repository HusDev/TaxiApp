package com.dapp.husbs.taxtax.Remote;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by sat on 23/03/2018.
 */

public class RetrofitClient {
    private static Retrofit retrofit = null ;
    public static Retrofit getRetrofit(String baseURL){
        if (retrofit == null){
            retrofit  = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build() ;

        }
        return retrofit ;
    }
}
