package com.example.project1

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class Detail(
    //name, price, address, rating
    val name: String,
    val price: String,
    val address: String,
    val rating: Double,

    val phone: String,
    val url: String,

    val lat: Double,
    val lon: Double
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readDouble()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(price)
        parcel.writeString(address)
        parcel.writeDouble(rating)
        parcel.writeString(phone)
        parcel.writeString(url)
        parcel.writeDouble(lat)
        parcel.writeDouble(lon)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Detail> {
        override fun createFromParcel(parcel: Parcel): Detail {
            return Detail(parcel)
        }

        override fun newArray(size: Int): Array<Detail?> {
            return arrayOfNulls(size)
        }
    }
}