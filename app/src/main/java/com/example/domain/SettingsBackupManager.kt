package com.example.domain

import android.content.Context
import android.util.Base64
import com.example.model.AppSettings
import com.example.model.BrandLogoChoice
import com.example.model.BrandingSlot
import com.example.model.CustomColorScheme
import com.example.model.HeaderEmblemChoice
import com.example.model.HeroBackdropChoice
import com.example.model.MascotSlotChoice
import com.example.model.PdfLogoMode
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class SettingsBackupExportStats(
    val exportedImageCount: Int
)

data class SettingsBackupRestoreResult(
    val settings: AppSettings,
    val restoredImageCount: Int
)

object SettingsBackupManager {

    const val SETTINGS_ENTRY_NAME = "settings.json"
    private const val IMAGES_DIR = "images/"
    private const val MAX_ARCHIVE_BYTES = 25 * 1024 * 1024
    private const val MAX_ENTRY_BYTES = 12 * 1024 * 1024

    private val RECOGNIZED_SETTINGS_KEYS = setOf(
        "companyName",
        "companyTagline",
        "themePreset",
        "accentColor",
        "defaultHourlyRate",
        "pdfEstimateTitle",
        "pdfProposalTitle",
        "headerEmblemChoice",
        "brandLogoChoice",
        "customColorSchemes",
        "activeCustomSchemeId"
    )

    private val RECOGNIZED_SCHEME_KEYS = setOf(
        "primaryHex",
        "accentHex",
        "backgroundHex",
        "surfaceHex"
    )

    fun encodeColorSchemeJson(scheme: CustomColorScheme): String =
        JSONObject().apply {
            put("format", "thdr_color_scheme_v1")
            put("scheme", PriceBookJsonCodec.encodeCustomColorScheme(scheme))
        }.toString(2)

    fun decodeColorSchemeFromStream(inputStream: InputStream): CustomColorScheme {
        val rawBytes = readBoundedBytes(inputStream, 1024 * 1024)
        val text = rawBytes.toString(Charsets.UTF_8).trim()
        if (!text.startsWith("{")) {
            throw IllegalArgumentException("Choose a valid .json color scheme file.")
        }
        val root = JSONObject(text)
        val schemeObj = root.optJSONObject("scheme") ?: root
        if (!RECOGNIZED_SCHEME_KEYS.any { schemeObj.has(it) }) {
            throw IllegalArgumentException("Selected JSON file does not contain color scheme colors.")
        }
        return PriceBookJsonCodec.decodeCustomColorScheme(schemeObj)
    }

