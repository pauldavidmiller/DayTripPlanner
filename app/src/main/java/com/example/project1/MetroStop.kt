package com.example.project1

import java.io.Serializable

data class MetroStop (
    val lat: Double,
    val lon: Double,
    val stationCode: String
) : Serializable