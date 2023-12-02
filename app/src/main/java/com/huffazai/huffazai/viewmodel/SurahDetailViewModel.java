package com.huffazai.huffazai.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.huffazai.huffazai.repository.SurahDetailRepo;
import com.huffazai.huffazai.response.SurahDetailResponse;

public class SurahDetailViewModel extends ViewModel {

    public SurahDetailRepo surahDetailRepo;

    public SurahDetailViewModel(){
        surahDetailRepo = new SurahDetailRepo();
    }

    public LiveData<SurahDetailResponse> getSurahDetail(String lan, int id){
        return surahDetailRepo.getSurahDetail(lan,id);
    }
}
