package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AppSettings
import com.example.ui.components.ThdrCard
import com.example.ui.components.ThdrPrimaryButton
import com.example.ui.components.ThdrSecondaryButton
import com.example.ui.components.ThdrSubCard
import com.example.ui.theme.LocalThdrPalette

data class PrivacySection(
    val title: String,
    val body: String
)

object PrivacyPolicyDocument {
    const val EFFECTIVE_DATE = "November 24, 2026"

    fun sections(contactEmail: String): List<PrivacySection> = listOf(
        PrivacySection(
            title = "1. Local On-Device Storage & Zero Cloud Collection",
            body = "THDR Estimator operates completely offline-first. All app data is stored locally on your device inside the app's private Room SQLite database (thdr_estimator_db). We do not operate remote accounts, collect analytics or advertising identifiers, or transmit your business or client records to external servers."
        ),
        PrivacySection(
            title = "2. Customer Information",
            body = "Customer records you enter—including names, company names, phone numbers, email addresses, street addresses, and job notes—remain strictly on your device. Customer details are used only to prefill estimates and proposals or appear inside printable/PDF documents when you choose to generate one."
        ),
        PrivacySection(
            title = "3. Estimates, Proposals & Business Details",
            body = "Room dimensions, service scopes, Price Book rates, material takeoffs, taxes, discounts, proposal terms, and your company header details are saved locally on your device. When you tap Print / Save PDF, Android renders the document locally through the system Print Manager; we never receive or store a copy."
        ),
        PrivacySection(
            title = "4. Data Retention",
            body = "Your saved estimates, proposals, customer records, Price Book items, and preferences remain stored locally on your device until you edit them, delete them inside the app, reset settings, clear app storage, or uninstall THDR Estimator."
        ),
        PrivacySection(
            title = "5. Device Backups & Manual Exports",
            body = "To protect client contact details and pricing proposals, automatic Android cloud backups (allowBackup=\"false\") and automated device-to-device transfers are disabled via Android backup and data extraction rules excluding thdr_estimator_db. Data leaves your device only when you manually export a Price Book JSON backup or export/share a PDF through Android's system file picker or print dialog."
        ),
        PrivacySection(
            title = "6. Data Deletion & User Control",
            body = "You have full control over your data at all times: delete individual estimates, proposals, customers, or Price Book items directly in the app; reset customization settings in Settings; or remove all app data permanently via Android Settings > Apps > THDR Estimator > Storage > Clear Storage, or by uninstalling the app."
        ),
        PrivacySection(
            title = "7. Contact & Policy Updates",
            body = "If you have questions about this Privacy Policy or how THDR Estimator handles data on your device, contact $contactEmail. Any future policy updates will be reflected inside this screen and at the matching published web URL listed in Settings."
        )
    )

    fun buildPlainText(companyName: String, contactEmail: String): String = buildString {
        appendLine("PRIVACY POLICY — THDR ESTIMATOR ($companyName)")
        appendLine("Effective Date: $EFFECTIVE_DATE")
        appendLine()
        sections(contactEmail).forEach { sec ->
            appendLine(sec.title)
            appendLine(sec.body)
            appendLine()
        }
    }

