package com.example.allergyscanner;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface OpenFoodFactsApi {

    @GET("api/v2/product/{barcode}.json")
    Call<ProductResponse> getProduct(@Path("barcode") String barcode);
}

