package com.example.v.wireguard

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * WireGuard protocol message types and structures
 * Based on the official WireGuard protocol specification
 */
object WireGuardMessages {
    
    // Message types
    const val MESSAGE_HANDSHAKE_INITIATION = 1.toByte()
    const val MESSAGE_HANDSHAKE_RESPONSE = 2.toByte()
    const val MESSAGE_COOKIE_REPLY = 3.toByte()
    const val MESSAGE_TRANSPORT_DATA = 4.toByte()
    
    // Message sizes
    const val MESSAGE_HANDSHAKE_INITIATION_SIZE = 148
    const val MESSAGE_HANDSHAKE_RESPONSE_SIZE = 92
    const val MESSAGE_COOKIE_REPLY_SIZE = 64
    const val MESSAGE_TRANSPORT_DATA_HEADER_SIZE = 16
    
    /**
     * WireGuard Handshake Initiation message
     */
    data class HandshakeInitiation(
        val messageType: Byte = MESSAGE_HANDSHAKE_INITIATION,
        val reserved: Int = 0,
        val senderIndex: Int,
        val unencryptedEphemeral: ByteArray, // 32 bytes
        val encryptedStatic: ByteArray,      // 48 bytes (32 + 16 tag)
        val encryptedTimestamp: ByteArray,   // 28 bytes (12 + 16 tag)
        val mac1: ByteArray,                 // 16 bytes
        val mac2: ByteArray                  // 16 bytes
    ) {
        fun toByteArray(): ByteArray {
            return ByteBuffer.allocate(MESSAGE_HANDSHAKE_INITIATION_SIZE).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                put(messageType)
                put(ByteArray(3)) // Reserved
                putInt(senderIndex)
                put(unencryptedEphemeral)
                put(encryptedStatic)
                put(encryptedTimestamp)
                put(mac1)
                put(mac2)
            }.array()
        }
        
