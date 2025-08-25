package com.example.v.vpn

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * Ad and Tracker Blocking Service
 * Provides content filtering capabilities similar to desktop version
 * Uses host-based blocking and DNS filtering
 */
class AdBlockingService private constructor(private val context: Context) {
    companion object {
        private const val TAG = "AdBlockingService"
        private const val PREFS_NAME = "ad_blocking_prefs"
        private const val KEY_ENABLED = "ad_blocking_enabled"
        private const val KEY_BLOCKING_LEVEL = "blocking_level"
        private const val KEY_CUSTOM_BLOCKS = "custom_blocks"
        private const val KEY_CUSTOM_ALLOWS = "custom_allows"
        private const val KEY_UPDATE_FREQUENCY = "update_frequency"
        
        // Blocking levels
        const val LEVEL_NONE = 0
        const val LEVEL_BASIC = 1
        const val LEVEL_AGGRESSIVE = 2
        const val LEVEL_ULTRA = 3
        const val LEVEL_CUSTOM = 4
        
        @Volatile
        private var INSTANCE: AdBlockingService? = null
        
        fun getInstance(context: Context): AdBlockingService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdBlockingService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val blockListDir = File(context.filesDir, "blocklists")
    
    // State flows
    private val _isEnabled = MutableStateFlow(prefs.getBoolean(KEY_ENABLED, true))
    val isEnabled: StateFlow<Boolean> = _isEnabled
    
    private val _blockingLevel = MutableStateFlow(prefs.getInt(KEY_BLOCKING_LEVEL, LEVEL_AGGRESSIVE))
    val blockingLevel: StateFlow<Int> = _blockingLevel
    
    private val _customBlocks = MutableStateFlow<Set<String>>(getCustomBlocks())
    val customBlocks: StateFlow<Set<String>> = _customBlocks
    
    private val _customAllows = MutableStateFlow<Set<String>>(getCustomAllows())
    val customAllows: StateFlow<Set<String>> = _customAllows
    
    private val _updateFrequency = MutableStateFlow(prefs.getInt(KEY_UPDATE_FREQUENCY, 7))
    val updateFrequency: StateFlow<Int> = _updateFrequency
    
    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive
    
    // Blocking statistics
    private val _blockedRequests = MutableStateFlow(0L)
    val blockedRequests: StateFlow<Long> = _blockedRequests
    
    private val _totalRequests = MutableStateFlow(0L)
    val totalRequests: StateFlow<Long> = _totalRequests
    
    // Blocklist cache
    private val blocklistCache = ConcurrentHashMap<String, Set<String>>()
    
    // Blocklist sources
    private val blocklistSources = mapOf(
        LEVEL_BASIC to listOf(
            "https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts",
            "https://raw.githubusercontent.com/pi-hole/pi-hole/master/adlists.default"
        ),
        LEVEL_AGGRESSIVE to listOf(
            "https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts",
            "https://raw.githubusercontent.com/pi-hole/pi-hole/master/adlists.default",
            "https://raw.githubusercontent.com/durablenapkin/scamblocklist/master/adguard.txt",
            "https://raw.githubusercontent.com/PolishFiltersTeam/KADhosts/master/KADhosts.txt"
        ),
        LEVEL_ULTRA to listOf(
            "https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts",
            "https://raw.githubusercontent.com/pi-hole/pi-hole/master/adlists.default",
            "https://raw.githubusercontent.com/durablenapkin/scamblocklist/master/adguard.txt",
            "https://raw.githubusercontent.com/PolishFiltersTeam/KADhosts/master/KADhosts.txt",
            "https://raw.githubusercontent.com/FadeMind/hosts.extras/master/add.Spam/hosts",
            "https://raw.githubusercontent.com/mitchellkrogza/The-Big-List-of-Hacked-Malware-Web-Sites/master/hosts"
        )
    )
    
    init {
        createBlocklistDirectory()
        loadBlocklists()
    }
    
    /**
     * Enable/disable ad blocking
     */
    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
        Log.i(TAG, "🔧 Ad blocking ${if (enabled) "enabled" else "disabled"}")
        
        if (enabled) {
            activate()
        } else {
            deactivate()
        }
    }
    
    /**
     * Set blocking level
     */
    fun setBlockingLevel(level: Int) {
        if (level in LEVEL_NONE..LEVEL_CUSTOM) {
            _blockingLevel.value = level
            prefs.edit().putInt(KEY_BLOCKING_LEVEL, level).apply()
            Log.i(TAG, "🔧 Blocking level set to: $level")
            
            if (level != LEVEL_CUSTOM) {
                // Note: updateBlocklists() is a suspend function and should be called from a coroutine
                // This will be handled by the caller
            }
        }
    }
    