    fun buildStandaloneHtml(companyName: String, contactEmail: String): String {
        val safeCompany = esc(companyName)
        val safeEmail = esc(contactEmail)
        val bodyHtml = sections(contactEmail).joinToString("\n") { sec ->
            "<section><h2>${esc(sec.title)}</h2><p>${esc(sec.body)}</p></section>"
        }
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8" />
              <meta name="viewport" content="width=device-width, initial-scale=1.0" />
              <title>Privacy Policy — THDR Estimator</title>
              <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; color: #16202F; background: #F4F7FB; margin: 0; padding: 24px; line-height: 1.6; }
                main { max-width: 780px; margin: 0 auto; background: #FFFFFF; border: 1px solid #D5E0EE; border-radius: 14px; padding: 28px; box-shadow: 0 4px 16px rgba(16,24,39,0.06); }
                h1 { margin: 0 0 6px; font-size: 26px; color: #004299; }
                .meta { color: #4D6278; font-size: 14px; margin-bottom: 22px; border-bottom: 2px solid #E2E8F0; padding-bottom: 14px; }
                section { margin-bottom: 18px; }
                h2 { font-size: 17px; color: #0F172A; margin: 0 0 6px; }
                p { margin: 0; color: #334155; font-size: 15px; }
                footer { margin-top: 24px; padding-top: 14px; border-top: 1px solid #E2E8F0; font-size: 13px; color: #52657B; }
              </style>
            </head>
            <body>
              <main>
                <h1>Privacy Policy — THDR Estimator</h1>
                <div class="meta">Publisher: $safeCompany · Effective Date: $EFFECTIVE_DATE · Contact: <a href="mailto:$safeEmail">$safeEmail</a></div>
                $bodyHtml
                <footer>Published for THDR Estimator (com.aistudio.thdr.estimator.kxmqzp). This web policy mirrors the in-app Privacy Policy screen.</footer>
              </main>
            </body>
            </html>
        """.trimIndent()
    }

    private fun esc(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}

fun isValidHttpUrl(raw: String): Boolean {
    val trimmed = raw.trim()
    return trimmed.startsWith("https://", ignoreCase = true) ||
        trimmed.startsWith("http://", ignoreCase = true)
}

fun openExternalUrl(context: Context, url: String) {
    val clean = url.trim()
    if (!isValidHttpUrl(clean)) {
        Toast.makeText(context, "Enter a valid https:// Privacy Policy URL in Settings first.", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(clean))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Unable to open browser for $clean", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun PrivacyPolicyScreen(
    settings: AppSettings,
    onBack: () -> Unit
) {
    val palette = LocalThdrPalette.current
    val context = LocalContext.current
    val companyName = settings.companyName.ifBlank { "The Home Improvement / House DR" }
    val contactEmail = settings.companyEmail.ifBlank { "thehousedrcny@gmail.com" }
    val sections = PrivacyPolicyDocument.sections(contactEmail)
    val hasValidUrl = isValidHttpUrl(settings.privacyPolicyUrl)

    val exportHtmlLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/html")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(
                        PrivacyPolicyDocument.buildStandaloneHtml(companyName, contactEmail)
                            .toByteArray(Charsets.UTF_8)
                    )
                }
                Toast.makeText(context, "Saved HTML Privacy Policy", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                Toast.makeText(context, "Could not save HTML file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .testTag("privacy_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = palette.textPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = palette.primary.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, palette.brandHighlight.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Privacy Policy",
                                tint = palette.brandHighlight,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Privacy Policy",
                                style = MaterialTheme.typography.headlineMedium,
                                color = palette.textPrimary
                            )
                            Text(
                                text = "Effective ${PrivacyPolicyDocument.EFFECTIVE_DATE} · On-Device First",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.brandHighlight
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "How THDR Estimator stores, protects, backs up, and lets you delete customer records, estimates, proposals, and local business settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                }
            }
        }

        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        sections.forEach { sec ->
                            ThdrSubCard {
                                Text(
                                    text = sec.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = palette.textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = sec.body,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Box(modifier = Modifier.widthIn(max = 980.dp)) {
                ThdrCard {
                    Text(
                        text = "Public Web Policy & Play Store Link",
                        style = MaterialTheme.typography.titleLarge,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (hasValidUrl) {
                            "Published URL: ${settings.privacyPolicyUrl.trim()}"
                        } else {
                            "No public URL configured yet. Add your hosted HTTPS Privacy Policy URL in Settings so this screen and your Google Play listing link to the exact same policy."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (hasValidUrl) {
                        ThdrPrimaryButton(
                            text = "Open Public Privacy Policy URL",
                            onClick = { openExternalUrl(context, settings.privacyPolicyUrl) },
                            testTag = "privacy_open_url_btn",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThdrSecondaryButton(
                            text = "Export HTML Policy",
                            onClick = { exportHtmlLauncher.launch("thdr-privacy-policy.html") },
                            testTag = "privacy_export_html_btn",
                            modifier = Modifier.weight(1f)
                        )
                        ThdrSecondaryButton(
                            text = "Copy Policy Text",
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(
                                    ClipData.newPlainText(
                                        "THDR Privacy Policy",
                                        PrivacyPolicyDocument.buildPlainText(companyName, contactEmail)
                                    )
                                )
                                Toast.makeText(context, "Privacy Policy copied", Toast.LENGTH_SHORT).show()
                            },
                            testTag = "privacy_copy_text_btn",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
