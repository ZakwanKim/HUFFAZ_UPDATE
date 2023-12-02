package com.huffazai.huffazai.network;

import com.huffazai.huffazai.model.Surah;
import com.huffazai.huffazai.response.SurahDetailResponse;
import com.huffazai.huffazai.response.SurahResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface JsonPlaceHolderApi {

    @GET("surah")
    Call<SurahResponse> getSurah();

    @GET("sura/{language}/{id}")
    Call<SurahDetailResponse> getSurahDetail(@Path("language")String lan,
                                             @Path("id") int surahId);

}
