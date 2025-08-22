package com.myapp.backend.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

// Ad Blocker Models
@Serializable
data class AdBlockingConfig(
    val userId: String,
    val enabled: Boolean = false,
    val filterLists: List<String> = emptyList(),
    val customRules: List<String> = emptyList(),
    val whitelist: List<String> = emptyList(),
    val lastUpdated: LocalDateTime = LocalDateTime.now(),
    val stats: AdBlockingStats = AdBlockingStats()
)

@Serializable
data class AdBlockingStats(
    val totalBlocked: Long = 0,
    val adsBlocked: Long = 0,
    val trackersBlocked: Long = 0,
    val lastReset: LocalDateTime = LocalDateTime.now()
)

@Serializable
data class AdBlockingRequest(
    val enabled: Boolean,
    val filterLists: List<String>? = null,
    val customRules: List<String>? = null,
    val whitelist: List<String>? = null
)

@Serializable
data class AdBlockingResponse(
    val success: Boolean,
    val message: String,
    val config: AdBlockingConfig? = null
)

// Malware Protection Models
@Serializable
data class MalwareProtectionConfig(
    val userId: String,
    val enabled: Boolean = false,
    val realTimeScanning: Boolean = true,
    val urlFiltering: Boolean = true,
    val fileScanning: Boolean = false,
    val threatDatabase: String = "default",
    val lastUpdated: LocalDateTime = LocalDateTime.now(),
    val stats: MalwareProtectionStats = MalwareProtectionStats()
)

@Serializable
data class MalwareProtectionStats(
    val threatsBlocked: Long = 0,
    val maliciousUrls: Long = 0,
    val suspiciousFiles: Long = 0,
    val lastThreat: LocalDateTime? = null,
    val lastReset: LocalDateTime = LocalDateTime.now()
)

@Serializable
data class MalwareProtectionRequest(
    val enabled: Boolean,
    val realTimeScanning: Boolean? = null,
    val urlFiltering: Boolean? = null,
    val fileScanning: Boolean? = null
)

@Serializable
data class MalwareProtectionResponse(
    val success: Boolean,
    val message: String,
    val config: MalwareProtectionConfig? = null
)

// Family Mode Models
@Serializable
data class FamilyModeConfig(
    val userId: String,
    val enabled: Boolean = false,
    val contentCategories: List<ContentCategory> = emptyList(),
    val timeRestrictions: TimeRestrictions? = null,
    val safeSearch: Boolean = true,
    val lastUpdated: LocalDateTime = LocalDateTime.now(),
    val stats: FamilyModeStats = FamilyModeStats()
)

@Serializable
data class ContentCategory(
    val id: String,
    val name: String,
    val description: String,
    val blocked: Boolean = false,
    val severity: ContentSeverity = ContentSeverity.MEDIUM
)

@Serializable
enum class ContentSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

@Serializable
data class TimeRestrictions(
    val enabled: Boolean = false,
    val dailyLimit: Int = 0, // hours
    val bedtime: String? = null, // HH:mm format
    val wakeTime: String? = null, // HH:mm format
    val weekendRules: Boolean = false
)

@Serializable
data class FamilyModeStats(
    val contentBlocked: Long = 0,
    val timeRestrictions: Long = 0,
    val lastBlock: LocalDateTime? = null,
    val lastReset: LocalDateTime = LocalDateTime.now()
)

@Serializable
data class FamilyModeRequest(
    val enabled: Boolean,
    val contentCategories: List<String>? = null, // category IDs
    val timeRestrictions: TimeRestrictions? = null,
    val safeSearch: Boolean? = null
)

@Serializable
data class FamilyModeResponse(
    val success: Boolean,
    val message: String,
    val config: FamilyModeConfig? = null
)

// DNS Leak Protection Models
@Serializable
data class DNSProtectionConfig(
    val userId: String,
    val enabled: Boolean = false,
    val secureDNSServers: List<String> = emptyList(),
    val leakDetection: Boolean = true,
    val autoSwitch: Boolean = true,
    val customDNS: String? = null,
    val lastUpdated: LocalDateTime = LocalDateTime.now(),
    val stats: DNSProtectionStats = DNSProtectionStats()
)

@Serializable
data class DNSProtectionStats(
    val leaksDetected: Long = 0,
    val dnsQueries: Long = 0,
    val lastLeak: LocalDateTime? = null,
    val lastReset: LocalDateTime = LocalDateTime.now()
)

@Serializable
data class DNSProtectionRequest(
    val enabled: Boolean,
    val secureDNSServers: List<String>? = null,
    val leakDetection: Boolean? = null,
    val autoSwitch: Boolean? = null,
    val customDNS: String? = null
)

@Serializable
data class DNSProtectionResponse(
    val success: Boolean,
    val message: String,
    val config: DNSProtectionConfig? = null
)

// Combined Security Configuration
@Serializable
data class SecurityConfig(
    val userId: String,
    val adBlocking: AdBlockingConfig,
    val malwareProtection: MalwareProtectionConfig,
    val familyMode: FamilyModeConfig,
    val dnsProtection: DNSProtectionConfig,
    val lastUpdated: LocalDateTime = LocalDateTime.now()
)

@Serializable
data class SecurityConfigRequest(
    val adBlocking: AdBlockingRequest? = null,
    val malwareProtection: MalwareProtectionRequest? = null,
    val familyMode: FamilyModeRequest? = null,
    val dnsProtection: DNSProtectionRequest? = null
)

@Serializable
data class SecurityConfigResponse(
    val success: Boolean,
    val message: String,
    val config: SecurityConfig? = null
)

// Threat Detection Models
@Serializable
data class ThreatReport(
    val id: String,
    val userId: String,
    val threatType: ThreatType,
    val severity: ThreatSeverity,
    val source: String,
    val description: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val blocked: Boolean = true,
    val details: Map<String, String> = emptyMap()
)

@Serializable
enum class ThreatType {
    AD, TRACKER, MALWARE, PHISHING, INAPPROPRIATE_CONTENT, DNS_LEAK
}

@Serializable
enum class ThreatSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

@Serializable
data class SecurityAnalytics(
    val userId: String,
    val period: String, // daily, weekly, monthly
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val threatsBlocked: Long = 0,
    val adsBlocked: Long = 0,
    val contentFiltered: Long = 0,
    val dnsQueries: Long = 0,
    val securityScore: Int = 100
)