    /**
     * Add custom blocked domain
     */
    fun addCustomBlock(domain: String) {
        val cleanDomain = cleanDomain(domain)
        val current = _customBlocks.value.toMutableSet()
        current.add(cleanDomain)
        _customBlocks.value = current
        saveCustomBlocks(current)
        Log.i(TAG, "➕ Custom block added: $cleanDomain")
    }
    
    /**
     * Remove custom blocked domain
     */
    fun removeCustomBlock(domain: String) {
        val cleanDomain = cleanDomain(domain)
        val current = _customBlocks.value.toMutableSet()
        if (current.remove(cleanDomain)) {
            _customBlocks.value = current
            saveCustomBlocks(current)
            Log.i(TAG, "➖ Custom block removed: $cleanDomain")
        }
    }
    
    /**
     * Add custom allowed domain
     */
    fun addCustomAllow(domain: String) {
        val cleanDomain = cleanDomain(domain)
        val current = _customAllows.value.toMutableSet()
        current.add(cleanDomain)
        _customAllows.value = current
        saveCustomAllows(current)
        Log.i(TAG, "➕ Custom allow added: $cleanDomain")
    }
    
    /**
     * Remove custom allowed domain
     */
    fun removeCustomAllow(domain: String) {
        val cleanDomain = cleanDomain(domain)
        val current = _customAllows.value.toMutableSet()
        if (current.remove(cleanDomain)) {
            _customAllows.value = current
            saveCustomAllows(current)
            Log.i(TAG, "➖ Custom allow removed: $cleanDomain")
        }
    }
    
    /**
     * Set update frequency (days)
     */
    fun setUpdateFrequency(days: Int) {
        if (days in 1..30) {
            _updateFrequency.value = days
            prefs.edit().putInt(KEY_UPDATE_FREQUENCY, days).apply()
            Log.i(TAG, "⏱️ Update frequency set to: ${days} days")
        }
    }
    
    /**
     * Check if domain should be blocked
     */
    fun shouldBlockDomain(domain: String): Boolean {
        if (!_isEnabled.value || _blockingLevel.value == LEVEL_NONE) {
            return false
        }
        
        val cleanDomain = cleanDomain(domain)
        _totalRequests.value++
        
        // Check custom allows first (highest priority)
        if (_customAllows.value.contains(cleanDomain)) {
            return false
        }
        
        // Check custom blocks
        if (_customBlocks.value.contains(cleanDomain)) {
            _blockedRequests.value++
            return true
        }
        
        // Check blocklists
        if (_blockingLevel.value != LEVEL_CUSTOM) {
            val isBlocked = isDomainInBlocklists(cleanDomain)
            if (isBlocked) {
                _blockedRequests.value++
                return true
            }
        }
        
        return false
    }
    
    /**
     * Activate ad blocking service
     */
    fun activate() {
        if (!_isEnabled.value) {
            Log.i(TAG, "⚠️ Ad blocking is disabled, not activating")
            return
        }
        
        _isActive.value = true
        Log.i(TAG, "🛡️ Ad blocking service activated")
    }
    
    /**
     * Deactivate ad blocking service
     */
    fun deactivate() {
        _isActive.value = false
        Log.i(TAG, "🛡️ Ad blocking service deactivated")
    }
    
    /**
     * Update blocklists from sources
     */
    suspend fun updateBlocklists() {
        try {
            Log.i(TAG, "🔄 Updating blocklists...")
            
            val level = _blockingLevel.value
            if (level == LEVEL_NONE || level == LEVEL_CUSTOM) {
                Log.i(TAG, "⚠️ No blocklist update needed for level: $level")
                return
            }
            
            val sources = blocklistSources[level] ?: emptyList()
            val allDomains = mutableSetOf<String>()
            
            sources.forEach { source ->
                try {
                    val domains = downloadBlocklist(source)
                    allDomains.addAll(domains)
                    Log.i(TAG, "📥 Downloaded ${domains.size} domains from: $source")
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Failed to download from $source: ${e.message}")
                }
            }
            
            // Save combined blocklist
            val blocklistFile = File(blockListDir, "level_${level}_blocklist.txt")
            FileWriter(blocklistFile).use { writer ->
                allDomains.forEach { domain ->
                    writer.write("$domain\n")
                }
            }
            
            // Update cache
            blocklistCache[level.toString()] = allDomains
            
            Log.i(TAG, "✅ Blocklist updated: ${allDomains.size} domains")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to update blocklists: ${e.message}")
        }
    }
    
    /**
     * Download blocklist from URL
     */
    private suspend fun downloadBlocklist(url: String): Set<String> {
        return try {
            val connection = URL(url).openConnection()
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val content = connection.getInputStream().bufferedReader().readText()
            val domains = mutableSetOf<String>()
            
            content.lines().forEach { line ->
                val cleanLine = line.trim()
                if (cleanLine.isNotEmpty() && !cleanLine.startsWith("#")) {
                    val domain = extractDomain(cleanLine)
                    if (domain.isNotEmpty()) {
                        domains.add(domain)
                    }
                }
            }
            
            domains
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to download blocklist from $url: ${e.message}")
            emptySet()
        }
    }
    
