package com.example.domain

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Base64
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.R
import com.example.model.AppSettings
import com.example.model.DocumentType
import com.example.model.EstimateDraft
import com.example.model.PriceBookItem
import com.example.model.TakeoffResult

object EstimatePrintHelper {

    fun printEstimate(
        context: Context,
        draft: EstimateDraft,
        takeoff: TakeoffResult,
        priceBook: List<PriceBookItem>,
        settings: AppSettings = AppSettings()
    ) {
        val logoBase64 = loadLogoBase64(context)
        val html = buildEstimateHtml(draft, takeoff, priceBook, settings, logoBase64)
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false

            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
                val docLabel = draft.docType.title
                val titleSlug = (draft.project.ifBlank { draft.customer }.ifBlank { "THDR_$docLabel" })
                val jobName = "${settings.companyName.ifBlank { "House DR" }} - $docLabel - $titleSlug"
                val adapter = webView.createPrintDocumentAdapter(jobName)
                printManager.print(jobName, adapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    private fun esc(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    private fun loadLogoBase64(context: Context): String? = try {
        context.resources.openRawResource(R.drawable.thdr_logo).use { stream ->
            Base64.encodeToString(stream.readBytes(), Base64.NO_WRAP)
        }
    } catch (_: Exception) {
        null
    }

    private fun buildEstimateHtml(
        draft: EstimateDraft,
        takeoff: TakeoffResult,
        priceBook: List<PriceBookItem>,
        settings: AppSettings,
        logoBase64: String?
    ): String {
        val currency = settings.currencySymbol
        val roomsHtml = if (draft.rooms.isEmpty()) {
            "<p>No rooms or work areas added.</p>"
        } else {
            draft.rooms.joinToString("\n") { room ->
                val d = EstimatorEngine.computeDimensions(room)
                val servicesHtml = if (room.services.isEmpty()) {
                    "<div class='muted'>No services selected.</div>"
                } else {
                    room.services.joinToString("\n") { svc ->
                        val sel = room.serviceSelections[svc]
                        val items = EstimatorEngine.pbItemsForService(svc, priceBook)
                        val chosen = items.firstOrNull { it.item == sel?.selectedPbItemName } ?: items.firstOrNull()
                        val unit = sel?.effectiveUnit(chosen?.unit ?: "EA") ?: chosen?.unit ?: "EA"
                        val qty = sel?.quantity ?: 0.0
                        val rate = sel?.rate ?: 0.0
                        val total = qty * rate
                        val notes = sel?.scopeNotes.orEmpty()
                        """
                        <div class='line'>
                          <div>
                            <strong>${esc(svc)}</strong>${if (chosen != null) " — ${esc(chosen.item)}" else ""}
                            ${if (notes.isNotBlank()) "<div class='note'>${esc(notes)}</div>" else ""}
                          </div>
                          <div>${EstimatorEngine.formatQty(qty)} $unit @ ${EstimatorEngine.formatMoney(rate, currency)}</div>
                          <div class='right'><strong>${EstimatorEngine.formatMoney(total, currency)}</strong></div>
                        </div>
                        """.trimIndent()
                    }
                }
                """
                <div class='card'>
                  <h3>${esc(room.name)} <span class='badge'>${esc(room.side.label)}</span></h3>
                  <div class='meta'>
                    ${EstimatorEngine.formatQty(room.lengthFt)} × ${EstimatorEngine.formatQty(room.widthFt)} × ${EstimatorEngine.formatQty(room.heightFt)} FT ·
                    Floor ${String.format("%.1f", d.floor)} SF · Walls ${String.format("%.1f", d.wall)} SF · Perim ${String.format("%.1f", d.perim)} LF
                    ${if (room.condition.isNotBlank()) " · Condition: ${esc(room.condition)}" else ""}
                  </div>
                  $servicesHtml
                  ${if (room.roomNotes.isNotBlank()) "<div class='note'>Notes: ${esc(room.roomNotes)}</div>" else ""}
                </div>
                """.trimIndent()
            }
        }

        val materialsHtml = if (takeoff.materials.isEmpty()) {
            "<p class='muted'>No materials calculated.</p>"
        } else {
            takeoff.materials.entries.joinToString("\n") { (name, qty) ->
                "<div class='line'><span>${esc(name)}</span><span></span><strong class='right'>${EstimatorEngine.formatQty(qty)}</strong></div>"
            }
        }

        val discount = draft.discountText.toDoubleOrNull() ?: 0.0
        val tax = draft.taxText.toDoubleOrNull() ?: 0.0
        val isProposal = draft.docType == DocumentType.PROPOSAL
        val docHeading = if (isProposal) "CLIENT PROPOSAL" else "PROJECT ESTIMATE"
        val depositPct = draft.depositPercentText.toDoubleOrNull() ?: settings.defaultProposalDepositPct
        val depositAmt = takeoff.grandTotal * (depositPct.coerceIn(0.0, 100.0) / 100.0)

        val contactLines = listOfNotNull(
            settings.companyPhone.takeIf { it.isNotBlank() },
            settings.companyEmail.takeIf { it.isNotBlank() },
            settings.companyAddress.takeIf { it.isNotBlank() },
            settings.companyLicense.takeIf { it.isNotBlank() }?.let { "Lic: $it" }
        ).joinToString(" · ")

        val termsBlock = if (isProposal) {
            val terms = draft.termsText.ifBlank { settings.defaultProposalTerms }
            """
            <div class="card">
              <h3>Proposal Terms &amp; Deposit</h3>
              <div class="meta">Status: <strong>${esc(draft.proposalStatus.label)}</strong>${if (draft.validUntil.isNotBlank()) " · Valid Until: <strong>${esc(draft.validUntil)}</strong>" else ""} · Deposit (${EstimatorEngine.formatQty(depositPct)}%): <strong>${EstimatorEngine.formatMoney(depositAmt, currency)}</strong></div>
              <p class="note">${esc(terms)}</p>
              ${
                if (settings.includeSignatureBlockOnProposal) {
                    """
                    <div style="display:flex;gap:32px;margin-top:28px;">
                      <div style="flex:1;border-top:1px solid #4d6278;padding-top:6px;font-size:12px;">Client Signature &amp; Date</div>
                      <div style="flex:1;border-top:1px solid #4d6278;padding-top:6px;font-size:12px;">Contractor Approval &amp; Date</div>
                    </div>
                    """.trimIndent()
                } else ""
            }
            </div>
            """.trimIndent()
        } else ""

        return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8" />
          <style>
            body { font-family: sans-serif; color: #1a2433; margin: 24px; line-height: 1.45; }
            .header { border-bottom: 3px solid #0A68D3; padding-bottom: 12px; margin-bottom: 18px; display: flex; justify-content: space-between; }
            .brand { font-size: 22px; font-weight: 800; color: #0A68D3; }
            .sub { color: #52657b; font-size: 12px; }
            .card { border: 1px solid #d2dde8; border-radius: 10px; padding: 14px; margin-bottom: 14px; }
            .meta { font-size: 12px; color: #4d6278; margin-bottom: 8px; }
            .badge { font-size: 10px; background: #e8f1fb; color: #0A68D3; padding: 2px 7px; border-radius: 99px; }
            .line { display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 8px; padding: 6px 0; border-bottom: 1px solid #edf2f7; font-size: 13px; }
            .right { text-align: right; }
            .note { font-size: 12px; color: #52657b; margin-top: 3px; }
            .total-box { background: #eef5fc; border: 1px solid #bdd4ee; border-radius: 10px; padding: 16px; margin-top: 16px; }
            .grand { font-size: 26px; font-weight: 800; color: #0A68D3; margin: 4px 0 10px; }
          </style>
        </head>
        <body>
          <div class="header">
            <div style="display:flex;align-items:center;gap:12px;">
              ${if (logoBase64 != null) "<img src='data:image/png;base64,$logoBase64' style='width:48px;height:48px;border-radius:8px;' />" else ""}
              <div>
                <div class="brand">${esc(settings.companyName.ifBlank { "EZ ESTIMATOR" })}</div>
                <div class="sub">${esc(settings.companyTagline.ifBlank { docHeading })}</div>
                ${if (contactLines.isNotBlank()) "<div class='sub'>${esc(contactLines)}</div>" else ""}
              </div>
            </div>
            <div class="sub" style="text-align:right">
              <div><strong>$docHeading</strong></div>
              <div><strong>Date:</strong> ${esc(draft.date)}</div>
              <div><strong>Customer:</strong> ${esc(draft.customer.ifBlank { "Customer" })}</div>
              <div><strong>Project:</strong> ${esc(draft.project.ifBlank { docHeading })}</div>
            </div>
          </div>

          <h2>Rooms &amp; Work Areas</h2>
          $roomsHtml

          <h2>Materials Needed</h2>
          <div class="card">$materialsHtml</div>

          <div class="total-box">
            <div class="sub">$docHeading TOTAL</div>
            <div class="grand">${EstimatorEngine.formatMoney(takeoff.grandTotal, currency)}</div>
            <div class="line"><span>Services Subtotal</span><span></span><strong class="right">${EstimatorEngine.formatMoney(takeoff.serviceTotal, currency)}</strong></div>
            <div class="line"><span>Discount</span><span></span><strong class="right">-${EstimatorEngine.formatMoney(discount, currency)}</strong></div>
            <div class="line"><span>Tax (${EstimatorEngine.formatQty(tax)}%)</span><span></span><strong class="right">Included in Total</strong></div>
          </div>
          $termsBlock
          <p class="note">Accuracy check: unknown conditions, unverified dimensions, structural work, and concealed damage should be verified on site before final pricing.</p>
        </body>
        </html>
        """.trimIndent()
    }
}
