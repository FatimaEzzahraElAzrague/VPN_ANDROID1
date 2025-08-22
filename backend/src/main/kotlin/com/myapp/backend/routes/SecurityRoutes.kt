package com.myapp.backend.routes

import com.myapp.backend.models.*
import com.myapp.backend.services.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.parsing.*

fun Route.securityRoutes() {
    val securityService = SecurityService.getInstance()
    val adBlockingService = AdBlockingService.getInstance()
    val malwareProtectionService = MalwareProtectionService.getInstance()
    val familyModeService = FamilyModeService.getInstance()
    val dnsProtectionService = DNSProtectionService.getInstance()
    
    route("/security") {
        // Combined Security Configuration
        get("/config/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respondText(
                "User ID is required", status = HttpStatusCode.BadRequest
            )
            
            try {
                val config = securityService.getSecurityConfig(userId)
                call.respond(HttpStatusCode.OK, SecurityConfigResponse(
                    success = true,
                    message = "Security configuration retrieved successfully",
                    config = config
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, SecurityConfigResponse(
                    success = false,
                    message = "Failed to retrieve security configuration: ${e.message}"
                ))
            }
        }
        
        put("/config/{userId}") {
            val userId = call.parameters["userId"] ?: return@put call.respondText(
                "User ID is required", status = HttpStatusCode.BadRequest
            )
            
            try {
                val request = call.receive<SecurityConfigRequest>()
                val config = securityService.updateSecurityConfig(userId, request)
                
                call.respond(HttpStatusCode.OK, SecurityConfigResponse(
                    success = true,
                    message = "Security configuration updated successfully",
                    config = config
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, SecurityConfigResponse(
                    success = false,
                    message = "Failed to update security configuration: ${e.message}"
                ))
            }
        }
        
        // Security Check
        post("/check/{userId}") {
            val userId = call.parameters["userId"] ?: return@post call.respondText(
                "User ID is required", status = HttpStatusCode.BadRequest
            )
            
            try {
                val request = call.receive<SecurityCheckRequest>()
                val result = securityService.checkSecurity(userId, request.url, request.domain)
                
                call.respond(HttpStatusCode.OK, SecurityCheckResponse(
                    success = true,
                    message = "Security check completed",
                    result = result
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, SecurityCheckResponse(
                    success = false,
                    message = "Failed to perform security check: ${e.message}"
                ))
            }
        }
        
        // Security Analytics
        get("/analytics/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respondText(
                "User ID is required", status = HttpStatusCode.BadRequest
            )
            
            val period = call.request.queryParameters["period"] ?: "daily"
            
            try {
                val analytics = securityService.getSecurityAnalytics(userId, period)
                call.respond(HttpStatusCode.OK, SecurityAnalyticsResponse(
                    success = true,
                    message = "Security analytics retrieved successfully",
                    analytics = analytics
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, SecurityAnalyticsResponse(
                    success = false,
                    message = "Failed to retrieve security analytics: ${e.message}"
                ))
            }
        }
        
        // Security Status
        get("/status/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respondText(
                "User ID is required", status = HttpStatusCode.BadRequest
            )
            
            try {
                val status = securityService.getSecurityStatus(userId)
                call.respond(HttpStatusCode.OK, SecurityStatusResponse(
                    success = true,
                    message = "Security status retrieved successfully",
                    status = status
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, SecurityStatusResponse(
                    success = false,
                    message = "Failed to retrieve security status: ${e.message}"
                ))
            }
        }
        
        // Bulk Operations
        post("/enable-all/{userId}") {
            val userId = call.parameters["userId"] ?: return@post call.respondText(
                "User ID is required", status = HttpStatusCode.BadRequest
            )
            
            try {
                val config = securityService.enableAllSecurityFeatures(userId)
                call.respond(HttpStatusCode.OK, SecurityConfigResponse(
                    success = true,
                    message = "All security features enabled successfully",
                    config = config
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, SecurityConfigResponse(
                    success = false,
                    message = "Failed to enable all security features: ${e.message}"
                ))
            }
        }
        
        post("/disable-all/{userId}") {
            val userId = call.parameters["userId"] ?: return@post call.respondText(
                "User ID is required", status = HttpStatusCode.BadRequest
            )
            
            try {
                val config = securityService.disableAllSecurityFeatures(userId)
                call.respond(HttpStatusCode.OK, SecurityConfigResponse(
                    success = true,
                    message = "All security features disabled successfully",
                    config = config
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, SecurityConfigResponse(
                    success = false,
                    message = "Failed to disable all security features: ${e.message}"
                ))
            }
        }
        
        post("/reset-stats/{userId}") {
            val userId = call.parameters["userId"] ?: return@post call.respondText(
                "User ID is required", status = HttpStatusCode.BadRequest
            )
            
            try {
                val config = securityService.resetAllStats(userId)
                call.respond(HttpStatusCode.OK, SecurityConfigResponse(
                    success = true,
                    message = "All security stats reset successfully",
                    config = config
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, SecurityConfigResponse(
                    success = false,
                    message = "Failed to reset security stats: ${e.message}"
                ))
            }
        }
        
        post("/update-databases") {
            try {
                val success = securityService.updateAllSecurityDatabases()
                if (success) {
                    call.respond(HttpStatusCode.OK, SecurityResponse(
                        success = true,
                        message = "All security databases updated successfully"
                    ))
                } else {
                    call.respond(HttpStatusCode.PartialContent, SecurityResponse(
                        success = false,
                        message = "Some security databases failed to update"
                    ))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, SecurityResponse(
                    success = false,
                    message = "Failed to update security databases: ${e.message}"
                ))
            }
        }
    }
    
    // Ad Blocking Routes
    route("/adblocking") {
        get("/config/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respondText(
                "User ID is required", status = HttpStatusCode.BadRequest
            )
            
            try {
                val config = adBlockingService.getAdBlockingConfig(userId)
                call.respond(HttpStatusCode.OK, AdBlockingResponse(
                    success = true,
                    message = "Ad blocking configuration retrieved successfully",
                    config = config
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, AdBlockingResponse(
                    success = false,
                    message = "Failed to retrieve ad blocking configuration: ${e.message}"
                ))
            }
        }
        
        put("/config/{userId}") {
            val userId = call.parameters["userId"] ?: return@put call.respondText(
                "User ID is required", status = HttpStatusCode.BadRequest
            )
            
            try {
                val request = call.receive<AdBlockingRequest>()
                val config = adBlockingService.updateAdBlockingConfig(userId, request)
                
                call.respond(HttpStatusCode.OK, AdBlockingResponse(
                    success = true,
                    message = "Ad blocking configuration updated successfully",
                    config = config
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, AdBlockingResponse(
                    success = false,
                    message = "Failed to update ad blocking configuration: ${e.message}"
                ))
            }
        }
        
        get("/filterlists") {
            try {
                val filterLists = adBlockingService.getFilterLists()
                call.respond(HttpStatusCode.OK, FilterListsResponse(
                    success = true,
                    message = "Filter lists retrieved successfully",
                    filterLists = filterLists
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, FilterListsResponse(
                    success = false,
                    message = "Failed to retrieve filter lists: ${e.message}"
                ))
            }
        }
        
        get("/stats/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respondText(
                "User ID is required", status = HttpStatusCode.BadRequest
            )
            
            try {
                val stats = adBlockingService.getStats(userId)
                call.respond(HttpStatusCode.OK, AdBlockingStatsResponse(
                    success = true,
                    message = "Ad blocking stats retrieved successfully",
                    stats = stats
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, AdBlockingStatsResponse(
                    success = false,
                    message = "Failed to retrieve ad blocking stats: ${e.message}"
                ))
            }
        }
        
        post("/update-filterlists") {
            try {
                val success = adBlockingService.updateFilterLists()
                call.respond(HttpStatusCode.OK, SecurityResponse(
                    success = success,
                    message = if (success) "Filter lists updated successfully" else "Failed to update filter lists"
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, SecurityResponse(
                    success = false,
                    message = "Failed to update filter lists: ${e.message}"
                ))
            }
        }
    }
    
    // Malware Protection Routes
    route("/malware") {
        get("/config/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respondText(
                "User ID is required", status = HttpStatusCode.BadRequest
            )
            
            try {
                val config = malwareProtectionService.getMalwareProtectionConfig(userId)
                call.respond(HttpStatusCode.OK, MalwareProtectionResponse(
                    success = true,
                    message = "Malware protection configuration retrieved successfully",
                    config = config
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, MalwareProtectionResponse(
                    success = false,
                    message = "Failed to retrieve malware protection configuration: ${e.message}"
                ))
            }
        }
        
        put("/config/{userId}") {
            val userId = call.parameters["userId"] ?: return@put call.respondText(
                "User ID is required", status = HttpStatusCode.BadRequest
            )
            
            try {
                val request = call.receive<MalwareProtectionRequest>()
                val config = malwareProtectionService.updateMalwareProtectionConfig(userId, request)
                
                call.respond(HttpStatusCode.OK, MalwareProtectionResponse(
                    success = true,
                    message = "Malware protection configuration updated successfully",
                    config = config
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, MalwareProtectionResponse(
                    success = false,
                    message = "Failed to update malware protection configuration: ${e.message}"
                ))
            }
        }
        
        get("/threat-database") {
            try {
                val threats = malwareProtectionService.getThreatDatabase()
                call.respond(HttpStatusCode.OK, ThreatDatabaseResponse(
                    success = true,
                    message = "Threat database retrieved successfully",
                    threats = threats
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ThreatDatabaseResponse(
                    success = false,
                    message = "Failed to retrieve threat database: ${e.message}"
                ))
            }
        }
        
        get("/stats/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respondText(
                "User ID is required", status = HttpStatusCode.BadRequest
            )
            
            try {
                val stats = malwareProtectionService.getStats(userId)
                call.respond(HttpStatusCode.OK, MalwareProtectionStatsResponse(
                    success = true,
                    message = "Malware protection stats retrieved successfully",
                    stats = stats
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, MalwareProtectionStatsResponse(
                    success = false,
                    message = "Failed to retrieve malware protection stats: ${e.message}"
                ))
            }
        }
        
        post("/update-threat-database") {
            try {
                val success = malwareProtectionService.updateThreatDatabase()
                call.respond(HttpStatusCode.OK, SecurityResponse(
                    success = success,
                    message = if (success) "Threat database updated successfully" else "Failed to update threat database"
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, SecurityResponse(
                    success = false,
                    message = "Failed to update threat database: ${e.message}"
                ))
            }
        }
    }
    
    // Family Mode Routes
    route("/familymode") {
        get("/config/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respondText(
                "User ID is required", status = HttpStatusCode.BadRequest
            )
            
            try {
                val config = familyModeService.getFamilyModeConfig(userId)
                call.respond(HttpStatusCode.OK, FamilyModeResponse(
                    success = true,
                    message = "Family mode configuration retrieved successfully",
                    config = config
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, FamilyModeResponse(
                    success = false,
                    message = "Failed to retrieve family mode configuration: ${e.message}"
                ))
            }
        }
        
        put("/config/{userId}") {
            val userId = call.parameters["userId"] ?: return@put call.respondText(
                "User ID is required", status = HttpStatusCode.BadRequest
            )
            
            try {
                val request = call.receive<FamilyModeRequest>()
                val config = familyModeService.updateFamilyModeConfig(userId, request)
                
                call.respond(HttpStatusCode.OK, FamilyModeResponse(
                    success = true,
                    message = "Family mode configuration updated successfully",
                    config = config
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, FamilyModeResponse(
                    success = false,
                    message = "Failed to update family mode configuration: ${e.message}"
                ))
            }
        }
        
        get("/content-categories") {
            try {
                val categories = familyModeService.getContentCategories()
                call.respond(HttpStatusCode.OK, ContentCategoriesResponse(
                    success = true,
                    message = "Content categories retrieved successfully",
                    categories = categories
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ContentCategoriesResponse(
                    success = false,
                    message = "Failed to retrieve content categories: ${e.message}"
                ))
            }
        }
        
        get("/stats/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respondText(
                "User ID is required", status = HttpStatusCode.BadRequest
            )
            
            try {
                val stats = familyModeService.getStats(userId)
                call.respond(HttpStatusCode.OK, FamilyModeStatsResponse(
                    success = true,
                    message = "Family mode stats retrieved successfully",
                    stats = stats
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, FamilyModeStatsResponse(
                    success = false,
                    message = "Failed to retrieve family mode stats: ${e.message}"
                ))
            }
        }
    }
    
    // DNS Protection Routes
    route("/dns") {
        get("/config/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respondText(
                "User ID is required", status = HttpStatusCode.BadRequest
            )
            
            try {
                val config = dnsProtectionService.getDNSProtectionConfig(userId)
                call.respond(HttpStatusCode.OK, DNSProtectionResponse(
                    success = true,
                    message = "DNS protection configuration retrieved successfully",
                    config = config
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, DNSProtectionResponse(
                    success = false,
                    message = "Failed to retrieve DNS protection configuration: ${e.message}"
                ))
            }
        }
        
        put("/config/{userId}") {
            val userId = call.parameters["userId"] ?: return@put call.respondText(
                "User ID is required", status = HttpStatusCode.BadRequest
            )
            
            try {
                val request = call.receive<DNSProtectionRequest>()
                val config = dnsProtectionService.updateDNSProtectionConfig(userId, request)
                
                call.respond(HttpStatusCode.OK, DNSProtectionResponse(
                    success = true,
                    message = "DNS protection configuration updated successfully",
                    config = config
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, DNSProtectionResponse(
                    success = false,
                    message = "Failed to update DNS protection configuration: ${e.message}"
                ))
            }
        }
        
        get("/secure-servers") {
            try {
                val servers = dnsProtectionService.getSecureDNSServers()
                call.respond(HttpStatusCode.OK, SecureDNSServersResponse(
                    success = true,
                    message = "Secure DNS servers retrieved successfully",
                    servers = servers
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, SecureDNSServersResponse(
                    success = false,
                    message = "Failed to retrieve secure DNS servers: ${e.message}"
                ))
            }
        }
        
        get("/stats/{userId}") {
            val userId = call.parameters["userId"] ?: return@get call.respondText(
                "User ID is required", status = HttpStatusCode.BadRequest
            )
            
            try {
                val stats = dnsProtectionService.getStats(userId)
                call.respond(HttpStatusCode.OK, DNSProtectionStatsResponse(
                    success = true,
                    message = "DNS protection stats retrieved successfully",
                    stats = stats
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, DNSProtectionStatsResponse(
                    success = false,
                    message = "Failed to retrieve DNS protection stats: ${e.message}"
                ))
            }
        }
        
        post("/test-server") {
            try {
                val request = call.receive<DNSTestRequest>()
                val result = dnsProtectionService.testDNSServer(request.server)
                call.respond(HttpStatusCode.OK, DNSTestResponse(
                    success = true,
                    message = "DNS server test completed",
                    result = result
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, DNSTestResponse(
                    success = false,
                    message = "Failed to test DNS server: ${e.message}"
                ))
            }
        }
        
        post("/update-servers") {
            try {
                val success = dnsProtectionService.updateSecureDNSServers()
                call.respond(HttpStatusCode.OK, SecurityResponse(
                    success = success,
                    message = if (success) "Secure DNS servers updated successfully" else "Failed to update secure DNS servers"
                ))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, SecurityResponse(
                    success = false,
                    message = "Failed to update secure DNS servers: ${e.message}"
                ))
            }
        }
    }
}

// Response models for the API
data class SecurityCheckRequest(
    val url: String,
    val domain: String
)

data class SecurityCheckResponse(
    val success: Boolean,
    val message: String,
    val result: SecurityService.SecurityCheckResult
)

data class SecurityAnalyticsResponse(
    val success: Boolean,
    val message: String,
    val analytics: SecurityAnalytics
)

data class SecurityStatusResponse(
    val success: Boolean,
    val message: String,
    val status: SecurityService.SecurityStatus
)

data class SecurityResponse(
    val success: Boolean,
    val message: String
)

data class FilterListsResponse(
    val success: Boolean,
    val message: String,
    val filterLists: Map<String, List<String>>
)

data class AdBlockingStatsResponse(
    val success: Boolean,
    val message: String,
    val stats: AdBlockingStats
)

data class ThreatDatabaseResponse(
    val success: Boolean,
    val message: String,
    val threats: Map<String, Any>
)

data class MalwareProtectionStatsResponse(
    val success: Boolean,
    val message: String,
    val stats: MalwareProtectionStats
)

data class ContentCategoriesResponse(
    val success: Boolean,
    val message: String,
    val categories: List<ContentCategory>
)

data class FamilyModeStatsResponse(
    val success: Boolean,
    val message: String,
    val stats: FamilyModeStats
)

data class SecureDNSServersResponse(
    val success: Boolean,
    val message: String,
    val servers: List<Any>
)

data class DNSProtectionStatsResponse(
    val success: Boolean,
    val message: String,
    val stats: DNSProtectionStats
)

data class DNSTestRequest(
    val server: String
)

data class DNSTestResponse(
    val success: Boolean,
    val message: String,
    val result: Any
)
