package com.example.v.models

import android.os.Parcel
import android.os.Parcelable

data class Server(
    val id: String,
    val name: String,
    val country: String,
    val countryCode: String,
    val city: String,
    val flag: String,
    val ping: Int,
    val load: Int,
    val isOptimal: Boolean = false,
    val isPremium: Boolean = false,
    var isFavorite: Boolean = false,
    val latitude: Double,
    val longitude: Double,
    val wireGuardConfig: WireGuardConfig? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "", // id
        parcel.readString() ?: "", // name
        parcel.readString() ?: "", // country
        parcel.readString() ?: "", // countryCode
        parcel.readString() ?: "", // city
        parcel.readString() ?: "", // flag
        parcel.readInt(),          // ping
        parcel.readInt(),          // load
        parcel.readByte() != 0.toByte(), // isOptimal
        parcel.readByte() != 0.toByte(), // isPremium
        parcel.readByte() != 0.toByte(), // isFavorite
        parcel.readDouble(),       // latitude
        parcel.readDouble(),       // longitude
        parcel.readParcelable(WireGuardConfig::class.java.classLoader, WireGuardConfig::class.java)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(country)
        parcel.writeString(countryCode)
        parcel.writeString(city)
        parcel.writeString(flag)
        parcel.writeInt(ping)
        parcel.writeInt(load)
        parcel.writeByte(if (isOptimal) 1 else 0)
        parcel.writeByte(if (isPremium) 1 else 0)
        parcel.writeByte(if (isFavorite) 1 else 0)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeParcelable(wireGuardConfig, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Server> {
        override fun createFromParcel(parcel: Parcel): Server {
            return Server(parcel)
        }

        override fun newArray(size: Int): Array<Server?> {
            return arrayOfNulls(size)
        }
    }
}

data class WireGuardConfig(
    val serverPublicKey: String,
    val serverEndpoint: String,
    val serverPort: Int = 51820,
    val allowedIPs: String = "0.0.0.0/0",
    val dns: String = "1.1.1.1, 8.8.8.8",
    val mtu: Int = 1420,
    val keepAlive: Int = 25,
    val presharedKey: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "0.0.0.0/0",
        parcel.readString() ?: "1.1.1.1, 8.8.8.8",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(serverPublicKey)
        parcel.writeString(serverEndpoint)
        parcel.writeInt(serverPort)
        parcel.writeString(allowedIPs)
        parcel.writeString(dns)
        parcel.writeInt(mtu)
        parcel.writeInt(keepAlive)
        parcel.writeString(presharedKey)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WireGuardConfig> {
        override fun createFromParcel(parcel: Parcel): WireGuardConfig {
            return WireGuardConfig(parcel)
        }

        override fun newArray(size: Int): Array<WireGuardConfig?> {
            return arrayOfNulls(size)
        }
    }
}

data class ClientConfig(
    val privateKey: String,
    val publicKey: String,
    val address: String,
    val dns: String = "1.1.1.1, 8.8.8.8"
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "1.1.1.1, 8.8.8.8"
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(privateKey)
        parcel.writeString(publicKey)
        parcel.writeString(address)
        parcel.writeString(dns)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ClientConfig> {
        override fun createFromParcel(parcel: Parcel): ClientConfig {
            return ClientConfig(parcel)
        }

        override fun newArray(size: Int): Array<ClientConfig?> {
            return arrayOfNulls(size)
        }
    }
}