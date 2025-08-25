package com.example.v.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.serialization.Serializable

@Serializable
data class VPNServer(
    val id: String,
    val name: String,
    val city: String,
    val country: String,
    val countryCode: String,
    val flag: String,
    val ip: String,
    val port: Int,
    val subnet: String,
    val serverIP: String,
    val dnsServers: List<String>,
    val wireguardPublicKey: String,
    val wireguardEndpoint: String,
    val allowedIPs: String,
    val mtu: Int = 1420,
    val isActive: Boolean = true,
    val priority: Int = 1,
    val createdAt: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(city)
        parcel.writeString(country)
        parcel.writeString(countryCode)
        parcel.writeString(flag)
        parcel.writeString(ip)
        parcel.writeInt(port)
        parcel.writeString(subnet)
        parcel.writeString(serverIP)
        parcel.writeStringList(dnsServers)
        parcel.writeString(wireguardPublicKey)
        parcel.writeString(wireguardEndpoint)
        parcel.writeString(allowedIPs)
        parcel.writeInt(mtu)
        parcel.writeByte(if (isActive) 1 else 0)
        parcel.writeInt(priority)
        parcel.writeString(createdAt)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<VPNServer> {
        override fun createFromParcel(parcel: Parcel): VPNServer {
            return VPNServer(parcel)
        }

        override fun newArray(size: Int): Array<VPNServer?> {
            return arrayOfNulls(size)
        }
    }
}

@Serializable
data class VPNClientConfig(
    val privateKey: String,
    val publicKey: String,
    val address: String,
    val dns: String = "1.1.1.1,8.8.8.8",
    val mtu: Int = 1420,
    val allowedIPs: String = "0.0.0.0/0,::/0"
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "1.1.1.1,8.8.8.8",
        parcel.readInt(),
        parcel.readString() ?: "0.0.0.0/0,::/0"
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(privateKey)
        parcel.writeString(publicKey)
        parcel.writeString(address)
        parcel.writeString(dns)
        parcel.writeInt(mtu)
        parcel.writeString(allowedIPs)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<VPNClientConfig> {
        override fun createFromParcel(parcel: Parcel): VPNClientConfig {
            return VPNClientConfig(parcel)
        }

        override fun newArray(size: Int): Array<VPNClientConfig?> {
            return arrayOfNulls(size)
        }
    }
}