    /**
     * Extract domain from blocklist line
     */
    private fun extractDomain(line: String): String {
        return try {
            // Handle different blocklist formats
            when {
                line.contains("\t") -> line.split("\t")[1].trim()
                line.contains(" ") -> line.split(" ")[1].trim()
                else -> line.trim()
            }.let { domain ->
                if (domain.startsWith("0.0.0.0 ") || domain.startsWith("127.0.0.1 ")) {
                    domain.substringAfter(" ").trim()
                } else {
                    domain
                }
            }
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Check if domain is in blocklists
     */
    private fun isDomainInBlocklists(domain: String): Boolean {
        val level = _blockingLevel.value.toString()
        val blocklist = blocklistCache[level] ?: loadBlocklistFromFile(level)
        
        return blocklist.contains(domain) || 
               blocklist.any { blockedDomain -> 
                   domain.endsWith(".$blockedDomain") || domain == blockedDomain 
               }
    }
    
    /**
     * Load blocklist from file
     */
    private fun loadBlocklistFromFile(level: String): Set<String> {
        return try {
            val blocklistFile = File(blockListDir, "level_${level}_blocklist.txt")
            if (blocklistFile.exists()) {
                val domains = blocklistFile.readLines().toSet()
                blocklistCache[level] = domains
                domains
            } else {
                emptySet()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to load blocklist for level $level: ${e.message}")
            emptySet()
        }
    }
    
    /**
     * Load all blocklists
     */
    private fun loadBlocklists() {
        try {
            for (level in LEVEL_BASIC..LEVEL_ULTRA) {
                loadBlocklistFromFile(level.toString())
            }
            Log.i(TAG, "📚 Blocklists loaded")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to load blocklists: ${e.message}")
        }
    }
    
    /**
     * Clean domain string
     */
    private fun cleanDomain(domain: String): String {
        return domain.lowercase().trim().removePrefix("www.")
    }
    
    /**
     * Create blocklist directory
     */
    private fun createBlocklistDirectory() {
        if (!blockListDir.exists()) {
            blockListDir.mkdirs()
            Log.i(TAG, "✅ Blocklist directory created: ${blockListDir.absolutePath}")
        }
    }
    
    /**
     * Get blocking statistics
     */
    fun getStatistics(): AdBlockingStats {
        return AdBlockingStats(
            isEnabled = _isEnabled.value,
            blockingLevel = _blockingLevel.value,
            blockedRequests = _blockedRequests.value,
            totalRequests = _totalRequests.value,
            blockRate = if (_totalRequests.value > 0) {
                (_blockedRequests.value.toDouble() / _totalRequests.value) * 100
            } else 0.0,
            customBlocks = _customBlocks.value.size,
            customAllows = _customAllows.value.size
        )
    }
    
    /**
     * Get ad blocking configuration
     */
    fun getConfig(): AdBlockingConfig {
        return AdBlockingConfig(
            isEnabled = _isEnabled.value,
            blockingLevel = _blockingLevel.value,
            customBlocks = _customBlocks.value.toList(),
            customAllows = _customAllows.value.toList(),
            updateFrequency = _updateFrequency.value,
            isActive = _isActive.value
        )
    }
    
    private fun getCustomBlocks(): Set<String> {
        return prefs.getStringSet(KEY_CUSTOM_BLOCKS, emptySet()) ?: emptySet()
    }
    
    private fun getCustomAllows(): Set<String> {
        return prefs.getStringSet(KEY_CUSTOM_ALLOWS, emptySet()) ?: emptySet()
    }
    
    private fun saveCustomBlocks(blocks: Set<String>) {
        prefs.edit().putStringSet(KEY_CUSTOM_BLOCKS, blocks).apply()
    }
    
    private fun saveCustomAllows(allows: Set<String>) {
        prefs.edit().putStringSet(KEY_CUSTOM_ALLOWS, allows).apply()
    }
}

/**
 * Ad blocking configuration
 */
data class AdBlockingConfig(
    val isEnabled: Boolean,
    val blockingLevel: Int,
    val customBlocks: List<String>,
    val customAllows: List<String>,
    val updateFrequency: Int,
    val isActive: Boolean
)

/**
 * Ad blocking statistics
 */
data class AdBlockingStats(
    val isEnabled: Boolean,
    val blockingLevel: Int,
    val blockedRequests: Long,
    val totalRequests: Long,
    val blockRate: Double,
    val customBlocks: Int,
    val customAllows: Int
)
