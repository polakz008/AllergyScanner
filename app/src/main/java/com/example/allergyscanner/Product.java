package com.example.allergyscanner;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Product {

    @SerializedName("product_name")
    public String name;

    @SerializedName("allergens_tags")
    public List<String> allergens;
}
