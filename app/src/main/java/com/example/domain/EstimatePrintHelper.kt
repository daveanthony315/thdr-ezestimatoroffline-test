package com.example.domain

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.print.PrintAttributes
import android.print.PrintManager
import com.example.model.AppSettings
import com.example.model.Customer
import com.example.model.DocumentType
import com.example.model.EstimateDraft
import com.example.model.PdfColorTheme
import com.example.model.PdfLogoMode
import com.example.model.PriceBookItem
import com.example.model.TakeoffResult
import java.util.Locale
import kotlin.math.max

object EstimatePrintHelper {

    internal data class ResolvedPdfPalette(
        val primaryHex: String,
        val deepHex: String,
        val steelHex: String,
        val skyHex: String,
        val softGrayHex: String,
        val accentHex: String
    )

    internal fun resolvePdfPalette(settings: AppSettings): ResolvedPdfPalette {
        val base = settings.pdfColorTheme
        val customScheme = settings.activeCustomScheme()
        if (customScheme != null &&
            (base == PdfColorTheme.MATCH_ACTIVE_THEME || base == PdfColorTheme.MATCH_APP_ACCENT)
        ) {
            val primaryHex = com.example.model.ColorHexCodec.normalizeHex(customScheme.primaryHex, "#2F5D8A")
            val deepHex = com.example.model.ColorHexCodec.darkenHex(primaryHex, 0.25f)
            val steelHex = com.example.model.ColorHexCodec.normalizeHex(customScheme.secondaryHex, "#5E8FB9")
            val skyHex = com.example.model.ColorHexCodec.lightenHex(steelHex, 0.72f)
            val softGrayHex = if (customScheme.isDark) "#E6E7EA" else {
                com.example.model.ColorHexCodec.normalizeHex(customScheme.backgroundHex, "#E6E7EA")
            }
            val accentHex = com.example.model.ColorHexCodec.normalizeHex(customScheme.accentHex, "#F5832B")
            return ResolvedPdfPalette(
                primaryHex = primaryHex,
                deepHex = deepHex,
                steelHex = steelHex,
                skyHex = skyHex,
                softGrayHex = softGrayHex,
                accentHex = accentHex
            )
        }
        if (base == PdfColorTheme.MATCH_ACTIVE_THEME) {
            val accentHex = String.format(Locale.US, "#%06X", (settings.accentColor.primaryArgb and 0xFFFFFFL))
            val starter = settings.starterSchemeFromCurrent("pdf_preview", "PDF")
            val primaryHex = com.example.model.ColorHexCodec.normalizeHex(starter.primaryHex, "#2F5D8A")
            val deepHex = com.example.model.ColorHexCodec.darkenHex(primaryHex, 0.25f)
            val steelHex = com.example.model.ColorHexCodec.normalizeHex(starter.secondaryHex, "#5E8FB9")
            return ResolvedPdfPalette(
                primaryHex = primaryHex,
                deepHex = deepHex,
                steelHex = steelHex,
                skyHex = com.example.model.ColorHexCodec.lightenHex(steelHex, 0.72f),
                softGrayHex = "#E6E7EA",
                accentHex = accentHex
            )
        }
        val accentHex = if (base == PdfColorTheme.MATCH_APP_ACCENT) {
            String.format(Locale.US, "#%06X", (settings.accentColor.primaryArgb and 0xFFFFFFL))
        } else {
            base.accentHex
        }
        return ResolvedPdfPalette(
            primaryHex = base.primaryHex,
            deepHex = base.deepHex,
            steelHex = base.steelHex,
            skyHex = base.skyHex,
            softGrayHex = base.softGrayHex,
            accentHex = accentHex
        )
    }

