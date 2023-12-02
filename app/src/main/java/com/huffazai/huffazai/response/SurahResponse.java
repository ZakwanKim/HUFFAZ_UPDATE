package com.huffazai.huffazai.response;

import com.google.gson.annotations.SerializedName;
import com.huffazai.huffazai.model.Surah;

import java.util.List;

public class SurahResponse {

    @SerializedName("data")
    private List<Surah> list;

    public List<Surah> getList() {
        return list;
    }

    public void setList(List<Surah> list) {
        this.list = list;
    }
}
