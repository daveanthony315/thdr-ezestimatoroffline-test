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
import com.example.model.Customer
import com.example.model.DocumentType
import com.example.model.EstimateDraft
import com.example.model.PriceBookItem
import com.example.model.TakeoffResult
import java.util.Locale
import kotlin.math.max

object EstimatePrintHelper {

    fun printEstimate(
        context: Context,
        draft: EstimateDraft,
        takeoff: TakeoffResult,
        priceBook: List<PriceBookItem>,
        settings: AppSettings = AppSettings(),
        customer: Customer? = null
    ) {
        val emblemBase64 = loadDrawableBase64(context, R.drawable.thdr_logo)
        val fullLogoBase64 = loadDrawableBase64(context, R.drawable.thdr_brand_logo)
        val html = buildEstimateHtml(
            draft = draft,
            takeoff = takeoff,
            priceBook = priceBook,
            settings = settings,
            customer = customer,
            emblemBase64 = emblemBase64,
            fullLogoBase64 = fullLogoBase64
        )
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

    private fun loadDrawableBase64(context: Context, resId: Int): String? = try {
        context.resources.openRawResource(resId).use { stream ->
            Base64.encodeToString(stream.readBytes(), Base64.NO_WRAP)
        }
    } catch (_: Exception) {
        null
    }

    private fun buildDocumentNumber(draft: EstimateDraft): String {
        val prefix = if (draft.docType == DocumentType.PROPOSAL) "PROP" else "EST"
        val yearPart = draft.date.take(4).takeIf { it.all(Char::isDigit) && it.length == 4 } ?: "2026"
        val idPart = draft.id?.let { String.format(Locale.US, "%03d", it) } ?: "DRAFT"
        return "THDR-$prefix-$yearPart-$idPart"
    }

    private fun buildCornerPatternSvg(color: String, topRight: Boolean): String {
        val cols = 5
        val rows = 4
        val spacing = 22
        val shapes = buildString {
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val weight = if (topRight) {
                        (c + (rows - 1 - r)) / 7.0
                    } else {
                        ((cols - 1 - c) + r) / 7.0
                    }
                    val half = 2.6 + weight * 4.4
                    val stroke = 1.6 + weight * 2.1
                    val cx = 14 + c * spacing
                    val cy = 14 + r * spacing
                    append(
                        "<path d='M${String.format(Locale.US, "%.1f", cx - half)},${String.format(Locale.US, "%.1f", cy - half)} " +
                            "L${String.format(Locale.US, "%.1f", cx + half)},${String.format(Locale.US, "%.1f", cy + half)} " +
                            "M${String.format(Locale.US, "%.1f", cx + half)},${String.format(Locale.US, "%.1f", cy - half)} " +
                            "L${String.format(Locale.US, "%.1f", cx - half)},${String.format(Locale.US, "%.1f", cy + half)}' " +
                            "stroke='$color' stroke-width='${String.format(Locale.US, "%.1f", stroke)}' stroke-linecap='round' />"
                    )
                }
            }
        }
        return "<svg width='116' height='94' viewBox='0 0 116 94' fill='none' xmlns='http://www.w3.org/2000/svg'>$shapes</svg>"
    }

    internal fun buildEstimateHtml(
        draft: EstimateDraft,
        takeoff: TakeoffResult,
        priceBook: List<PriceBookItem>,
        settings: AppSettings,
        customer: Customer? = null,
        emblemBase64: String? = null,
        fullLogoBase64: String? = null
    ): String {
        val currency = settings.currencySymbol
        val isProposal = draft.docType == DocumentType.PROPOSAL
        val docWord = if (isProposal) "PROPOSAL" else "ESTIMATE"
        val docHeading = if (isProposal) "CLIENT PROPOSAL" else "PROJECT ESTIMATE"
        val docNumber = buildDocumentNumber(draft)
        val companyHeading = settings.companyName.ifBlank { "THE HOME IMPROVEMENT" }.uppercase(Locale.US)

        val customerName = draft.customer.ifBlank { customer?.name.orEmpty() }.ifBlank { "Valued Client" }
        val customerCompany = customer?.company.orEmpty().ifBlank { "Residential Property" }
        val customerEmail = customer?.email.orEmpty().ifBlank { "—" }
        val customerPhone = customer?.phone.orEmpty()
        val customerAddress = customer?.address.orEmpty().ifBlank { "—" }

        val projectSummaryHtml = buildString {
            val title = draft.project.ifBlank { "${draft.estimateSide.label} Remodeling & Home Improvement" }
            append("<div class='proj-title'>${esc(title)}</div>")
            if (draft.rooms.isEmpty()) {
                append("<div class='proj-sub'>No rooms or work areas added yet.</div>")
            } else {
                append("<div class='proj-rooms'>")
                draft.rooms.forEach { room ->
                    val d = EstimatorEngine.computeDimensions(room)
                    val condText = if (room.condition.isNotBlank()) " · Condition: ${esc(room.condition)}" else ""
                    val notesText = if (room.roomNotes.isNotBlank()) " — <em>${esc(room.roomNotes)}</em>" else ""
                    append(
                        "<div class='proj-room-line'><strong>${esc(room.name)}</strong> (${esc(room.side.label)}): " +
                            "${EstimatorEngine.formatQty(room.lengthFt)} × ${EstimatorEngine.formatQty(room.widthFt)} × ${EstimatorEngine.formatQty(room.heightFt)} FT " +
                            "· Floor ${String.format(Locale.US, "%.1f", d.floor)} SF · Walls ${String.format(Locale.US, "%.1f", d.wall)} SF$condText$notesText</div>"
                    )
                }
                append("</div>")
            }
        }

        val serviceRowsHtml = buildString {
            var count = 0
            draft.rooms.forEach { room ->
                room.services.forEach { svc ->
                    count++
                    val sel = room.serviceSelections[svc]
                    val items = EstimatorEngine.pbItemsForService(svc, priceBook)
                    val chosen = items.firstOrNull { it.item == sel?.selectedPbItemName } ?: items.firstOrNull()
                    val unit = sel?.effectiveUnit(chosen?.unit ?: "EA") ?: chosen?.unit ?: "EA"
                    val qty = sel?.quantity ?: 0.0
                    val rate = sel?.rate ?: 0.0
                    val total = qty * rate
                    val subcatLine = sel?.selectedSubcategories?.takeIf { it.isNotEmpty() }?.joinToString(", ").orEmpty()
                    val scopeNotes = sel?.scopeNotes.orEmpty()
                    val itemDetail = buildString {
                        append("<strong>${esc(room.name)} — ${esc(svc)}</strong>")
                        if (chosen != null) append(" <span class='item-pb'>(${esc(chosen.item)})</span>")
                        if (subcatLine.isNotBlank()) append("<div class='item-note'>Scope: ${esc(subcatLine)}</div>")
                        if (scopeNotes.isNotBlank()) append("<div class='item-note'>${esc(scopeNotes)}</div>")
                    }
                    append(
                        """
                        <tr>
                          <td>$itemDetail</td>
                          <td class='center'>${EstimatorEngine.formatQty(qty)} ${esc(unit)}</td>
                          <td class='right'>${EstimatorEngine.formatMoney(rate, currency)}</td>
                          <td class='right'><strong>${EstimatorEngine.formatMoney(total, currency)}</strong></td>
                        </tr>
                        """.trimIndent()
                    )
                }
            }
            if (count == 0) {
                append(
                    """
                    <tr>
                      <td colspan='4' class='center muted-cell'>No billable services added to rooms yet.</td>
                    </tr>
                    """.trimIndent()
                )
            }
        }

        val materialsSectionHtml = if (takeoff.materials.isNotEmpty()) {
            val matRows = takeoff.materials.entries.joinToString("\n") { (name, qty) ->
                """
                <tr>
                  <td>${esc(name)}</td>
                  <td class='right'><strong>${EstimatorEngine.formatQty(qty)}</strong></td>
                </tr>
                """.trimIndent()
            }
            """
            <table class='grid-table box-section' style='margin-top:12px;'>
              <thead>
                <tr>
                  <th>Materials Takeoff Reference</th>
                  <th style='width:120px;text-align:right;'>Estimated Qty</th>
                </tr>
              </thead>
              <tbody>
                $matRows
              </tbody>
            </table>
            """.trimIndent()
        } else ""

        val discount = max(0.0, draft.discountText.toDoubleOrNull() ?: 0.0)
        val taxPct = max(0.0, draft.taxText.toDoubleOrNull() ?: 0.0)
        val subtotal = takeoff.serviceTotal
        val taxableBase = max(0.0, subtotal - discount)
        val taxAmount = taxableBase * (taxPct / 100.0)
        val depositPct = draft.depositPercentText.toDoubleOrNull() ?: settings.defaultProposalDepositPct
        val depositAmt = takeoff.grandTotal * (depositPct.coerceIn(0.0, 100.0) / 100.0)

        val notesHtml = if (isProposal) {
            val terms = draft.termsText.ifBlank { settings.defaultProposalTerms }
            buildString {
                append("<strong>Notes &amp; Proposal Terms</strong>")
                append("<p>${esc(terms)}</p>")
                append(
                    "<p><strong>Status:</strong> ${esc(draft.proposalStatus.label)}" +
                        (if (draft.validUntil.isNotBlank()) " · <strong>Valid Until:</strong> ${esc(draft.validUntil)}" else "") +
                        " · <strong>Deposit (${EstimatorEngine.formatQty(depositPct)}%):</strong> ${EstimatorEngine.formatMoney(depositAmt, currency)}</p>"
                )
            }
        } else {
            """
            <strong>Notes</strong>
            <p>This estimate is valid for ${settings.defaultProposalValidDays} days from the date issued. A ${EstimatorEngine.formatQty(depositPct)}% deposit is required to schedule work.</p>
            <p>Accuracy check: unknown conditions, unverified dimensions, structural work, and concealed damage should be verified on site before final pricing.</p>
            """.trimIndent()
        }

        val emailLine = settings.companyEmail.ifBlank { "info@thehousedr.com" }
        val phoneLine = settings.companyPhone.ifBlank { "Call office for scheduling" }
        val addressLine = settings.companyAddress.ifBlank { settings.companyTagline.ifBlank { "Interior & Exterior Remodeling" } }
        val licenseLine = settings.companyLicense

        val showSignatures = !isProposal || settings.includeSignatureBlockOnProposal
        val signatureHtml = if (showSignatures) {
            """
            <div class='signatures'>
              <div class='sig-col'>
                <div class='sig-role'>Contractor</div>
                <div class='sig-line'></div>
                <div class='sig-name'>${esc(settings.companyName.ifBlank { "House DR" })}</div>
                <div class='sig-org'>${esc(settings.companyTagline.ifBlank { "Authorized Representative" })}</div>
              </div>
              <div class='sig-col'>
                <div class='sig-role'>Client</div>
                <div class='sig-line'></div>
                <div class='sig-name'>${esc(customerName)}</div>
                <div class='sig-org'>${esc(customerCompany)}</div>
              </div>
            </div>
            """.trimIndent()
        } else ""

        val topRightPattern = buildCornerPatternSvg("#5E8FB9", topRight = true)
        val bottomLeftPattern = buildCornerPatternSvg("#FFFFFF", topRight = false)

        return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8" />
          <style>
            @page { size: letter; margin: 0; }
            * { -webkit-print-color-adjust: exact; print-color-adjust: exact; box-sizing: border-box; }
            html, body { margin: 0; padding: 0; font-family: 'Helvetica Neue', Arial, sans-serif; color: #1D2B3A; background: #E6E7EA; }
            .page-shell {
              position: relative;
              min-height: 10.9in;
              padding: 26px 28px 34px 28px;
              background: linear-gradient(to bottom, #E6E7EA 0%, #E6E7EA 56%, #2F5D8A 56%, #2F5D8A 100%);
              overflow: hidden;
            }
            .corner-tr { position: absolute; top: 10px; right: 10px; z-index: 1; }
            .corner-bl { position: absolute; bottom: 10px; left: 10px; z-index: 1; }
            .content-layer { position: relative; z-index: 2; }
            .top-row {
              display: flex;
              justify-content: space-between;
              align-items: flex-start;
              gap: 16px;
              padding-right: 118px;
              margin-bottom: 14px;
            }
            .brand-badge {
              display: inline-flex;
              align-items: center;
              gap: 10px;
              background: #22466A;
              border: 2px solid #F5832B;
              border-radius: 8px;
              padding: 7px 12px;
              margin-bottom: 12px;
            }
            .title-stack { line-height: 1.02; }
            .title-company { font-size: 25px; font-weight: 900; color: #1D2B3A; letter-spacing: 0.4px; }
            .title-doc { font-size: 25px; font-weight: 500; color: #2F5D8A; letter-spacing: 0.8px; margin-top: 2px; }
            .meta-col { min-width: 170px; padding-top: 8px; }
            .meta-rule { width: 92px; height: 3px; background: #F5832B; margin-bottom: 14px; border-radius: 2px; }
            .meta-label { font-size: 11px; font-weight: 700; color: #1D2B3A; margin-top: 8px; }
            .meta-val { font-size: 11.5px; color: #4A4A4A; margin-top: 2px; }
            .section-pill {
              background: #2F5D8A;
              color: #FFFFFF;
              font-size: 13px;
              font-weight: 700;
              padding: 7px 16px;
              border-radius: 99px;
              border-left: 5px solid #F5832B;
              margin-bottom: 12px;
              letter-spacing: 0.2px;
            }
            .cust-grid {
              display: grid;
              grid-template-columns: 1fr 1fr;
              gap: 16px;
              margin-bottom: 16px;
            }
            .kv-table { width: 100%; border-collapse: collapse; font-size: 11.5px; background: #FFFFFF; }
            .kv-table td { border: 1px solid #5E8FB9; padding: 5px 8px; vertical-align: middle; }
            .kv-label { width: 84px; background: #CDE3F5; font-weight: 700; color: #1D2B3A; }
            .kv-val { color: #4A4A4A; background: #FFFFFF; }
            .main-card {
              background: #FFFFFF;
              border: 1.5px solid #5E8FB9;
              border-radius: 14px;
              padding: 18px 20px;
              box-shadow: 0 3px 10px rgba(47, 93, 138, 0.12);
            }
            .grid-table { width: 100%; border-collapse: collapse; font-size: 11.5px; }
            .grid-table th {
              background: #CDE3F5;
              color: #1D2B3A;
              font-weight: 700;
              text-align: left;
              border: 1px solid #5E8FB9;
              padding: 6px 9px;
            }
            .grid-table td {
              border: 1px solid #5E8FB9;
              padding: 6px 9px;
              vertical-align: top;
              color: #4A4A4A;
            }
            .grid-table tr { break-inside: avoid; }
            .box-section { break-inside: avoid; }
            .proj-title { font-weight: 700; color: #1D2B3A; margin-bottom: 4px; font-size: 12px; }
            .proj-sub { color: #4A4A4A; font-size: 11px; }
            .proj-rooms { display: flex; flex-direction: column; gap: 3px; }
            .proj-room-line { font-size: 11px; color: #4A4A4A; line-height: 1.35; }
            .item-pb { color: #5E8FB9; font-size: 10.5px; }
            .item-note { color: #4A4A4A; font-size: 10.5px; margin-top: 2px; }
            .muted-cell { color: #4A4A4A; padding: 12px; }
            .center { text-align: center; }
            .right { text-align: right; }
            .bottom-split {
              display: flex;
              justify-content: space-between;
              align-items: flex-start;
              gap: 18px;
              margin-top: 14px;
              break-inside: avoid;
            }
            .notes-col { flex: 1.15; font-size: 11px; color: #4A4A4A; line-height: 1.45; }
            .notes-col strong { font-size: 12px; color: #1D2B3A; }
            .notes-col p { margin: 5px 0; }
            .totals-col { flex: 0.85; min-width: 220px; }
            .totals-table { width: 100%; border-collapse: collapse; font-size: 11.5px; }
            .totals-table td { border: 1px solid #5E8FB9; padding: 6px 9px; }
            .totals-label { background: #CDE3F5; font-weight: 700; color: #1D2B3A; width: 52%; }
            .totals-val { text-align: right; color: #1D2B3A; font-weight: 600; background: #FFFFFF; }
            .totals-grand td { font-weight: 800; font-size: 12.5px; border-top: 2px solid #F5832B; }
            .contact-sign-row {
              display: flex;
              justify-content: space-between;
              align-items: flex-end;
              gap: 18px;
              margin-top: 22px;
              padding-top: 10px;
              border-top: 1px solid #CDE3F5;
              break-inside: avoid;
            }
            .contact-col { flex: 1; }
            .contact-heading { font-size: 13px; font-weight: 800; color: #2F5D8A; margin-bottom: 8px; }
            .contact-item { display: flex; align-items: center; gap: 8px; font-size: 11px; color: #4A4A4A; margin-bottom: 5px; }
            .signatures { display: flex; gap: 26px; flex: 1; justify-content: flex-end; }
            .sig-col { width: 145px; text-align: center; }
            .sig-role { font-size: 12px; font-weight: 800; color: #2F5D8A; margin-bottom: 24px; }
            .sig-line { border-bottom: 1.5px solid #5E8FB9; margin-bottom: 5px; }
            .sig-name { font-size: 11px; font-weight: 700; color: #1D2B3A; }
            .sig-org { font-size: 10px; color: #4A4A4A; }
          </style>
        </head>
        <body>
          <div class="page-shell">
            <div class="corner-tr">$topRightPattern</div>
            <div class="corner-bl">$bottomLeftPattern</div>

            <div class="content-layer">
              <div class="top-row">
                <div>
                  ${
                    if (emblemBase64 != null || fullLogoBase64 != null) {
                        buildString {
                            append("<div class='brand-badge'>")
                            if (emblemBase64 != null) {
                                append("<img src='data:image/png;base64,$emblemBase64' style='width:38px;height:38px;border-radius:5px;object-fit:contain;' />")
                            }
                            if (fullLogoBase64 != null) {
                                append("<img src='data:image/png;base64,$fullLogoBase64' style='height:38px;width:auto;max-width:190px;object-fit:contain;' />")
                            }
                            append("</div>")
                        }
                    } else {
                        "<div class='brand-badge' style='color:#FFFFFF;font-size:12px;font-weight:700;'>${esc(companyHeading)} · $docWord</div>"
                    }
                  }
                  <div class="title-stack">
                    <div class="title-company">$companyHeading</div>
                    <div class="title-doc">$docWord</div>
                  </div>
                </div>

                <div class="meta-col">
                  <div class="meta-rule"></div>
                  <div class="meta-label">Date</div>
                  <div class="meta-val">${esc(draft.date.ifBlank { "—" })}</div>
                  <div class="meta-label">Number</div>
                  <div class="meta-val">${esc(docNumber)}</div>
                  ${
                    if (isProposal && draft.validUntil.isNotBlank()) {
                        "<div class='meta-label'>Valid Until</div><div class='meta-val'>${esc(draft.validUntil)}</div>"
                    } else ""
                  }
                </div>
              </div>

              <div class="section-pill">Customer Information</div>

              <div class="cust-grid">
                <table class="kv-table">
                  <tr>
                    <td class="kv-label">Name</td>
                    <td class="kv-val">${esc(customerName)}</td>
                  </tr>
                  <tr>
                    <td class="kv-label">Company</td>
                    <td class="kv-val">${esc(customerCompany)}</td>
                  </tr>
                  ${
                    if (customerPhone.isNotBlank()) {
                        "<tr><td class='kv-label'>Phone</td><td class='kv-val'>${esc(customerPhone)}</td></tr>"
                    } else ""
                  }
                </table>
                <table class="kv-table">
                  <tr>
                    <td class="kv-label">Email</td>
                    <td class="kv-val">${esc(customerEmail)}</td>
                  </tr>
                  <tr>
                    <td class="kv-label">Address</td>
                    <td class="kv-val">${esc(customerAddress)}</td>
                  </tr>
                </table>
              </div>

              <div class="main-card">
                <table class="grid-table box-section" style="margin-bottom:14px;">
                  <thead>
                    <tr><th>Project Description</th></tr>
                  </thead>
                  <tbody>
                    <tr><td>$projectSummaryHtml</td></tr>
                  </tbody>
                </table>

                <table class="grid-table">
                  <thead>
                    <tr>
                      <th>Item/Service Description</th>
                      <th style="width:92px;text-align:center;">Quantity</th>
                      <th style="width:94px;text-align:right;">Unit Price</th>
                      <th style="width:102px;text-align:right;">Total Price</th>
                    </tr>
                  </thead>
                  <tbody>
                    $serviceRowsHtml
                  </tbody>
                </table>

                $materialsSectionHtml

                <div class="bottom-split">
                  <div class="notes-col">
                    $notesHtml
                  </div>
                  <div class="totals-col">
                    <table class="totals-table">
                      <tr>
                        <td class="totals-label">Subtotal</td>
                        <td class="totals-val">${EstimatorEngine.formatMoney(subtotal, currency)}</td>
                      </tr>
                      ${
                        if (discount > 0.0) {
                            "<tr><td class='totals-label'>Discount</td><td class='totals-val'>-${EstimatorEngine.formatMoney(discount, currency)}</td></tr>"
                        } else ""
                      }
                      <tr>
                        <td class="totals-label">Tax (${EstimatorEngine.formatQty(taxPct)}%)</td>
                        <td class="totals-val">${EstimatorEngine.formatMoney(taxAmount, currency)}</td>
                      </tr>
                      <tr class="totals-grand">
                        <td class="totals-label">Total ${if (isProposal) "Proposal" else "Estimate"}</td>
                        <td class="totals-val">${EstimatorEngine.formatMoney(takeoff.grandTotal, currency)}</td>
                      </tr>
                    </table>
                  </div>
                </div>

                <div class="contact-sign-row">
                  <div class="contact-col">
                    <div class="contact-heading">Contact Us</div>
                    <div class="contact-item">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="#2F5D8A" xmlns="http://www.w3.org/2000/svg"><path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4-8 5-8-5V6l8 5 8-5v2z"/></svg>
                      <span>${esc(emailLine)}</span>
                    </div>
                    <div class="contact-item">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="#2F5D8A" xmlns="http://www.w3.org/2000/svg"><path d="M6.62 10.79a15.05 15.05 0 0 0 6.59 6.59l2.2-2.2a1 1 0 0 1 1.01-.24c1.12.37 2.33.57 3.58.57.55 0 1 .45 1 1V20c0 .55-.45 1-1 1C10.61 21 3 13.39 3 4c0-.55.45-1 1-1h3.5c.55 0 1 .45 1 1 0 1.25.2 2.46.57 3.58.11.35.03.74-.25 1.01l-2.2 2.2z"/></svg>
                      <span>${esc(phoneLine)}</span>
                    </div>
                    <div class="contact-item">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="#2F5D8A" xmlns="http://www.w3.org/2000/svg"><path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5z"/></svg>
                      <span>${esc(addressLine)}</span>
                    </div>
                    ${
                      if (licenseLine.isNotBlank()) {
                          "<div class='contact-item'><svg width='13' height='13' viewBox='0 0 24 24' fill='#2F5D8A' xmlns='http://www.w3.org/2000/svg'><path d='M12 1 3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-1 15-4-4 1.41-1.41L11 13.17l5.59-5.59L18 9l-7 7z'/></svg><span>Lic: ${esc(licenseLine)}</span></div>"
                      } else ""
                    }
                  </div>
                  $signatureHtml
                </div>
              </div>
            </div>
          </div>
        </body>
        </html>
        """.trimIndent()
    }
}
