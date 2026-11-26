package com.example

import com.example.domain.SettingsBackupManager
import com.example.model.AppSettings
import com.example.model.BrandLogoChoice
import com.example.model.BrandingSlot
import com.example.model.HeaderEmblemChoice
import com.example.model.HeroBackdropChoice
import com.example.model.MascotSlotChoice
import com.example.model.PdfLogoMode
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsBackupManagerTest {

    @Test
    fun zipRoundTripFiltersUnsafeTraversalPathsAndDetectsHeader() {
        val sampleJson = """{"companyName":"House DR"}""".toByteArray(Charsets.UTF_8)
        val samplePng = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x01, 0x02)

        val zipBytes = SettingsBackupManager.buildZipBytes(
            mapOf(
                "settings.json" to sampleJson,
                "images/header_emblem.png" to samplePng,
                "../evil.txt" to "ignore".toByteArray(Charsets.UTF_8)
            )
        )

        assertTrue(SettingsBackupManager.isZipHeader(zipBytes))
        assertFalse(SettingsBackupManager.isZipHeader(sampleJson))

        val extracted = SettingsBackupManager.extractZipEntries(zipBytes)
        assertEquals(2, extracted.size)
        assertArrayEquals(sampleJson, extracted["settings.json"])
        assertArrayEquals(samplePng, extracted["images/header_emblem.png"])
        assertFalse(extracted.containsKey("../evil.txt"))
    }

    @Test
    fun slotForEntryNameMatchesAllBrandingSlotsAndIgnoresNonImages() {
        assertEquals(
            BrandingSlot.HEADER_EMBLEM,
            SettingsBackupManager.slotForEntryName("images/header_emblem.png")
        )
        assertEquals(
            BrandingSlot.BRAND_LOCKUP,
            SettingsBackupManager.slotForEntryName("brand_lockup_12345.webp")
        )
        assertEquals(
            BrandingSlot.HERO_BACKDROP,
            SettingsBackupManager.slotForEntryName("images\\hero_backdrop.JPEG")
        )
        assertEquals(
            BrandingSlot.HOME_MASCOT,
            SettingsBackupManager.slotForEntryName("home_mascot.jpg")
        )
        assertEquals(
            BrandingSlot.PDF_LOGO,
            SettingsBackupManager.slotForEntryName("images/pdf_logo_99.png")
        )
        assertNull(SettingsBackupManager.slotForEntryName("settings.json"))
        assertNull(SettingsBackupManager.slotForEntryName("images/unknown_slot.png"))
    }

    @Test
    fun stripLocalPathsClearsAllDeviceSpecificImagePaths() {
        val dirty = AppSettings(
            companyName = "Apex Contracting",
            customHeaderEmblemPath = "/data/user/0/app/files/branding/header.png",
            customBrandLogoPath = "/data/user/0/app/files/branding/lockup.png",
            customBackdropPath = "/data/user/0/app/files/branding/hero.png",
            customMascotPath = "/data/user/0/app/files/branding/mascot.png",
            customPdfLogoPath = "/data/user/0/app/files/branding/pdf.png"
        )

        val stripped = SettingsBackupManager.stripLocalPaths(dirty)
        assertEquals("Apex Contracting", stripped.companyName)
        assertEquals("", stripped.customHeaderEmblemPath)
        assertEquals("", stripped.customBrandLogoPath)
        assertEquals("", stripped.customBackdropPath)
        assertEquals("", stripped.customMascotPath)
        assertEquals("", stripped.customPdfLogoPath)
    }

    @Test
    fun reconcileRestoredSettingsAppliesImportedImagesAndResetsMissingCustomSlots() {
        val decoded = AppSettings(
            companyName = "Restored Co",
            headerEmblemChoice = HeaderEmblemChoice.CUSTOM,
            customHeaderEmblemPath = "/old/device/path/header.png",
            brandLogoChoice = BrandLogoChoice.CUSTOM,
            customBrandLogoPath = "/old/device/path/lockup.png",
            heroBackdropChoice = HeroBackdropChoice.CUSTOM,
            customBackdropPath = "",
            mascotChoice = MascotSlotChoice.CUSTOM,
            customMascotPath = "/old/device/path/mascot.png",
            pdfLogoMode = PdfLogoMode.CUSTOM_PDF_IMAGE,
            customPdfLogoPath = "/old/device/path/pdf.png"
        )

        val imported = mapOf(
            BrandingSlot.HEADER_EMBLEM to "/new/files/branding/header_emblem_1.png",
            BrandingSlot.PDF_LOGO to "/new/files/branding/pdf_logo_2.png"
        )

        val reconciled = SettingsBackupManager.reconcileRestoredSettings(
            decoded = decoded,
            importedSlotPaths = imported,
            existingPathValidator = { false }
        )

        assertEquals("Restored Co", reconciled.companyName)
        assertEquals(HeaderEmblemChoice.CUSTOM, reconciled.headerEmblemChoice)
        assertEquals("/new/files/branding/header_emblem_1.png", reconciled.customHeaderEmblemPath)
        assertEquals(PdfLogoMode.CUSTOM_PDF_IMAGE, reconciled.pdfLogoMode)
        assertEquals("/new/files/branding/pdf_logo_2.png", reconciled.customPdfLogoPath)

        // Missing images safely fall back to built-in choices rather than broken CUSTOM slots
        assertEquals(BrandLogoChoice.DEFAULT_LOCKUP, reconciled.brandLogoChoice)
        assertEquals("", reconciled.customBrandLogoPath)
        assertEquals(HeroBackdropChoice.DEFAULT_BLUEPRINT, reconciled.heroBackdropChoice)
        assertEquals("", reconciled.customBackdropPath)
        assertEquals(MascotSlotChoice.STANDING_MASCOT, reconciled.mascotChoice)
        assertEquals("", reconciled.customMascotPath)
    }
}
