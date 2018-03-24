package com.dapp.husbs.taxtax.Common;

import com.dapp.husbs.taxtax.Remote.IGoogleAPI;
import com.dapp.husbs.taxtax.Remote.RetrofitClient;

/**
 * Created by sat on 23/03/2018.
 */

public class Common {
    public static final String baseURL = "https://maps.googleapis.com" ;
    public static IGoogleAPI getGoogleAPI(){
        return RetrofitClient.getRetrofit(baseURL).create(IGoogleAPI.class) ;

    }
}
