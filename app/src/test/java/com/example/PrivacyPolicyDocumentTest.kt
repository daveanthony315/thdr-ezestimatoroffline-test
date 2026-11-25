package com.example

import com.example.model.AppSettings
import com.example.ui.screens.PrivacyPolicyDocument
import com.example.ui.screens.isValidHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyPolicyDocumentTest {

    @Test
    fun policyCoversStorageCustomerEstimatesRetentionBackupsAndDeletion() {
        val text = PrivacyPolicyDocument.buildPlainText("The Home Improvement", "thehousedrcny@gmail.com")
        val html = PrivacyPolicyDocument.buildStandaloneHtml("The Home Improvement", "thehousedrcny@gmail.com")

        listOf(
            "thdr_estimator_db",
            "Customer",
            "Estimates",
            "Retention",
            "allowBackup",
            "Clear Storage",
            "thehousedrcny@gmail.com"
        ).forEach { keyword ->
            assertTrue("Plain text missing $keyword", text.contains(keyword))
            assertTrue("HTML missing $keyword", html.contains(keyword))
        }
        assertTrue(text.contains("allowBackup=\"false\""))
        assertTrue(html.contains("allowBackup=&quot;false&quot;"))
    }

    @Test
    fun appSettingsStoresPrivacyPolicyUrl() {
        val custom = AppSettings(privacyPolicyUrl = "https://thehousedrcny.com/privacy-policy")
        assertEquals("https://thehousedrcny.com/privacy-policy", custom.privacyPolicyUrl)
        assertEquals("", AppSettings().privacyPolicyUrl)
    }

    @Test
    fun urlValidatorOnlyPermitsHttpOrHttps() {
        assertTrue(isValidHttpUrl("https://example.com/privacy"))
        assertTrue(isValidHttpUrl("http://example.com/privacy"))
        assertFalse(isValidHttpUrl(""))
        assertFalse(isValidHttpUrl("javascript:alert(1)"))
        assertFalse(isValidHttpUrl("file:///etc/passwd"))
    }
}
