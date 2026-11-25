package com.example

import com.example.domain.EstimatePrintHelper
import com.example.domain.EstimatorEngine
import com.example.model.AppSettings
import com.example.model.Customer
import com.example.model.DocumentType
import com.example.model.EstimateDraft
import com.example.model.EstimateRoom
import com.example.model.EstimateSide
import com.example.model.PriceBookItem
import com.example.model.ProposalStatus
import com.example.model.RoomServiceSelection
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EstimatePrintHelperTest {

    private val samplePriceBook = listOf(
        PriceBookItem(
            category = "004 Drywall",
            item = "Hang & Finish Drywall",
            unit = "SF",
            laborRate = 85.0,
            benchmarkLaborHoursPerUnit = 0.02,
            benchmarkUnitPrice = 4.0
        )
    )

    @Test
    fun estimateHtmlFollowsTemplateBackboneAndCalculations() {
        val room = EstimateRoom(
            id = "r1",
            name = "Kitchen",
            side = EstimateSide.INTERIOR,
            lengthFt = 10.0,
            widthFt = 12.0,
            heightFt = 8.0,
            condition = "Good",
            services = listOf("Drywall"),
            serviceSelections = mapOf(
                "Drywall" to RoomServiceSelection(
                    serviceName = "Drywall",
                    selectedPbItemName = "Hang & Finish Drywall",
                    quantity = 200.0,
                    quantityManual = true,
                    rate = 4.0,
                    rateInitialized = true,
                    scopeNotes = "Smooth finish ready for paint"
                )
            )
        )
        val draft = EstimateDraft(
            id = 25L,
            docType = DocumentType.ESTIMATE,
            customer = "Lina Bridger",
            project = "Kitchen Drywall Remodel",
            date = "2026-11-24",
            rooms = listOf(room),
            discountText = "50",
            taxText = "8"
        )
        val settings = AppSettings(
            companyName = "The Home Improvement",
            companyPhone = "315-555-0199",
            companyEmail = "office@thehousedr.com",
            companyAddress = "1145 Oak St, Syracuse, NY"
        )
        val customer = Customer(
            id = 7L,
            name = "Lina Bridger",
            company = "Unity Enterprises",
            phone = "315-555-0100",
            email = "lina@unity.com",
            address = "742 Evergreen Ter"
        )
        val takeoff = EstimatorEngine.computeTakeoff(
            rooms = draft.rooms,
            priceBook = samplePriceBook,
            manualMaterials = emptyMap(),
            discount = 50.0,
            taxPercent = 8.0,
            settings = settings
        )

        val html = EstimatePrintHelper.buildEstimateHtml(
            draft = draft,
            takeoff = takeoff,
            priceBook = samplePriceBook,
            settings = settings,
            customer = customer,
            emblemBase64 = "EMBLEM_B64",
            fullLogoBase64 = "FULL_LOGO_B64"
        )

        assertTrue(html.contains("Customer Information"))
        assertTrue(html.contains("Project Description"))
        assertTrue(html.contains("Item/Service Description"))
        assertTrue(html.contains("Total Price"))
        assertTrue(html.contains("Contact Us"))
        assertTrue(html.contains("THE HOME IMPROVEMENT"))
        assertTrue(html.contains("ESTIMATE"))
        assertTrue(html.contains("THDR-EST-2026-025"))
        assertTrue(html.contains("Unity Enterprises"))
        assertTrue(html.contains("lina@unity.com"))
        assertTrue(html.contains("742 Evergreen Ter"))
        assertTrue(html.contains("$800.00"))
        assertTrue(html.contains("-$50.00"))
        assertTrue(html.contains("Tax (8%)"))
        assertTrue(html.contains("$60.00"))
        assertTrue(html.contains("Total Estimate"))
        assertTrue(html.contains("$810.00"))
        assertTrue(html.contains("data:image/png;base64,EMBLEM_B64"))
        assertTrue(html.contains("data:image/png;base64,FULL_LOGO_B64"))
    }

    @Test
    fun proposalHtmlRespectsProposalLabelsAndSignatureSetting() {
        val draft = EstimateDraft(
            id = 4L,
            docType = DocumentType.PROPOSAL,
            proposalStatus = ProposalStatus.SENT,
            customer = "Roger Kyson",
            project = "Basement Finish",
            date = "2026-11-24",
            validUntil = "2026-12-24",
            depositPercentText = "25",
            termsText = "Net 15 upon completion."
        )
        val takeoff = EstimatorEngine.computeTakeoff(
            rooms = emptyList(),
            priceBook = samplePriceBook,
            manualMaterials = emptyMap(),
            discount = 0.0,
            taxPercent = 0.0
        )
        val settingsNoSig = AppSettings(includeSignatureBlockOnProposal = false)

        val html = EstimatePrintHelper.buildEstimateHtml(
            draft = draft,
            takeoff = takeoff,
            priceBook = samplePriceBook,
            settings = settingsNoSig
        )

        assertTrue(html.contains("PROPOSAL"))
        assertTrue(html.contains("THDR-PROP-2026-004"))
        assertTrue(html.contains("Valid Until"))
        assertTrue(html.contains("2026-12-24"))
        assertTrue(html.contains("Total Proposal"))
        assertTrue(html.contains("Net 15 upon completion."))
        assertFalse(html.contains("class='signatures'"))
    }
}