        companion object {
            fun fromByteArray(data: ByteArray): HandshakeInitiation? {
                if (data.size != MESSAGE_HANDSHAKE_INITIATION_SIZE) return null
                
                val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
                val messageType = buffer.get()
                if (messageType != MESSAGE_HANDSHAKE_INITIATION) return null
                
                buffer.position(4) // Skip reserved
                val senderIndex = buffer.int
                
                val unencryptedEphemeral = ByteArray(32)
                buffer.get(unencryptedEphemeral)
                
                val encryptedStatic = ByteArray(48)
                buffer.get(encryptedStatic)
                
                val encryptedTimestamp = ByteArray(28)
                buffer.get(encryptedTimestamp)
                
                val mac1 = ByteArray(16)
                buffer.get(mac1)
                
                val mac2 = ByteArray(16)
                buffer.get(mac2)
                
                return HandshakeInitiation(
                    messageType, 0, senderIndex,
                    unencryptedEphemeral, encryptedStatic, encryptedTimestamp,
                    mac1, mac2
                )
            }
        }
    }
    
    /**
     * WireGuard Handshake Response message
     */
    data class HandshakeResponse(
        val messageType: Byte = MESSAGE_HANDSHAKE_RESPONSE,
        val reserved: Int = 0,
        val senderIndex: Int,
        val receiverIndex: Int,
        val unencryptedEphemeral: ByteArray, // 32 bytes
        val encryptedNothing: ByteArray,     // 16 bytes (0 + 16 tag)
        val mac1: ByteArray,                 // 16 bytes
        val mac2: ByteArray                  // 16 bytes
    ) {
        fun toByteArray(): ByteArray {
            return ByteBuffer.allocate(MESSAGE_HANDSHAKE_RESPONSE_SIZE).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                put(messageType)
                put(ByteArray(3)) // Reserved
                putInt(senderIndex)
                putInt(receiverIndex)
                put(unencryptedEphemeral)
                put(encryptedNothing)
                put(mac1)
                put(mac2)
            }.array()
        }
        
        companion object {
            fun fromByteArray(data: ByteArray): HandshakeResponse? {
                if (data.size != MESSAGE_HANDSHAKE_RESPONSE_SIZE) return null
                
                val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
                val messageType = buffer.get()
                if (messageType != MESSAGE_HANDSHAKE_RESPONSE) return null
                
                buffer.position(4) // Skip reserved
                val senderIndex = buffer.int
                val receiverIndex = buffer.int
                
                val unencryptedEphemeral = ByteArray(32)
                buffer.get(unencryptedEphemeral)
                
                val encryptedNothing = ByteArray(16)
                buffer.get(encryptedNothing)
                
                val mac1 = ByteArray(16)
                buffer.get(mac1)
                
                val mac2 = ByteArray(16)
                buffer.get(mac2)
                
                return HandshakeResponse(
                    messageType, 0, senderIndex, receiverIndex,
                    unencryptedEphemeral, encryptedNothing, mac1, mac2
                )
            }
        }
    }
    
    /**
     * WireGuard Transport Data message
     */
    data class TransportData(
        val messageType: Byte = MESSAGE_TRANSPORT_DATA,
        val reserved: Int = 0,
        val receiverIndex: Int,
        val counter: Long,
        val encryptedData: ByteArray // Variable length (data + 16 byte tag)
    ) {
        fun toByteArray(): ByteArray {
            return ByteBuffer.allocate(MESSAGE_TRANSPORT_DATA_HEADER_SIZE + encryptedData.size).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                put(messageType)
                put(ByteArray(3)) // Reserved
                putInt(receiverIndex)
                putLong(counter)
                put(encryptedData)
            }.array()
        }
        
        companion object {
            fun fromByteArray(data: ByteArray): TransportData? {
                if (data.size < MESSAGE_TRANSPORT_DATA_HEADER_SIZE) return null
                
                val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
                val messageType = buffer.get()
                if (messageType != MESSAGE_TRANSPORT_DATA) return null
                
                buffer.position(4) // Skip reserved
                val receiverIndex = buffer.int
                val counter = buffer.long
                
                val encryptedData = ByteArray(data.size - MESSAGE_TRANSPORT_DATA_HEADER_SIZE)
                buffer.get(encryptedData)
                
                return TransportData(
                    messageType, 0, receiverIndex, counter, encryptedData
                )
            }
        }
    }
    
    /**
     * WireGuard Cookie Reply message
     */
    data class CookieReply(
        val messageType: Byte = MESSAGE_COOKIE_REPLY,
        val reserved: Int = 0,
        val receiverIndex: Int,
        val nonce: ByteArray,           // 24 bytes
        val encryptedCookie: ByteArray  // 32 bytes (16 + 16 tag)
    ) {
        fun toByteArray(): ByteArray {
            return ByteBuffer.allocate(MESSAGE_COOKIE_REPLY_SIZE).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                put(messageType)
                put(ByteArray(3)) // Reserved
                putInt(receiverIndex)
                put(nonce)
                put(encryptedCookie)
            }.array()
        }
        
        companion object {
            fun fromByteArray(data: ByteArray): CookieReply? {
                if (data.size != MESSAGE_COOKIE_REPLY_SIZE) return null
                
                val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
                val messageType = buffer.get()
                if (messageType != MESSAGE_COOKIE_REPLY) return null
                
                buffer.position(4) // Skip reserved
                val receiverIndex = buffer.int
                
                val nonce = ByteArray(24)
                buffer.get(nonce)
                
                val encryptedCookie = ByteArray(32)
                buffer.get(encryptedCookie)
                
                return CookieReply(messageType, 0, receiverIndex, nonce, encryptedCookie)
            }
        }
    }
    
    /**
     * Parse any WireGuard message from byte array
     */
    fun parseMessage(data: ByteArray): Any? {
        if (data.isEmpty()) return null
        
        return when (val messageType = data[0]) {
            MESSAGE_HANDSHAKE_INITIATION -> HandshakeInitiation.fromByteArray(data)
            MESSAGE_HANDSHAKE_RESPONSE -> HandshakeResponse.fromByteArray(data)
            MESSAGE_COOKIE_REPLY -> CookieReply.fromByteArray(data)
            MESSAGE_TRANSPORT_DATA -> TransportData.fromByteArray(data)
            else -> {
                println("Unknown WireGuard message type: $messageType")
                null
            }
        }
    }
}
