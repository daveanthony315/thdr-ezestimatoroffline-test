package com.example.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import com.example.R
import com.example.model.AppSettings
import com.example.model.BrandLogoChoice
import com.example.model.Customer
import com.example.model.DocumentType
import com.example.model.EstimateDraft
import com.example.model.HeaderEmblemChoice
import com.example.model.PdfLogoMode
import com.example.model.PriceBookItem
import com.example.model.TakeoffResult
import java.io.FileOutputStream
import java.util.Locale
import kotlin.math.max

internal class EstimatePdfPrintAdapter(
    private val context: Context,
    private val jobName: String,
    private val draft: EstimateDraft,
    private val takeoff: TakeoffResult,
    private val priceBook: List<PriceBookItem>,
    private val settings: AppSettings,
    private val customer: Customer?
) : PrintDocumentAdapter() {

    private var printAttributes: PrintAttributes = PrintAttributes.Builder()
        .setMediaSize(PrintAttributes.MediaSize.NA_LETTER)
        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
        .build()

    private var totalPages: Int = 1

    private val pdfColors = EstimatePrintHelper.resolvePdfPalette(settings)
    private val colorPrimary = Color.parseColor(pdfColors.primaryHex)
    private val colorPrimaryDeep = Color.parseColor(pdfColors.deepHex)
    private val colorSteel = Color.parseColor(pdfColors.steelHex)
    private val colorSky = Color.parseColor(pdfColors.skyHex)
    private val colorSoftGray = Color.parseColor(pdfColors.softGrayHex)
    private val colorAccent = Color.parseColor(pdfColors.accentHex)
    private val colorCharcoal = Color.parseColor("#4A4A4A")
    private val colorSlateInk = Color.parseColor("#1D2B3A")
    private val colorWhite = Color.WHITE

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }
        printAttributes = newAttributes
        val (pageW, pageH) = resolvePageSizePoints(newAttributes)
        totalPages = renderOrMeasure(
            pdf = null,
            requestedPages = arrayOf(PageRange.ALL_PAGES),
            pageWidth = pageW,
            pageHeight = pageH,
            emblemBitmap = null,
            fullLogoBitmap = null
        )
        val info = PrintDocumentInfo.Builder(jobName)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(totalPages)
            .build()
        callback.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onWriteCancelled()
            return
        }
        val pdf = PrintedPdfDocument(context, printAttributes)
        val (emblemBitmap, fullLogoBitmap) = resolvePdfBitmaps()
        try {
            val (pageW, pageH) = resolvePageSizePoints(printAttributes)
            renderOrMeasure(
                pdf = pdf,
                requestedPages = pages,
                pageWidth = pageW,
                pageHeight = pageH,
                emblemBitmap = emblemBitmap,
                fullLogoBitmap = fullLogoBitmap,
                cancellationSignal = cancellationSignal
            )
            if (cancellationSignal?.isCanceled == true) {
                callback.onWriteCancelled()
                return
            }
            FileOutputStream(destination.fileDescriptor).use { out ->
                pdf.writeTo(out)
            }
            callback.onWriteFinished(pages)
        } catch (e: Exception) {
            callback.onWriteFailed(e.message ?: "Failed to generate PDF")
        } finally {
            emblemBitmap?.recycle()
            fullLogoBitmap?.recycle()
            pdf.close()
        }
    }

    private fun resolvePdfBitmaps(): Pair<Bitmap?, Bitmap?> {
        if (!settings.pdfShowBrandBadge) return null to null
        return when (settings.pdfLogoMode) {
            PdfLogoMode.HIDDEN, PdfLogoMode.TEXT_BADGE -> null to null
            PdfLogoMode.CUSTOM_PDF_IMAGE -> {
                val custom = BrandingImageStore.loadScaledBitmapFromPath(settings.customPdfLogoPath, 512)
                    ?: loadEmblemFromSettings()
                null to custom
            }
            PdfLogoMode.EMBLEM_ONLY -> loadEmblemFromSettings() to null
            PdfLogoMode.LOCKUP_ONLY -> null to loadBrandLogoFromSettings()
            PdfLogoMode.EMBLEM_AND_LOCKUP -> loadEmblemFromSettings() to loadBrandLogoFromSettings()
        }
    }

    private fun loadEmblemFromSettings(): Bitmap? {
        return when (settings.headerEmblemChoice) {
            HeaderEmblemChoice.CUSTOM ->
                BrandingImageStore.loadScaledBitmapFromPath(settings.customHeaderEmblemPath, 256)
                    ?: loadScaledBitmap(R.drawable.thdr_logo, 256)
            HeaderEmblemChoice.MASCOT_BADGE -> loadScaledBitmap(R.drawable.thdr_mascot_badge, 256)
            HeaderEmblemChoice.FULL_LOCKUP -> loadScaledBitmap(R.drawable.thdr_brand_logo, 256)
            HeaderEmblemChoice.HIDDEN -> null
            HeaderEmblemChoice.DEFAULT_EMBLEM -> loadScaledBitmap(R.drawable.thdr_logo, 256)
        }
    }

    private fun loadBrandLogoFromSettings(): Bitmap? {
        return when (settings.brandLogoChoice) {
            BrandLogoChoice.CUSTOM ->
                BrandingImageStore.loadScaledBitmapFromPath(settings.customBrandLogoPath, 512)
                    ?: loadScaledBitmap(R.drawable.thdr_brand_logo, 512)
            BrandLogoChoice.DEFAULT_EMBLEM -> loadScaledBitmap(R.drawable.thdr_logo, 512)
            BrandLogoChoice.MASCOT_BADGE -> loadScaledBitmap(R.drawable.thdr_mascot_badge, 512)
            BrandLogoChoice.HIDDEN -> null
            BrandLogoChoice.DEFAULT_LOCKUP -> loadScaledBitmap(R.drawable.thdr_brand_logo, 512)
        }
    }

    private fun resolvePageSizePoints(attrs: PrintAttributes): Pair<Float, Float> {
        val media = attrs.mediaSize ?: PrintAttributes.MediaSize.NA_LETTER
        val w = (media.widthMils * 72f / 1000f).coerceAtLeast(360f)
        val h = (media.heightMils * 72f / 1000f).coerceAtLeast(480f)
        return w to h
    }

    private fun loadScaledBitmap(resId: Int, maxDimPx: Int): Bitmap? = try {
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(context.resources, resId, boundsOpts)
        var sampleSize = 1
        while (boundsOpts.outWidth / sampleSize > maxDimPx || boundsOpts.outHeight / sampleSize > maxDimPx) {
            sampleSize *= 2
        }
        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        BitmapFactory.decodeResource(context.resources, resId, decodeOpts)
    } catch (_: Exception) {
        null
    }

    private fun isPageRequested(pageIndex: Int, ranges: Array<out PageRange>): Boolean {
        for (range in ranges) {
            if (range == PageRange.ALL_PAGES) return true
            if (pageIndex >= range.start && pageIndex <= range.end) return true
        }
        return false
    }

    private data class ServiceLineRow(
        val title: String,
        val details: String,
        val qtyText: String,
        val rateText: String,
        val totalText: String
    )

    private fun renderOrMeasure(
        pdf: PrintedPdfDocument?,
        requestedPages: Array<out PageRange>,
        pageWidth: Float,
        pageHeight: Float,
        emblemBitmap: Bitmap?,
        fullLogoBitmap: Bitmap?,
        cancellationSignal: CancellationSignal? = null
    ): Int {
        val marginX = 24f
        val marginTop = 22f
        val marginBottom = 26f
        val contentWidth = pageWidth - marginX * 2f

        val isProposal = draft.docType == DocumentType.PROPOSAL
        val docWord = if (isProposal) {
            settings.pdfProposalTitle.ifBlank { "PROPOSAL" }
        } else {
            settings.pdfEstimateTitle.ifBlank { "ESTIMATE" }
        }
        val docNumber = EstimatePrintHelper.buildDocumentNumber(draft, settings)
        val companyHeading = settings.pdfCompanyTitleOverride
            .ifBlank { settings.companyName.ifBlank { "THE HOME IMPROVEMENT" } }
            .uppercase(Locale.US)
        val currency = settings.currencySymbol

        val customerName = draft.customer.ifBlank { customer?.name.orEmpty() }.ifBlank { "Valued Client" }
        val customerCompany = customer?.company.orEmpty().ifBlank { "Residential Property" }
        val customerEmail = customer?.email.orEmpty().ifBlank { "—" }
        val customerPhone = customer?.phone.orEmpty()
        val customerAddress = customer?.address.orEmpty().ifBlank { "—" }

        val serviceRows = buildList {
            draft.rooms.forEach { room ->
                room.services.forEach { svc ->
                    val sel = room.serviceSelections[svc]
                    val items = EstimatorEngine.pbItemsForService(svc, priceBook)
                    val chosen = items.firstOrNull { it.item == sel?.selectedPbItemName } ?: items.firstOrNull()
                    val unit = sel?.effectiveUnit(chosen?.unit ?: "EA") ?: chosen?.unit ?: "EA"
                    val qty = sel?.quantity ?: 0.0
                    val rate = sel?.rate ?: 0.0
                    val total = qty * rate
                    val subcatLine = sel?.selectedSubcategories?.takeIf { it.isNotEmpty() }?.joinToString(", ").orEmpty()
                    val scopeNotes = sel?.scopeNotes.orEmpty()
                    val title = buildString {
                        append("${room.name} — $svc")
                        if (chosen != null) append(" (${chosen.item})")
                    }
                    val details = buildString {
                        if (subcatLine.isNotBlank()) append("Scope: $subcatLine")
                        if (scopeNotes.isNotBlank()) {
                            if (isNotEmpty()) append("\n")
                            append(scopeNotes)
                        }
                    }
                    add(
                        ServiceLineRow(
                            title = title,
                            details = details,
                            qtyText = "${EstimatorEngine.formatQty(qty)} $unit",
                            rateText = EstimatorEngine.formatMoney(rate, currency),
                            totalText = EstimatorEngine.formatMoney(total, currency)
                        )
                    )
                }
            }
        }

        val textBold = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorSlateInk
            textSize = 9.2f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val textBody = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorCharcoal
            textSize = 8.8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val textMuted = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorCharcoal
            textSize = 8.2f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        var pageIndex = 0
        var activePage: android.graphics.pdf.PdfDocument.Page? = null
        var canvas: Canvas? = null
        var cardRect: RectF? = null

        fun finishCurrentPage() {
            val c = canvas
            val rect = cardRect
            if (c != null && rect != null) {
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    color = colorSteel
                    strokeWidth = 1.2f
                }
                c.drawRoundRect(rect, 11f, 11f, borderPaint)
            }
            if (activePage != null && pdf != null) {
                pdf.finishPage(activePage)
            }
            activePage = null
            canvas = null
            cardRect = null
        }

        fun startPage(cardTopY: Float): Float {
            if (cancellationSignal?.isCanceled == true) return cardTopY
            val shouldDraw = pdf != null && isPageRequested(pageIndex, requestedPages)
            if (shouldDraw && pdf != null) {
                activePage = pdf.startPage(pageIndex)
                canvas = activePage?.canvas
            } else {
                activePage = null
                canvas = null
            }
            pageIndex++

            val rect = RectF(marginX, cardTopY, pageWidth - marginX, pageHeight - marginBottom)
            cardRect = rect

            canvas?.let { c ->
                drawPageShell(c, pageWidth, pageHeight)
                val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    color = colorWhite
                }
                c.drawRoundRect(rect, 11f, 11f, fillPaint)
            }
            return cardTopY + 14f
        }

        val showBadge = settings.pdfShowBrandBadge && settings.pdfLogoMode != PdfLogoMode.HIDDEN
        val headerHeight = drawFirstPageIntroMeasure(
            showBadge = showBadge,
            showCustomerSection = settings.pdfShowCustomerSection,
            customerPhone = customerPhone,
            marginTop = marginTop
        )
        var y = startPage(headerHeight)
        canvas?.let { c ->
            drawFirstPageIntro(
                canvas = c,
                pageWidth = pageWidth,
                marginX = marginX,
                marginTop = marginTop,
                contentWidth = contentWidth,
                companyHeading = companyHeading,
                docWord = docWord,
                docNumber = docNumber,
                isProposal = isProposal,
                showBadge = showBadge,
                customerName = customerName,
                customerCompany = customerCompany,
                customerEmail = customerEmail,
                customerPhone = customerPhone,
                customerAddress = customerAddress,
                emblemBitmap = emblemBitmap,
                fullLogoBitmap = fullLogoBitmap
            )
        }

        val cardPadX = marginX + 14f
        val innerW = contentWidth - 28f
        val cardBottomLimit = pageHeight - marginBottom - 14f

        fun ensureSpace(neededHeight: Float, repeatServiceHeader: Boolean = false): Float {
            if (y + neededHeight <= cardBottomLimit) return y
            finishCurrentPage()
            var nextY = startPage(marginTop + 6f)
            if (repeatServiceHeader) {
                nextY = drawServiceTableHeader(canvas, cardPadX, nextY, innerW)
            }
            return nextY
        }

        // Project Description box
        if (settings.pdfShowProjectDescription) {
            val projectTitle = draft.project.ifBlank { "${draft.estimateSide.label} Remodeling & Home Improvement" }
            val roomLines = if (draft.rooms.isEmpty()) {
                listOf("No rooms or work areas added yet.")
            } else {
                draft.rooms.map { room ->
                    val d = EstimatorEngine.computeDimensions(room)
                    val condText = if (room.condition.isNotBlank()) " · Condition: ${room.condition}" else ""
                    val notesText = if (room.roomNotes.isNotBlank()) " — ${room.roomNotes}" else ""
                    "${room.name} (${room.side.label}): ${EstimatorEngine.formatQty(room.lengthFt)} × ${EstimatorEngine.formatQty(room.widthFt)} × ${EstimatorEngine.formatQty(room.heightFt)} FT · Floor ${String.format(Locale.US, "%.1f", d.floor)} SF · Walls ${String.format(Locale.US, "%.1f", d.wall)} SF$condText$notesText"
                }
            }
            val projTitleLayout = buildStaticLayout(projectTitle, textBold, (innerW - 14f).toInt())
            val roomLayouts = roomLines.map { buildStaticLayout(it, textMuted, (innerW - 14f).toInt()) }
            val projBodyH = 10f + projTitleLayout.height + 4f + roomLayouts.sumOf { it.height + 2 }
            val projTotalH = 20f + projBodyH

            y = ensureSpace(projTotalH)
            drawSectionTableBox(
                canvas = canvas,
                x = cardPadX,
                y = y,
                w = innerW,
                headerTitle = settings.pdfProjectSectionTitle.ifBlank { "Project Description" },
                bodyHeight = projBodyH
            ) { c, bodyTop ->
                var py = bodyTop + 5f
                c.save()
                c.translate(cardPadX + 7f, py)
                projTitleLayout.draw(c)
                c.restore()
                py += projTitleLayout.height + 4f
                roomLayouts.forEach { rl ->
                    c.save()
                    c.translate(cardPadX + 7f, py)
                    rl.draw(c)
                    c.restore()
                    py += rl.height + 2f
                }
            }
            y += projTotalH + 10f
        }

        // Item/Service Description Table
        val colDescW = innerW * 0.50f
        val colQtyW = innerW * 0.16f
        val colUnitW = innerW * 0.16f
        val colTotalW = innerW - colDescW - colQtyW - colUnitW

        y = ensureSpace(46f)
        y = drawServiceTableHeader(canvas, cardPadX, y, innerW)

        if (serviceRows.isEmpty()) {
            val emptyH = 26f
            y = ensureSpace(emptyH, repeatServiceHeader = true)
            drawTableRowBorders(canvas, cardPadX, y, innerW, emptyH, listOf(innerW))
            canvas?.let { c ->
                val p = Paint(textMuted).apply { textAlign = Paint.Align.CENTER }
                c.drawText("No billable services added to rooms yet.", cardPadX + innerW / 2f, y + 16f, p)
            }
            y += emptyH
        } else {
            serviceRows.forEach { row ->
                val titleLayout = buildStaticLayout(row.title, textBold, (colDescW - 12f).toInt())
                val detailsLayout = if (row.details.isNotBlank()) {
                    buildStaticLayout(row.details, textMuted, (colDescW - 12f).toInt())
                } else null
                val textH = titleLayout.height + (detailsLayout?.height?.plus(2) ?: 0)
                val rowH = max(22f, textH + 10f)
                y = ensureSpace(rowH, repeatServiceHeader = true)
                drawTableRowBorders(canvas, cardPadX, y, innerW, rowH, listOf(colDescW, colQtyW, colUnitW, colTotalW))
                canvas?.let { c ->
                    c.save()
                    c.translate(cardPadX + 6f, y + 5f)
                    titleLayout.draw(c)
                    if (detailsLayout != null) {
                        c.translate(0f, titleLayout.height + 2f)
                        detailsLayout.draw(c)
                    }
                    c.restore()

                    val baseline = y + 14f
                    val centerP = Paint(textBody).apply { textAlign = Paint.Align.CENTER }
                    c.drawText(row.qtyText, cardPadX + colDescW + colQtyW / 2f, baseline, centerP)

                    val rightP = Paint(textBody).apply { textAlign = Paint.Align.RIGHT }
                    c.drawText(row.rateText, cardPadX + colDescW + colQtyW + colUnitW - 6f, baseline, rightP)

                    val rightBoldP = Paint(textBold).apply { textAlign = Paint.Align.RIGHT }
                    c.drawText(row.totalText, cardPadX + innerW - 6f, baseline, rightBoldP)
                }
                y += rowH
            }
        }

        // Materials Takeoff Reference (if enabled and present)
        if (settings.pdfShowMaterialsTable && takeoff.materials.isNotEmpty()) {
            y += 10f
            val matCol1 = innerW * 0.75f
            val matCol2 = innerW - matCol1
            y = ensureSpace(42f)
            drawTableHeaderCells(
                canvas = canvas,
                x = cardPadX,
                y = y,
                h = 20f,
                widths = listOf(matCol1, matCol2),
                labels = listOf(
                    settings.pdfMaterialsSectionTitle.ifBlank { "Materials Takeoff Reference" },
                    "Estimated Qty"
                ),
                rightAlignedLast = true
            )
            y += 20f
            takeoff.materials.entries.forEach { (name, qty) ->
                val nameLayout = buildStaticLayout(name, textBody, (matCol1 - 12f).toInt())
                val rowH = max(20f, nameLayout.height + 8f)
                y = ensureSpace(rowH)
                drawTableRowBorders(canvas, cardPadX, y, innerW, rowH, listOf(matCol1, matCol2))
                canvas?.let { c ->
                    c.save()
                    c.translate(cardPadX + 6f, y + 4f)
                    nameLayout.draw(c)
                    c.restore()
                    val rightBoldP = Paint(textBold).apply { textAlign = Paint.Align.RIGHT }
                    c.drawText(EstimatorEngine.formatQty(qty), cardPadX + innerW - 6f, y + 13f, rightBoldP)
                }
                y += rowH
            }
        }

        // Bottom Split: Notes + Totals
        y += 12f
        val discount = max(0.0, draft.discountText.toDoubleOrNull() ?: 0.0)
        val taxPct = max(0.0, draft.taxText.toDoubleOrNull() ?: 0.0)
        val subtotal = takeoff.serviceTotal
        val taxableBase = max(0.0, subtotal - discount)
        val taxAmount = taxableBase * (taxPct / 100.0)
        val depositPct = draft.depositPercentText.toDoubleOrNull() ?: settings.defaultProposalDepositPct
        val depositAmt = takeoff.grandTotal * (depositPct.coerceIn(0.0, 100.0) / 100.0)

        val notesText = if (!settings.pdfShowNotesSection) {
            ""
        } else if (isProposal) {
            val terms = draft.termsText.ifBlank { settings.defaultProposalTerms }
            val validStr = if (draft.validUntil.isNotBlank()) " · Valid Until: ${draft.validUntil}" else ""
            "$terms\nStatus: ${draft.proposalStatus.label}$validStr · Deposit (${EstimatorEngine.formatQty(depositPct)}%): ${EstimatorEngine.formatMoney(depositAmt, currency)}"
        } else if (settings.pdfCustomEstimateNotes.isNotBlank()) {
            settings.pdfCustomEstimateNotes
        } else {
            "This estimate is valid for ${settings.defaultProposalValidDays} days from the date issued. A ${EstimatorEngine.formatQty(depositPct)}% deposit is required to schedule work.\nAccuracy check: unknown conditions, unverified dimensions, structural work, and concealed damage should be verified on site before final pricing."
        }
        val notesW = innerW * 0.54f
        val totalsW = innerW - notesW - 14f
        val notesHeading = if (isProposal) "Notes & Proposal Terms" else "Notes"
        val notesLayout = if (settings.pdfShowNotesSection) {
            buildStaticLayout(notesText, textMuted, notesW.toInt())
        } else null

        val totalRows = buildList {
            add("Subtotal" to EstimatorEngine.formatMoney(subtotal, currency))
            if (discount > 0.0) add("Discount" to "-${EstimatorEngine.formatMoney(discount, currency)}")
            add("Tax (${EstimatorEngine.formatQty(taxPct)}%)" to EstimatorEngine.formatMoney(taxAmount, currency))
            add("Total ${if (isProposal) "Proposal" else "Estimate"}" to EstimatorEngine.formatMoney(takeoff.grandTotal, currency))
        }
        val totalsH = totalRows.size * 20f
        val notesH = notesLayout?.let { 14f + it.height } ?: 0f
        val splitH = max(notesH, totalsH)
        y = ensureSpace(splitH)

        canvas?.let { c ->
            if (settings.pdfShowNotesSection && notesLayout != null) {
                c.drawText(notesHeading, cardPadX, y + 10f, textBold)
                c.save()
                c.translate(cardPadX, y + 15f)
                notesLayout.draw(c)
                c.restore()
            }

            val tx = cardPadX + notesW + 14f
            val labelW = totalsW * 0.54f
            var ty = y
            totalRows.forEachIndexed { idx, (lbl, value) ->
                val isGrand = idx == totalRows.lastIndex
                val rowH = 20f
                val bgLabel = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorSky }
                val bgVal = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorWhite }
                c.drawRect(tx, ty, tx + labelW, ty + rowH, bgLabel)
                c.drawRect(tx + labelW, ty, tx + totalsW, ty + rowH, bgVal)

                val strokeP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    color = colorSteel
                    strokeWidth = 0.8f
                }
                c.drawRect(tx, ty, tx + labelW, ty + rowH, strokeP)
                c.drawRect(tx + labelW, ty, tx + totalsW, ty + rowH, strokeP)

                if (isGrand) {
                    val accentLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = colorAccent
                        strokeWidth = 2f
                    }
                    c.drawLine(tx, ty, tx + totalsW, ty, accentLine)
                }
                val lblP = Paint(textBold).apply { textSize = if (isGrand) 9.5f else 8.8f }
                val valP = Paint(textBold).apply {
                    textSize = if (isGrand) 9.8f else 8.8f
                    textAlign = Paint.Align.RIGHT
                }
                c.drawText(lbl, tx + 6f, ty + 13.5f, lblP)
                c.drawText(value, tx + totalsW - 6f, ty + 13.5f, valP)
                ty += rowH
            }
        }
        y += splitH + 14f

        // Contact Us & Signatures Footer
        val showSignatures = if (isProposal) {
            settings.includeSignatureBlockOnProposal
        } else {
            settings.includeSignatureBlockOnEstimate
        }
        if (settings.pdfShowContactSection || showSignatures) {
            val emailLine = settings.companyEmail.ifBlank { "info@thehousedr.com" }
            val phoneLine = settings.companyPhone.ifBlank { "Call office for scheduling" }
            val addressLine = settings.companyAddress.ifBlank { settings.companyTagline.ifBlank { "Interior & Exterior Remodeling" } }
            val licenseLine = settings.companyLicense
            val footerH = if (settings.pdfShowContactSection && licenseLine.isNotBlank()) 82f else 68f

            y = ensureSpace(footerH)
            canvas?.let { c ->
                val divP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = colorSky
                    strokeWidth = 1f
                }
                c.drawLine(cardPadX, y, cardPadX + innerW, y, divP)
                val fy = y + 14f
                val contactHeadP = Paint(textBold).apply {
                    color = colorPrimary
                    textSize = 10.5f
                }
                if (settings.pdfShowContactSection) {
                    c.drawText(settings.pdfContactSectionTitle.ifBlank { "Contact Us" }, cardPadX, fy, contactHeadP)

                    val dotP = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorPrimary }
                    var cy = fy + 14f
                    val contacts = buildList {
                        add("Email: $emailLine")
                        add("Phone: $phoneLine")
                        add("Location: $addressLine")
                        if (licenseLine.isNotBlank()) add("License: $licenseLine")
                    }
                    contacts.forEach { line ->
                        c.drawCircle(cardPadX + 3.5f, cy - 3f, 2.5f, dotP)
                        c.drawText(line, cardPadX + 11f, cy, textBody)
                        cy += 13f
                    }
                }

                if (showSignatures) {
                    val sigW = 108f
                    val sigGap = 18f
                    val sig2X = cardPadX + innerW - sigW
                    val sig1X = sig2X - sigGap - sigW

                    drawSignatureCol(
                        canvas = c,
                        x = sig1X,
                        topY = fy,
                        width = sigW,
                        role = settings.pdfContractorSignatureLabel.ifBlank { "Contractor" },
                        name = settings.companyName.ifBlank { "House DR" },
                        org = settings.companyTagline.ifBlank { "Authorized Representative" },
                        rolePaint = contactHeadP,
                        namePaint = textBold,
                        orgPaint = textMuted
                    )
                    drawSignatureCol(
                        canvas = c,
                        x = sig2X,
                        topY = fy,
                        width = sigW,
                        role = settings.pdfClientSignatureLabel.ifBlank { "Client" },
                        name = customerName,
                        org = customerCompany,
                        rolePaint = contactHeadP,
                        namePaint = textBold,
                        orgPaint = textMuted
                    )
                }
            }
        }

        finishCurrentPage()
        return max(1, pageIndex)
    }

    private fun drawSignatureCol(
        canvas: Canvas,
        x: Float,
        topY: Float,
        width: Float,
        role: String,
        name: String,
        org: String,
        rolePaint: Paint,
        namePaint: Paint,
        orgPaint: Paint
    ) {
        val centerRole = Paint(rolePaint).apply { textAlign = Paint.Align.CENTER; textSize = 9.5f }
        val centerName = Paint(namePaint).apply { textAlign = Paint.Align.CENTER; textSize = 8.8f }
        val centerOrg = Paint(orgPaint).apply { textAlign = Paint.Align.CENTER; textSize = 8f }
        val cx = x + width / 2f
        canvas.drawText(role, cx, topY, centerRole)
        val lineY = topY + 28f
        val lineP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorSteel
            strokeWidth = 1.2f
        }
        canvas.drawLine(x, lineY, x + width, lineY, lineP)
        canvas.drawText(name.take(26), cx, lineY + 11f, centerName)
        canvas.drawText(org.take(28), cx, lineY + 21f, centerOrg)
    }

    private fun drawPageShell(canvas: Canvas, pageW: Float, pageH: Float) {
        if (settings.pdfShowSplitBackdrop) {
            val splitY = pageH * 0.56f
            val topPaint = Paint().apply { color = colorSoftGray }
            val botPaint = Paint().apply { color = colorPrimary }
            canvas.drawRect(0f, 0f, pageW, splitY, topPaint)
            canvas.drawRect(0f, splitY, pageW, pageH, botPaint)
        } else {
            canvas.drawRect(0f, 0f, pageW, pageH, Paint().apply { color = colorWhite })
        }

        if (settings.pdfShowCornerPattern) {
            drawCornerPattern(canvas, pageW - 108f, 10f, colorSteel, topRight = true)
            val bottomPatternColor = if (settings.pdfShowSplitBackdrop) colorWhite else colorSteel
            drawCornerPattern(canvas, 12f, pageH - 86f, bottomPatternColor, topRight = false)
        }
    }

    private fun drawCornerPattern(canvas: Canvas, startX: Float, startY: Float, color: Int, topRight: Boolean) {
        val cols = 5
        val rows = 4
        val spacing = 19f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val weight = if (topRight) (c + (rows - 1 - r)) / 7f else ((cols - 1 - c) + r) / 7f
                val half = 2.2f + weight * 3.6f
                paint.strokeWidth = 1.3f + weight * 1.7f
                val cx = startX + 10f + c * spacing
                val cy = startY + 10f + r * spacing
                canvas.drawLine(cx - half, cy - half, cx + half, cy + half, paint)
                canvas.drawLine(cx + half, cy - half, cx - half, cy + half, paint)
            }
        }
    }

    private fun drawFirstPageIntroMeasure(
        showBadge: Boolean,
        showCustomerSection: Boolean,
        customerPhone: String,
        marginTop: Float
    ): Float {
        val badgeOffset = if (showBadge) 50f else 8f
        val titleStackH = 44f
        val custBlockH = if (showCustomerSection) {
            val extraPhone = if (customerPhone.isNotBlank()) 20f else 0f
            12f + 24f + 10f + 40f + extraPhone
        } else 0f
        return marginTop + badgeOffset + titleStackH + custBlockH + 12f
    }

    private fun drawFirstPageIntro(
        canvas: Canvas,
        pageWidth: Float,
        marginX: Float,
        marginTop: Float,
        contentWidth: Float,
        companyHeading: String,
        docWord: String,
        docNumber: String,
        isProposal: Boolean,
        showBadge: Boolean,
        customerName: String,
        customerCompany: String,
        customerEmail: String,
        customerPhone: String,
        customerAddress: String,
        emblemBitmap: Bitmap?,
        fullLogoBitmap: Bitmap?
    ) {
        if (showBadge) {
            val badgeW = when {
                emblemBitmap != null && fullLogoBitmap != null -> 196f
                emblemBitmap != null || fullLogoBitmap != null -> 146f
                else -> 184f
            }
            val badgeRect = RectF(marginX, marginTop, marginX + badgeW, marginTop + 42f)
            val badgeFill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorPrimaryDeep }
            val badgeBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                color = colorAccent
                strokeWidth = 1.6f
            }
            canvas.drawRoundRect(badgeRect, 7f, 7f, badgeFill)
            canvas.drawRoundRect(badgeRect, 7f, 7f, badgeBorder)

            var bx = marginX + 8f
            if (emblemBitmap != null) {
                val dst = RectF(bx, marginTop + 5f, bx + 32f, marginTop + 37f)
                canvas.drawBitmap(emblemBitmap, null, dst, Paint(Paint.FILTER_BITMAP_FLAG))
                bx += 38f
            }
            if (fullLogoBitmap != null) {
                val aspect = fullLogoBitmap.width.toFloat() / fullLogoBitmap.height.coerceAtLeast(1).toFloat()
                val h = 30f
                val maxW = if (emblemBitmap != null) 142f else 130f
                val w = (h * aspect).coerceAtMost(maxW)
                val dst = RectF(bx, marginTop + 6f, bx + w, marginTop + 6f + h)
                canvas.drawBitmap(fullLogoBitmap, null, dst, Paint(Paint.FILTER_BITMAP_FLAG))
            } else if (emblemBitmap == null) {
                val fallbackP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = colorWhite
                    textSize = 9.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val fittedBadge = TextUtils.ellipsize(
                    "$companyHeading · $docWord",
                    TextPaint(fallbackP),
                    badgeW - 18f,
                    TextUtils.TruncateAt.END
                ).toString()
                canvas.drawText(fittedBadge, marginX + 10f, marginTop + 25f, fallbackP)
            }
        }

        // Title Stack
        val metaX = pageWidth - marginX - 210f
        val titleMaxW = (metaX - marginX - 16f).coerceAtLeast(120f)
        val titleY = if (showBadge) marginTop + 66f else marginTop + 24f
        val compPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorSlateInk
            textSize = 18.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val docPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorPrimary
            textSize = 18.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val fittedCompany = TextUtils.ellipsize(companyHeading, compPaint, titleMaxW, TextUtils.TruncateAt.END).toString()
        canvas.drawText(fittedCompany, marginX, titleY, compPaint)
        canvas.drawText(docWord, marginX, titleY + 21f, docPaint)

        // Meta Column
        val rulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorAccent }
        canvas.drawRoundRect(RectF(metaX, marginTop + 6f, metaX + 76f, marginTop + 9f), 1.5f, 1.5f, rulePaint)
        val metaLblP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorSlateInk
            textSize = 8.8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val metaValP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorCharcoal
            textSize = 9f
        }
        var my = marginTop + 24f
        canvas.drawText("Date", metaX, my, metaLblP)
        canvas.drawText(draft.date.ifBlank { "—" }, metaX, my + 11f, metaValP)
        my += 26f
        canvas.drawText("Number", metaX, my, metaLblP)
        canvas.drawText(docNumber, metaX, my + 11f, metaValP)
        if (isProposal && draft.validUntil.isNotBlank()) {
            my += 26f
            canvas.drawText("Valid Until", metaX, my, metaLblP)
            canvas.drawText(draft.validUntil, metaX, my + 11f, metaValP)
        }

        if (settings.pdfShowCustomerSection) {
            val pillY = titleY + 40f
            val pillRect = RectF(marginX, pillY, marginX + contentWidth, pillY + 24f)
            canvas.drawRoundRect(pillRect, 12f, 12f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorPrimary })
            canvas.drawRoundRect(RectF(marginX, pillY, marginX + 8f, pillY + 24f), 4f, 4f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colorAccent })
            val pillTextP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colorWhite
                textSize = 10.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText(
                settings.pdfCustomerSectionTitle.ifBlank { "Customer Information" },
                marginX + 16f,
                pillY + 15.5f,
                pillTextP
            )

            val gridY = pillY + 34f
            val gap = 14f
            val colW = (contentWidth - gap) / 2f
            val leftRows = buildList {
                add("Name" to customerName)
                add("Company" to customerCompany)
                if (customerPhone.isNotBlank()) add("Phone" to customerPhone)
            }
            val rightRows = listOf(
                "Email" to customerEmail,
                "Address" to customerAddress
            )
            drawKvTable(canvas, marginX, gridY, colW, leftRows)
            drawKvTable(canvas, marginX + colW + gap, gridY, colW, rightRows)
        }
    }

    private fun drawKvTable(canvas: Canvas, x: Float, startY: Float, w: Float, rows: List<Pair<String, String>>) {
        val lblW = 68f
        val rowH = 20f
        val lblFill = Paint().apply { color = colorSky }
        val valFill = Paint().apply { color = colorWhite }
        val stroke = Paint().apply {
            style = Paint.Style.STROKE
            color = colorSteel
            strokeWidth = 0.8f
        }
        val lblP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorSlateInk
            textSize = 8.8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val valP = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorCharcoal
            textSize = 8.8f
        }
        val maxValW = (w - lblW - 12f).coerceAtLeast(40f)
        var y = startY
        rows.forEach { (k, v) ->
            canvas.drawRect(x, y, x + lblW, y + rowH, lblFill)
            canvas.drawRect(x + lblW, y, x + w, y + rowH, valFill)
            canvas.drawRect(x, y, x + lblW, y + rowH, stroke)
            canvas.drawRect(x + lblW, y, x + w, y + rowH, stroke)
            canvas.drawText(k, x + 6f, y + 13.5f, lblP)
            val fittedVal = TextUtils.ellipsize(v, valP, maxValW, TextUtils.TruncateAt.END).toString()
            canvas.drawText(fittedVal, x + lblW + 6f, y + 13.5f, valP)
            y += rowH
        }
    }

    private fun drawSectionTableBox(
        canvas: Canvas?,
        x: Float,
        y: Float,
        w: Float,
        headerTitle: String,
        bodyHeight: Float,
        drawBody: (Canvas, Float) -> Unit
    ) {
        val headerH = 20f
        drawTableHeaderCells(canvas, x, y, headerH, listOf(w), listOf(headerTitle), rightAlignedLast = false)
        drawTableRowBorders(canvas, x, y + headerH, w, bodyHeight, listOf(w))
        canvas?.let { drawBody(it, y + headerH) }
    }

    private fun drawServiceTableHeader(canvas: Canvas?, x: Float, y: Float, w: Float): Float {
        val colDescW = w * 0.50f
        val colQtyW = w * 0.16f
        val colUnitW = w * 0.16f
        val colTotalW = w - colDescW - colQtyW - colUnitW
        drawTableHeaderCells(
            canvas = canvas,
            x = x,
            y = y,
            h = 20f,
            widths = listOf(colDescW, colQtyW, colUnitW, colTotalW),
            labels = listOf("Item/Service Description", "Quantity", "Unit Price", "Total Price"),
            rightAlignedLast = true,
            centerSecond = true,
            rightAlignedThird = true
        )
        return y + 20f
    }

    private fun drawTableHeaderCells(
        canvas: Canvas?,
        x: Float,
        y: Float,
        h: Float,
        widths: List<Float>,
        labels: List<String>,
        rightAlignedLast: Boolean,
        centerSecond: Boolean = false,
        rightAlignedThird: Boolean = false
    ) {
        canvas ?: return
        val bg = Paint().apply { color = colorSky }
        val stroke = Paint().apply {
            style = Paint.Style.STROKE
            color = colorSteel
            strokeWidth = 0.8f
        }
        val textP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorSlateInk
            textSize = 8.8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        var cx = x
        widths.forEachIndexed { idx, w ->
            canvas.drawRect(cx, y, cx + w, y + h, bg)
            canvas.drawRect(cx, y, cx + w, y + h, stroke)
            val label = labels[idx]
            val isRight = (rightAlignedLast && idx == widths.lastIndex) || (rightAlignedThird && idx == 2)
            val isCenter = centerSecond && idx == 1
            val p = Paint(textP).apply {
                textAlign = when {
                    isRight -> Paint.Align.RIGHT
                    isCenter -> Paint.Align.CENTER
                    else -> Paint.Align.LEFT
                }
            }
            val tx = when {
                isRight -> cx + w - 6f
                isCenter -> cx + w / 2f
                else -> cx + 6f
            }
            canvas.drawText(label, tx, y + 13.5f, p)
            cx += w
        }
    }

    private fun drawTableRowBorders(canvas: Canvas?, x: Float, y: Float, w: Float, h: Float, colWidths: List<Float>) {
        canvas ?: return
        val bg = Paint().apply { color = colorWhite }
        val stroke = Paint().apply {
            style = Paint.Style.STROKE
            color = colorSteel
            strokeWidth = 0.8f
        }
        canvas.drawRect(x, y, x + w, y + h, bg)
        var cx = x
        colWidths.forEach { cw ->
            canvas.drawRect(cx, y, cx + cw, y + h, stroke)
            cx += cw
        }
    }

    private fun buildStaticLayout(text: CharSequence, paint: TextPaint, width: Int): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width.coerceAtLeast(1))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setIncludePad(false)
            .build()
}
