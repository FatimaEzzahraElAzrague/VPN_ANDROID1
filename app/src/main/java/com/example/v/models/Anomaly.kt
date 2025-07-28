package com.example.v.models
data class Anomaly(
    val id: String,
    val timestamp: Long,
    val type: AnomalyType,
    val severity: Severity,
    val description: String,
    val suggestion: String,
    val isResolved: Boolean = false
)

enum class AnomalyType {
    SUSPICIOUS_TRAFFIC,
    MALWARE_DETECTED,
    DNS_LEAK,
    UNUSUAL_BANDWIDTH,
    TRACKING_ATTEMPT,
    PHISHING_BLOCKED
}

enum class Severity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

val mockAnomalies = listOf(
    Anomaly(
        id = "1",
        timestamp = System.currentTimeMillis() - 1800000,
        type = AnomalyType.TRACKING_ATTEMPT,
        severity = Severity.MEDIUM,
        description = "Blocked 15 tracking attempts from social media platforms",
        suggestion = "Consider enabling stricter ad blocking in Security settings"
    ),
    Anomaly(
        id = "2",
        timestamp = System.currentTimeMillis() - 3600000,
        type = AnomalyType.SUSPICIOUS_TRAFFIC,
        severity = Severity.HIGH,
        description = "Unusual outbound traffic detected to unknown servers",
        suggestion = "Run a malware scan and check installed applications"
    ),
    Anomaly(
        id = "3",
        timestamp = System.currentTimeMillis() - 7200000,
        type = AnomalyType.PHISHING_BLOCKED,
        severity = Severity.CRITICAL,
        description = "Blocked access to known phishing website",
        suggestion = "Avoid clicking suspicious links in emails or messages"
    ),
    Anomaly(
        id = "4",
        timestamp = System.currentTimeMillis() - 10800000,
        type = AnomalyType.DNS_LEAK,
        severity = Severity.LOW,
        description = "Minor DNS leak detected and automatically resolved",
        suggestion = "DNS leak protection is working correctly"
    )
)