    fun writeBackupZip(
        settings: AppSettings,
        outputStream: OutputStream
    ): SettingsBackupExportStats {
        val portableSettings = stripLocalPaths(settings)
        val settingsJson = PriceBookJsonCodec.encodeAppSettings(portableSettings)

        var exportedImages = 0
        ZipOutputStream(outputStream.buffered()).use { zip ->
            zip.putNextEntry(ZipEntry(SETTINGS_ENTRY_NAME))
            zip.write(settingsJson.toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            for (slot in BrandingSlot.entries) {
                val path = pathForSlot(settings, slot)
                if (path.isNotBlank()) {
                    val file = File(path)
                    if (file.exists() && file.isFile && file.length() in 1L..MAX_ENTRY_BYTES.toLong()) {
                        val bytes = file.readBytes()
                        zip.putNextEntry(ZipEntry("$IMAGES_DIR${slot.key}.png"))
                        zip.write(bytes)
                        zip.closeEntry()
                        exportedImages++
                    }
                }
            }
            zip.finish()
        }
        return SettingsBackupExportStats(exportedImageCount = exportedImages)
    }

    fun restoreFromStream(
        context: Context,
        inputStream: InputStream
    ): SettingsBackupRestoreResult {
        val rawBytes = readBoundedBytes(inputStream, MAX_ARCHIVE_BYTES)
        if (rawBytes.isEmpty()) {
            throw IllegalArgumentException("The selected file is empty.")
        }

        val importedPaths = mutableMapOf<BrandingSlot, String>()
        val decodedSettings: AppSettings

        try {
            if (isZipHeader(rawBytes)) {
                val entries = extractZipEntries(rawBytes)
                val settingsBytes = entries[SETTINGS_ENTRY_NAME]
                    ?: entries.entries.firstOrNull { (name, _) ->
                        name.endsWith(".json", ignoreCase = true) && !name.contains("/")
                    }?.value
                    ?: throw IllegalArgumentException("Archive is missing settings.json.")

                val jsonText = settingsBytes.toString(Charsets.UTF_8)
                decodedSettings = parseSettingsObject(jsonText)

                for ((entryName, bytes) in entries) {
                    val slot = slotForEntryName(entryName) ?: continue
                    val savedPath = BrandingImageStore.importFromBytes(context, slot, bytes)
                    if (!savedPath.isNullOrBlank()) {
                        importedPaths[slot]?.let { BrandingImageStore.deleteFileIfExists(it) }
                        importedPaths[slot] = savedPath
                    }
                }
            } else {
                val jsonText = rawBytes.toString(Charsets.UTF_8).trim()
                if (!jsonText.startsWith("{")) {
                    throw IllegalArgumentException("Unsupported file format. Choose a .zip or .json settings backup.")
                }
                val root = JSONObject(jsonText)
                val settingsObj = root.optJSONObject("settings") ?: root
                if (!hasRecognizedKeys(settingsObj)) {
                    throw IllegalArgumentException("Selected JSON file is not a valid Settings backup.")
                }
                decodedSettings = PriceBookJsonCodec.decodeAppSettings(settingsObj.toString())

                val imagesObj = root.optJSONObject("images") ?: root.optJSONObject("embeddedImages")
                if (imagesObj != null) {
                    for (slot in BrandingSlot.entries) {
                        val b64 = imagesObj.optString(slot.key, "").trim()
                        if (b64.isNotEmpty()) {
                            val cleanB64 = b64.substringAfter("base64,", b64)
                            val imageBytes = runCatching { Base64.decode(cleanB64, Base64.DEFAULT) }.getOrNull()
                            if (imageBytes != null && imageBytes.isNotEmpty()) {
                                val savedPath = BrandingImageStore.importFromBytes(context, slot, imageBytes)
                                if (!savedPath.isNullOrBlank()) {
                                    importedPaths[slot] = savedPath
                                }
                            }
                        }
                    }
                }
            }

            val reconciled = reconcileRestoredSettings(
                decoded = decodedSettings,
                importedSlotPaths = importedPaths,
                existingPathValidator = { existingPath ->
                    existingPath.isNotBlank() && File(existingPath).let { it.exists() && it.isFile }
                }
            )
            return SettingsBackupRestoreResult(
                settings = reconciled,
                restoredImageCount = importedPaths.size
            )
        } catch (t: Throwable) {
            importedPaths.values.forEach { BrandingImageStore.deleteFileIfExists(it) }
            if (t is IllegalArgumentException) throw t
            throw IllegalArgumentException("Could not read settings backup: ${t.message ?: "invalid file"}")
        }
    }

    internal fun isZipHeader(bytes: ByteArray): Boolean =
        bytes.size >= 4 &&
            bytes[0] == 0x50.toByte() &&
            bytes[1] == 0x4B.toByte() &&
            (bytes[2] == 0x03.toByte() || bytes[2] == 0x05.toByte() || bytes[2] == 0x07.toByte())

    internal fun extractZipEntries(zipBytes: ByteArray): Map<String, ByteArray> {
        val entries = linkedMapOf<String, ByteArray>()
        var totalRead = 0L
        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val rawName = entry.name.replace('\\', '/').trimStart('/')
                if (!entry.isDirectory && rawName.isNotEmpty() && !rawName.contains("..")) {
                    val buf = ByteArrayOutputStream()
                    val chunk = ByteArray(8192)
                    var entryTotal = 0L
                    while (true) {
                        val read = zis.read(chunk)
                        if (read <= 0) break
                        entryTotal += read
                        totalRead += read
                        if (entryTotal > MAX_ENTRY_BYTES || totalRead > MAX_ARCHIVE_BYTES) {
                            throw IllegalArgumentException("Backup archive exceeds maximum supported size.")
                        }
                        buf.write(chunk, 0, read)
                    }
                    entries[rawName] = buf.toByteArray()
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return entries
    }

    internal fun buildZipBytes(files: Map<String, ByteArray>): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zip ->
            files.forEach { (name, content) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(content)
                zip.closeEntry()
            }
            zip.finish()
        }
        return baos.toByteArray()
    }

    internal fun slotForEntryName(entryName: String): BrandingSlot? {
        val normalized = entryName.replace('\\', '/').trimStart('/').lowercase(Locale.US)
        val fileName = normalized.substringAfterLast('/')
        if (!fileName.endsWith(".png") && !fileName.endsWith(".jpg") &&
            !fileName.endsWith(".jpeg") && !fileName.endsWith(".webp")
        ) {
            return null
        }
        val base = fileName.substringBeforeLast('.')
        return BrandingSlot.entries.firstOrNull { slot ->
            base == slot.key || base.startsWith("${slot.key}_")
        }
    }

