package com.huffazai.huffazai.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.huffazai.huffazai.model.Surah;
import com.huffazai.huffazai.network.JsonPlaceHolderApi;
import com.huffazai.huffazai.network.Api;
import com.huffazai.huffazai.response.SurahResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SurahRepo {
    private JsonPlaceHolderApi jsonPlaceHolderApi;

    public SurahRepo() {
        jsonPlaceHolderApi = Api.getRetrofit().create(JsonPlaceHolderApi.class);
    }

    public LiveData<SurahResponse> getSurah() {
        MutableLiveData<SurahResponse> data = new MutableLiveData<>();
        jsonPlaceHolderApi.getSurah().enqueue(new Callback<SurahResponse>() {
            @Override
            public void onResponse(Call<SurahResponse> call, Response<SurahResponse> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    // Handle unsuccessful API response, e.g., log the error or show a message.
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<SurahResponse> call, Throwable t) {
                // Handle network or other errors, e.g., log the error or show a message.
                data.setValue(null);
            }
        });
        return data;
    }
}