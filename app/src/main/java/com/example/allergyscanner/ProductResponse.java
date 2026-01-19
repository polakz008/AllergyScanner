package com.example.allergyscanner;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProductResponse {
    public int status; // 1 = znaleziono, 0 = brak
    public Product product;

    public static class Product {
        @SerializedName("product_name")
        public String productName;

        @SerializedName("brands")
        public String brands;

        @SerializedName("allergens_tags")
        public List<String> allergensTags;

        @SerializedName("ingredients_text")
        public String ingredientsText;

        @SerializedName("ingredients_text_pl")
        public String ingredientsTextPl;

        @SerializedName("ingredients_text_en")
        public String ingredientsTextEn;

        @SerializedName("generic_name_pl")
        public String descriptionPl;

        @SerializedName("generic_name")
        public String description;

        // Metoda zwracająca najlepszy dostępny opis
        public String getFinalDescription() {
            if (descriptionPl != null && !descriptionPl.isEmpty())
                return descriptionPl;
            return (description != null && !description.isEmpty()) ? description : "";
        }

        // Metoda zwracająca najlepszy dostępny skład
        public String getFinalIngredients() {
            // Priorytet: polski > ogólny > angielski
            if (ingredientsTextPl != null && !ingredientsTextPl.isEmpty())
                return ingredientsTextPl;
            if (ingredientsText != null && !ingredientsText.isEmpty())
                return ingredientsText;
            if (ingredientsTextEn != null && !ingredientsTextEn.isEmpty())
                return ingredientsTextEn;
            return null;
        }
    }
}