    fun printEstimate(
        context: Context,
        draft: EstimateDraft,
        takeoff: TakeoffResult,
        priceBook: List<PriceBookItem>,
        settings: AppSettings = AppSettings(),
        customer: Customer? = null
    ) {
        val hostContext = findActivity(context) ?: context
        val printManager = hostContext.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val docLabel = draft.docType.title
        val titleSlug = (draft.project.ifBlank { draft.customer }.ifBlank { "THDR_$docLabel" })
        val jobName = "${settings.companyName.ifBlank { "House DR" }} - $docLabel - $titleSlug"
        val attributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.NA_LETTER)
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()
        val adapter = EstimatePdfPrintAdapter(
            context = hostContext,
            jobName = jobName,
            draft = draft,
            takeoff = takeoff,
            priceBook = priceBook,
            settings = settings,
            customer = customer
        )
        printManager.print(jobName, adapter, attributes)
    }

    private tailrec fun findActivity(context: Context): Activity? = when (context) {
        is Activity -> context
        is ContextWrapper -> findActivity(context.baseContext)
        else -> null
    }

    private fun esc(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    internal fun buildDocumentNumber(
        draft: EstimateDraft,
        settings: AppSettings = AppSettings()
    ): String {
        val rootPrefix = settings.pdfDocNumberPrefix.trim().ifBlank { "THDR" }.uppercase(Locale.US)
        val prefix = if (draft.docType == DocumentType.PROPOSAL) "PROP" else "EST"
        val yearPart = draft.date.take(4).takeIf { it.all(Char::isDigit) && it.length == 4 } ?: "2026"
        val idPart = draft.id?.let { String.format(Locale.US, "%03d", it) } ?: "DRAFT"
        return "$rootPrefix-$prefix-$yearPart-$idPart"
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
        val pdfPalette = resolvePdfPalette(settings)
        val currency = settings.currencySymbol
        val isProposal = draft.docType == DocumentType.PROPOSAL
        val docWord = if (isProposal) {
            settings.pdfProposalTitle.ifBlank { "PROPOSAL" }
        } else {
            settings.pdfEstimateTitle.ifBlank { "ESTIMATE" }
        }
        val docNumber = buildDocumentNumber(draft, settings)
        val companyHeading = settings.pdfCompanyTitleOverride
            .ifBlank { settings.companyName.ifBlank { "THE HOME IMPROVEMENT" } }
            .uppercase(Locale.US)

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

        val materialsSectionHtml = if (settings.pdfShowMaterialsTable && takeoff.materials.isNotEmpty()) {
            val matTitle = settings.pdfMaterialsSectionTitle.ifBlank { "Materials Takeoff Reference" }
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
                  <th>${esc(matTitle)}</th>
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

        val notesHtml = if (!settings.pdfShowNotesSection) {
            ""
        } else if (isProposal) {
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
        } else if (settings.pdfCustomEstimateNotes.isNotBlank()) {
            """
            <strong>Notes</strong>
            <p>${esc(settings.pdfCustomEstimateNotes)}</p>
            """.trimIndent()
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

        val showSignatures = if (isProposal) {
            settings.includeSignatureBlockOnProposal
        } else {
            settings.includeSignatureBlockOnEstimate
        }
        val contractorRole = settings.pdfContractorSignatureLabel.ifBlank { "Contractor" }
        val clientRole = settings.pdfClientSignatureLabel.ifBlank { "Client" }
        val signatureHtml = if (showSignatures) {
            """
            <div class='signatures'>
              <div class='sig-col'>
                <div class='sig-role'>${esc(contractorRole)}</div>
                <div class='sig-line'></div>
                <div class='sig-name'>${esc(settings.companyName.ifBlank { "House DR" })}</div>
                <div class='sig-org'>${esc(settings.companyTagline.ifBlank { "Authorized Representative" })}</div>
              </div>
              <div class='sig-col'>
                <div class='sig-role'>${esc(clientRole)}</div>
                <div class='sig-line'></div>
                <div class='sig-name'>${esc(customerName)}</div>
                <div class='sig-org'>${esc(customerCompany)}</div>
              </div>
            </div>
            """.trimIndent()
        } else ""

        val showBadge = settings.pdfShowBrandBadge && settings.pdfLogoMode != PdfLogoMode.HIDDEN
        val badgeHtml = if (showBadge) {
            val useEmblem = settings.pdfLogoMode != PdfLogoMode.LOCKUP_ONLY &&
                settings.pdfLogoMode != PdfLogoMode.TEXT_BADGE &&
                emblemBase64 != null
            val useLockup = settings.pdfLogoMode != PdfLogoMode.EMBLEM_ONLY &&
                settings.pdfLogoMode != PdfLogoMode.TEXT_BADGE &&
                fullLogoBase64 != null
            if (useEmblem || useLockup) {
                buildString {
                    append("<div class='brand-badge'>")
                    if (useEmblem) {
                        append("<img src='data:image/png;base64,$emblemBase64' style='width:38px;height:38px;border-radius:5px;object-fit:contain;' />")
                    }
                    if (useLockup) {
                        append("<img src='data:image/png;base64,$fullLogoBase64' style='height:38px;width:auto;max-width:190px;object-fit:contain;' />")
                    }
                    append("</div>")
                }
            } else {
                "<div class='brand-badge' style='color:#FFFFFF;font-size:12px;font-weight:700;'>${esc(companyHeading)} · ${esc(docWord)}</div>"
            }
        } else ""

        val customerSectionHtml = if (settings.pdfShowCustomerSection) {
            val customerTitle = settings.pdfCustomerSectionTitle.ifBlank { "Customer Information" }
            """
              <div class="section-pill">${esc(customerTitle)}</div>
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
            """.trimIndent()
        } else ""

        val projectSectionHtml = if (settings.pdfShowProjectDescription) {
            val projTitle = settings.pdfProjectSectionTitle.ifBlank { "Project Description" }
            """
                <table class="grid-table box-section" style="margin-bottom:14px;">
                  <thead>
                    <tr><th>${esc(projTitle)}</th></tr>
                  </thead>
                  <tbody>
                    <tr><td>$projectSummaryHtml</td></tr>
                  </tbody>
                </table>
            """.trimIndent()
        } else ""

        val contactColHtml = if (settings.pdfShowContactSection) {
            val contactTitle = settings.pdfContactSectionTitle.ifBlank { "Contact Us" }
            """
                  <div class="contact-col">
                    <div class="contact-heading">${esc(contactTitle)}</div>
                    <div class="contact-item">
                      <span>${esc(emailLine)}</span>
                    </div>
                    <div class="contact-item">
                      <span>${esc(phoneLine)}</span>
                    </div>
                    <div class="contact-item">
                      <span>${esc(addressLine)}</span>
                    </div>
                    ${
                      if (licenseLine.isNotBlank()) {
                          "<div class='contact-item'><span>Lic: ${esc(licenseLine)}</span></div>"
                      } else ""
                    }
                  </div>
            """.trimIndent()
        } else ""

        val topRightPattern = if (settings.pdfShowCornerPattern) buildCornerPatternSvg(pdfPalette.steelHex, topRight = true) else ""
        val bottomLeftPattern = if (settings.pdfShowCornerPattern) buildCornerPatternSvg("#FFFFFF", topRight = false) else ""
        val pageBgCss = if (settings.pdfShowSplitBackdrop) {
            "linear-gradient(to bottom, ${pdfPalette.softGrayHex} 0%, ${pdfPalette.softGrayHex} 56%, ${pdfPalette.primaryHex} 56%, ${pdfPalette.primaryHex} 100%)"
        } else {
            "#FFFFFF"
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8" />
          <style>
            @page { size: letter; margin: 0; }
            * { -webkit-print-color-adjust: exact; print-color-adjust: exact; box-sizing: border-box; }
            html, body { margin: 0; padding: 0; font-family: 'Helvetica Neue', Arial, sans-serif; color: #1D2B3A; background: ${pdfPalette.softGrayHex}; }
            .page-shell {
              position: relative;
              min-height: 10.9in;
              padding: 26px 28px 34px 28px;
              background: $pageBgCss;
              overflow: hidden;
            }
            .corner-tr { position: absolute; top: 10px; right: 10px; z-index: 1; }
            .corner-bl { position: absolute; bottom: 10px; left: 10px; z-index: 1; }
            .content-layer { position: relative; z-index: 2; }
            .top-row { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; padding-right: 118px; margin-bottom: 14px; }
            .brand-badge { display: inline-flex; align-items: center; gap: 10px; background: ${pdfPalette.deepHex}; border: 2px solid ${pdfPalette.accentHex}; border-radius: 8px; padding: 7px 12px; margin-bottom: 12px; }
            .title-stack { line-height: 1.02; }
            .title-company { font-size: 25px; font-weight: 900; color: #1D2B3A; letter-spacing: 0.4px; }
            .title-doc { font-size: 25px; font-weight: 500; color: ${pdfPalette.primaryHex}; letter-spacing: 0.8px; margin-top: 2px; }
            .meta-col { min-width: 170px; padding-top: 8px; }
            .meta-rule { width: 92px; height: 3px; background: ${pdfPalette.accentHex}; margin-bottom: 14px; border-radius: 2px; }
            .meta-label { font-size: 11px; font-weight: 700; color: #1D2B3A; margin-top: 8px; }
            .meta-val { font-size: 11.5px; color: #4A4A4A; margin-top: 2px; }
            .section-pill { background: ${pdfPalette.primaryHex}; color: #FFFFFF; font-size: 13px; font-weight: 700; padding: 7px 16px; border-radius: 99px; border-left: 5px solid ${pdfPalette.accentHex}; margin-bottom: 12px; letter-spacing: 0.2px; }
            .cust-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px; }
            .kv-table { width: 100%; border-collapse: collapse; font-size: 11.5px; background: #FFFFFF; }
            .kv-table td { border: 1px solid ${pdfPalette.steelHex}; padding: 5px 8px; vertical-align: middle; }
            .kv-label { width: 84px; background: ${pdfPalette.skyHex}; font-weight: 700; color: #1D2B3A; }
            .kv-val { color: #4A4A4A; background: #FFFFFF; }
            .main-card { background: #FFFFFF; border: 1.5px solid ${pdfPalette.steelHex}; border-radius: 14px; padding: 18px 20px; }
            .grid-table { width: 100%; border-collapse: collapse; font-size: 11.5px; }
            .grid-table th { background: ${pdfPalette.skyHex}; color: #1D2B3A; font-weight: 700; text-align: left; border: 1px solid ${pdfPalette.steelHex}; padding: 6px 9px; }
            .grid-table td { border: 1px solid ${pdfPalette.steelHex}; padding: 6px 9px; vertical-align: top; color: #4A4A4A; }
            .grid-table tr { break-inside: avoid; }
            .box-section { break-inside: avoid; }
            .proj-title { font-weight: 700; color: #1D2B3A; margin-bottom: 4px; font-size: 12px; }
            .proj-sub { color: #4A4A4A; font-size: 11px; }
            .proj-rooms { display: flex; flex-direction: column; gap: 3px; }
            .proj-room-line { font-size: 11px; color: #4A4A4A; line-height: 1.35; }
            .item-pb { color: ${pdfPalette.steelHex}; font-size: 10.5px; }
            .item-note { color: #4A4A4A; font-size: 10.5px; margin-top: 2px; }
            .muted-cell { color: #4A4A4A; padding: 12px; }
            .center { text-align: center; }
            .right { text-align: right; }
            .bottom-split { display: flex; justify-content: space-between; align-items: flex-start; gap: 18px; margin-top: 14px; break-inside: avoid; }
            .notes-col { flex: 1.15; font-size: 11px; color: #4A4A4A; line-height: 1.45; }
            .notes-col strong { font-size: 12px; color: #1D2B3A; }
            .notes-col p { margin: 5px 0; }
            .totals-col { flex: 0.85; min-width: 220px; }
            .totals-table { width: 100%; border-collapse: collapse; font-size: 11.5px; }
            .totals-table td { border: 1px solid ${pdfPalette.steelHex}; padding: 6px 9px; }
            .totals-label { background: ${pdfPalette.skyHex}; font-weight: 700; color: #1D2B3A; width: 52%; }
            .totals-val { text-align: right; color: #1D2B3A; font-weight: 600; background: #FFFFFF; }
            .totals-grand td { font-weight: 800; font-size: 12.5px; border-top: 2px solid ${pdfPalette.accentHex}; }
            .contact-sign-row { display: flex; justify-content: space-between; align-items: flex-end; gap: 18px; margin-top: 22px; padding-top: 10px; border-top: 1px solid ${pdfPalette.skyHex}; break-inside: avoid; }
            .contact-col { flex: 1; }
            .contact-heading { font-size: 13px; font-weight: 800; color: ${pdfPalette.primaryHex}; margin-bottom: 8px; }
            .contact-item { display: flex; align-items: center; gap: 8px; font-size: 11px; color: #4A4A4A; margin-bottom: 5px; }
            .signatures { display: flex; gap: 26px; flex: 1; justify-content: flex-end; }
            .sig-col { width: 145px; text-align: center; }
            .sig-role { font-size: 12px; font-weight: 800; color: ${pdfPalette.primaryHex}; margin-bottom: 24px; }
            .sig-line { border-bottom: 1.5px solid ${pdfPalette.steelHex}; margin-bottom: 5px; }
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
                  $badgeHtml
                  <div class="title-stack">
                    <div class="title-company">$companyHeading</div>
                    <div class="title-doc">${esc(docWord)}</div>
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
              $customerSectionHtml
              <div class="main-card">
                $projectSectionHtml
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
                  <div class="notes-col">$notesHtml</div>
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
                ${
                  if (settings.pdfShowContactSection || showSignatures) {
                      "<div class='contact-sign-row'>$contactColHtml$signatureHtml</div>"
                  } else ""
                }
              </div>
            </div>
          </div>
        </body>
        </html>
        """.trimIndent()
    }
}
