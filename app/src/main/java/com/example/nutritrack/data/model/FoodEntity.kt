package com.example.nutritrack.data.model

data class Food(
    val fdcId: Int,
    val dataType: String,
    val description: String,
    val foodCode: String,
    val foodNutrients: List<FoodNutrient>,
    val publicationDate: String,
    val scientificName: String?,
    val brandOwner: String?,
    val gtinUpc: String?,
    val ingredients: String?,
    val ndbNumber: String?,
    val additionalDescriptions: String?,
    val allHighlightFields: String?,
    val score: Double,
    val packageWeight: String?,
    val servingSize: String?,
    val servingSizeUnit: String?,
    val householdServingFullText: String?,
)

data class FoodNutrient(
    val nutrientId: Int,
    val nutrientName: String?,
    val nutrientNumber: String?,
    val unitName: String,
    val derivationCode: String?,
    val derivationDescription: String,
    val value: Double,
)