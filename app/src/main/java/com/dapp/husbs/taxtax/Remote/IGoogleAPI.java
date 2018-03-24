package com.dapp.husbs.taxtax.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by sat on 23/03/2018.
 */

public interface IGoogleAPI {
    @GET
    Call<String>getPath(@Url String url) ;


}
