package com.example.v.models

import android.os.Parcel
import android.os.Parcelable

data class Server(
    val id: String,
    val name: String,
    val city: String,
    val country: String,
    val flag: String,
    val ip: String,
    val port: Int,
    val subnet: String,
    val serverIP: String,
    val dnsServers: List<String>,
    val latency: Int = 0,
    var isConnected: Boolean = false,
    // Essential fields only
    val countryCode: String = country.take(2).uppercase(),
    val ping: Int = latency,
    val load: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "", // id
        parcel.readString() ?: "", // name
        parcel.readString() ?: "", // city
        parcel.readString() ?: "", // country
        parcel.readString() ?: "", // flag
        parcel.readString() ?: "", // ip
        parcel.readInt(),          // port
        parcel.readString() ?: "", // subnet
        parcel.readString() ?: "", // serverIP
        parcel.createStringArrayList() ?: emptyList(), // dnsServers
        parcel.readInt(),          // latency
        parcel.readByte() != 0.toByte(), // isConnected
        parcel.readString() ?: "", // countryCode
        parcel.readInt(),          // ping
        parcel.readInt()           // load
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(city)
        parcel.writeString(country)
        parcel.writeString(flag)
        parcel.writeString(ip)
        parcel.writeInt(port)
        parcel.writeString(subnet)
        parcel.writeString(serverIP)
        parcel.writeStringList(dnsServers)
        parcel.writeInt(latency)
        parcel.writeByte(if (isConnected) 1 else 0)
        parcel.writeString(countryCode)
        parcel.writeInt(ping)
        parcel.writeInt(load)
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