    internal fun reconcileRestoredSettings(
        decoded: AppSettings,
        importedSlotPaths: Map<BrandingSlot, String>,
        existingPathValidator: (String) -> Boolean = { false }
    ): AppSettings {
        val headerPath = resolveSlotPath(
            importedSlotPaths[BrandingSlot.HEADER_EMBLEM],
            decoded.customHeaderEmblemPath,
            existingPathValidator
        )
        val lockupPath = resolveSlotPath(
            importedSlotPaths[BrandingSlot.BRAND_LOCKUP],
            decoded.customBrandLogoPath,
            existingPathValidator
        )
        val backdropPath = resolveSlotPath(
            importedSlotPaths[BrandingSlot.HERO_BACKDROP],
            decoded.customBackdropPath,
            existingPathValidator
        )
        val mascotPath = resolveSlotPath(
            importedSlotPaths[BrandingSlot.HOME_MASCOT],
            decoded.customMascotPath,
            existingPathValidator
        )
        val pdfLogoPath = resolveSlotPath(
            importedSlotPaths[BrandingSlot.PDF_LOGO],
            decoded.customPdfLogoPath,
            existingPathValidator
        )

        return decoded.copy(
            customHeaderEmblemPath = headerPath,
            headerEmblemChoice = if (decoded.headerEmblemChoice == HeaderEmblemChoice.CUSTOM && headerPath.isBlank()) {
                HeaderEmblemChoice.DEFAULT_EMBLEM
            } else {
                decoded.headerEmblemChoice
            },
            customBrandLogoPath = lockupPath,
            brandLogoChoice = if (decoded.brandLogoChoice == BrandLogoChoice.CUSTOM && lockupPath.isBlank()) {
                BrandLogoChoice.DEFAULT_LOCKUP
            } else {
                decoded.brandLogoChoice
            },
            customBackdropPath = backdropPath,
            heroBackdropChoice = if (decoded.heroBackdropChoice == HeroBackdropChoice.CUSTOM && backdropPath.isBlank()) {
                HeroBackdropChoice.DEFAULT_BLUEPRINT
            } else {
                decoded.heroBackdropChoice
            },
            customMascotPath = mascotPath,
            mascotChoice = if (decoded.mascotChoice == MascotSlotChoice.CUSTOM && mascotPath.isBlank()) {
                MascotSlotChoice.STANDING_MASCOT
            } else {
                decoded.mascotChoice
            },
            customPdfLogoPath = pdfLogoPath,
            pdfLogoMode = if (decoded.pdfLogoMode == PdfLogoMode.CUSTOM_PDF_IMAGE && pdfLogoPath.isBlank()) {
                PdfLogoMode.EMBLEM_AND_LOCKUP
            } else {
                decoded.pdfLogoMode
            }
        )
    }

    internal fun stripLocalPaths(settings: AppSettings): AppSettings = settings.copy(
        customHeaderEmblemPath = "",
        customBrandLogoPath = "",
        customBackdropPath = "",
        customMascotPath = "",
        customPdfLogoPath = ""
    )

    private fun resolveSlotPath(
        importedPath: String?,
        decodedPath: String,
        existingPathValidator: (String) -> Boolean
    ): String = when {
        !importedPath.isNullOrBlank() -> importedPath
        decodedPath.isNotBlank() && existingPathValidator(decodedPath) -> decodedPath
        else -> ""
    }

    private fun pathForSlot(settings: AppSettings, slot: BrandingSlot): String = when (slot) {
        BrandingSlot.HEADER_EMBLEM -> settings.customHeaderEmblemPath
        BrandingSlot.BRAND_LOCKUP -> settings.customBrandLogoPath
        BrandingSlot.HERO_BACKDROP -> settings.customBackdropPath
        BrandingSlot.HOME_MASCOT -> settings.customMascotPath
        BrandingSlot.PDF_LOGO -> settings.customPdfLogoPath
    }

    private fun parseSettingsObject(jsonText: String): AppSettings {
        val root = JSONObject(jsonText)
        val settingsObj = root.optJSONObject("settings") ?: root
        if (!hasRecognizedKeys(settingsObj)) {
            throw IllegalArgumentException("Selected archive does not contain valid Settings data.")
        }
        return PriceBookJsonCodec.decodeAppSettings(settingsObj.toString())
    }

    private fun hasRecognizedKeys(obj: JSONObject): Boolean =
        RECOGNIZED_SETTINGS_KEYS.any { obj.has(it) }

    private fun readBoundedBytes(inputStream: InputStream, maxBytes: Int): ByteArray {
        val out = ByteArrayOutputStream()
        val buf = ByteArray(8192)
        var total = 0L
        while (true) {
            val read = inputStream.read(buf)
            if (read <= 0) break
            total += read
            if (total > maxBytes) {
                throw IllegalArgumentException("Selected file is too large (max ${maxBytes / (1024 * 1024)} MB).")
            }
            out.write(buf, 0, read)
        }
        return out.toByteArray()
    }
}
