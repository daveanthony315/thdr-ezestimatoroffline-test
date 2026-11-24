package com.example.model

enum class EstimateSide(val key: String, val label: String) {
    INTERIOR("interior", "INTERIOR"),
    EXTERIOR("exterior", "EXTERIOR");

    companion object {
        fun fromKey(key: String?): EstimateSide =
            if (key == "exterior") EXTERIOR else INTERIOR
    }
}

data class RoomDimensions(
    val floor: Double,
    val wall: Double,
    val ceiling: Double,
    val perim: Double
) {
    val squares: Double get() = floor / 100.0
}

data class RoomServiceSelection(
    val serviceName: String,
    val selectedPbItemName: String = "",
    val quantity: Double = 0.0,
    val quantityManual: Boolean = false,
    val rate: Double = 0.0,
    val rateInitialized: Boolean = false,
    val scopeNotes: String = "",
    val selectedSubcategories: List<String> = emptyList(),
    val questionAnswers: Map<String, List<String>> = emptyMap()
) {
    val lineTotal: Double get() = quantity * rate
}

data class EstimateRoom(
    val id: String,
    val name: String,
    val side: EstimateSide = EstimateSide.INTERIOR,
    val lengthFt: Double = 0.0,
    val widthFt: Double = 0.0,
    val heightFt: Double = 0.0,
    val fenceLF: Double = 0.0,
    val doors: Int = 0,
    val windows: Int = 0,
    val condition: String = "",
    val roomNotes: String = "",
    val expanded: Boolean = true,
    val services: List<String> = emptyList(),
    val serviceSelections: Map<String, RoomServiceSelection> = emptyMap()
)

data class EstimateDraft(
    val id: Long? = null,
    val customer: String = "",
    val project: String = "",
    val date: String = "",
    val estimateSide: EstimateSide = EstimateSide.INTERIOR,
    val detailsVisible: Boolean = false,
    val rooms: List<EstimateRoom> = emptyList(),
    val manualMaterials: Map<String, Double> = emptyMap(),
    val discountText: String = "0",
    val taxText: String = "0"
)

data class SavedProposal(
    val id: Long = 0,
    val customer: String,
    val project: String,
    val date: String,
    val total: Double,
    val savedAt: String,
    val payloadJson: String
)

data class TakeoffResult(
    val materials: Map<String, Double>,
    val serviceTotal: Double,
    val jobCost: Double,
    val grandTotal: Double,
    val grossProfit: Double,
    val serviceBreakdown: Map<String, Double>
)

data class ServiceQuestionDef(
    val label: String,
    val isMulti: Boolean,
    val options: List<String>
)

data class ServiceDefinition(
    val subcategories: List<String>,
    val questions: List<ServiceQuestionDef>
)

enum class MaterialCalcKind(val title: String) {
    CONCRETE("Concrete — Slabs & Footings"),
    CMU("CMU — Masonry Walls"),
    FRAMING("Framing — Studs & Plates"),
    DRYWALL("Drywall — Sheet Takeoff"),
    PAINT("Paint — Gallons & Openings"),
    FLOORING("Flooring & Tile — Pattern Waste")
}

data class CalcResultRow(
    val key: String,
    val name: String,
    val qty: Double,
    val unit: String,
    val displayQty: String
)

object EstimateCatalog {
    val INTERIOR_ROOM_TYPES = listOf(
        "Kitchen", "Bathroom", "Bedroom", "Living Room",
        "Dining Room", "Hallway", "Laundry", "Basement",
        "Attic", "Garage", "Office", "Other"
    )

    val EXTERIOR_AREA_TYPES = listOf(
        "Deck / Porch", "Fence Line", "Exterior Door / Window", "Exterior Surface"
    )

    val ROOM_CONDITIONS = listOf(
        "New construction", "Good", "Fair", "Poor", "Unknown / inspect"
    )

    val INTERIOR_SERVICES = listOf(
        "Drywall", "Painting", "Flooring", "Tile", "Demolition",
        "Framing", "Fencing", "Roofing", "Insulation", "Plumbing",
        "HVAC", "Electrical", "Trim & Millwork", "Windows & Doors",
        "Cabinetry", "Decks", "Siding"
    )

    val EXTERIOR_SERVICES = listOf(
        "Decks", "Fencing", "Windows & Doors", "Exterior Painting"
    )

    val SERVICE_DEFINITIONS: Map<String, ServiceDefinition> = mapOf(
        "Insulation" to ServiceDefinition(
            subcategories = listOf("Batt insulation", "Blown-in insulation", "Rigid foam insulation", "Spray foam insulation", "Vapor barrier", "Insulation removal"),
            questions = listOf(
                ServiceQuestionDef("Area", false, listOf("Walls", "Ceilings / attic", "Floor / crawlspace", "Multiple areas")),
                ServiceQuestionDef("Existing insulation", false, listOf("None", "Batt", "Blown-in", "Rigid foam", "Unknown")),
                ServiceQuestionDef("Vapor retarder", false, listOf("Required", "Not required", "Verify")),
                ServiceQuestionDef("Access", false, listOf("Open framing", "Attic access", "Crawlspace", "Restricted"))
            )
        ),
        "Plumbing" to ServiceDefinition(
            subcategories = listOf("Fixture replacement", "Rough-in", "Drain / waste / vent", "Water supply lines", "Water heater", "Leak repair", "Bathroom plumbing", "Kitchen plumbing"),
            questions = listOf(
                ServiceQuestionDef("Work type", false, listOf("Repair", "Replacement", "New installation", "Rough-in")),
                ServiceQuestionDef("Fixtures involved", true, listOf("Toilet", "Sink", "Faucet", "Shower", "Tub", "Water heater", "Other")),
                ServiceQuestionDef("Wall / floor access", false, listOf("Open", "Requires opening", "Unknown")),
                ServiceQuestionDef("Permit / inspection", false, listOf("Included", "Not included", "Verify"))
            )
        ),
        "HVAC" to ServiceDefinition(
            subcategories = listOf("Equipment installation", "Ductwork", "Duct modification", "Ventilation", "Bath fan", "Thermostat", "Service / repair"),
            questions = listOf(
                ServiceQuestionDef("System type", false, listOf("Forced air", "Mini-split", "Boiler", "Other / verify")),
                ServiceQuestionDef("Ductwork scope", false, listOf("None", "New", "Modify existing", "Repair")),
                ServiceQuestionDef("Access", false, listOf("Open", "Attic", "Basement / crawlspace", "Restricted")),
                ServiceQuestionDef("Permit / startup", false, listOf("Included", "Not included", "Verify"))
            )
        ),
        "Electrical" to ServiceDefinition(
            subcategories = listOf("Fixture installation", "Switch / receptacle", "New circuit", "Panel work", "Lighting", "Wiring", "Smoke / CO devices"),
            questions = listOf(
                ServiceQuestionDef("Work type", false, listOf("Replacement", "New installation", "Relocation", "Troubleshooting")),
                ServiceQuestionDef("Circuit / panel work", false, listOf("No", "Yes", "Verify")),
                ServiceQuestionDef("Fixtures / devices", false, listOf("1-3", "4-8", "9+", "Count separately")),
                ServiceQuestionDef("Permit / inspection", false, listOf("Included", "Not included", "Verify"))
            )
        ),
        "Trim & Millwork" to ServiceDefinition(
            subcategories = listOf("Baseboard", "Casing", "Crown molding", "Window trim", "Door trim", "Chair rail", "Wainscoting", "Built-ins"),
            questions = listOf(
                ServiceQuestionDef("Measurement basis", false, listOf("Linear feet", "Each / count", "Square feet", "Mixed")),
                ServiceQuestionDef("Material", false, listOf("MDF", "Pine", "Hardwood", "PVC", "Customer supplied", "Other")),
                ServiceQuestionDef("Finish", false, listOf("Unfinished", "Painted", "Stained / clear")),
                ServiceQuestionDef("Removal", false, listOf("No", "Remove existing trim", "Partial removal"))
            )
        ),
        "Windows & Doors" to ServiceDefinition(
            subcategories = listOf("Interior door", "Exterior door", "Prehung door", "Slab door", "Window replacement", "New window opening", "Hardware / lockset", "Casing / trim"),
            questions = listOf(
                ServiceQuestionDef("Quantity", false, listOf("1", "2-3", "4-6", "7+")),
                ServiceQuestionDef("Opening condition", false, listOf("Existing standard opening", "Requires modification", "New opening", "Unknown")),
                ServiceQuestionDef("Unit supplied by", false, listOf("Contractor", "Customer", "Not determined")),
                ServiceQuestionDef("Trim / disposal", false, listOf("Included", "Excluded", "Separate price"))
            )
        ),
        "Cabinetry" to ServiceDefinition(
            subcategories = listOf("Base cabinets", "Wall cabinets", "Tall cabinets", "Vanity", "Cabinet installation", "Cabinet removal", "Hardware", "Countertop coordination"),
            questions = listOf(
                ServiceQuestionDef("Quantity basis", false, listOf("Each cabinet", "Linear feet", "Full kitchen / room", "Field measure")),
                ServiceQuestionDef("Cabinet type", false, listOf("Stock", "Semi-custom", "Custom", "Customer supplied")),
                ServiceQuestionDef("Assembly", false, listOf("Preassembled", "RTA / assemble on site")),
                ServiceQuestionDef("Countertops", false, listOf("Included", "Excluded", "Separate trade"))
            )
        ),
        "Decks" to ServiceDefinition(
            subcategories = listOf("Pressure-treated deck", "Composite deck", "Deck framing", "Deck boards", "Railing", "Stairs", "Footings / posts", "Deck repair"),
            questions = listOf(
                ServiceQuestionDef("Deck dimensions", false, listOf("Enter length and width", "Irregular / multi-level")),
                ServiceQuestionDef("Height above grade", false, listOf("Under 2 ft", "2-4 ft", "4-8 ft", "Over 8 ft")),
                ServiceQuestionDef("Foundation", false, listOf("Concrete footings", "Ground-contact rated supports", "Existing structure", "Verify")),
                ServiceQuestionDef("Railing / stairs", false, listOf("Neither", "Railing", "Stairs", "Both"))
            )
        ),
        "Siding" to ServiceDefinition(
            subcategories = listOf("Vinyl siding", "Fiber cement siding", "Wood siding", "Siding repair", "House wrap", "Soffit", "Fascia", "Aluminum trim"),
            questions = listOf(
                ServiceQuestionDef("Wall area", false, listOf("Measure each elevation", "Use verified total SF", "Repair area only")),
                ServiceQuestionDef("Existing siding removal", false, listOf("No", "One layer", "Multiple layers", "Partial")),
                ServiceQuestionDef("House wrap", false, listOf("Include", "Exclude", "Existing / verify")),
                ServiceQuestionDef("Trim / access", true, listOf("Soffit", "Fascia", "Corner trim", "Second story", "Limited access"))
            )
        ),
        "Drywall" to ServiceDefinition(
            subcategories = listOf("Hang only", "Hang and finish", "Finish existing drywall", "Patch and repair"),
            questions = listOf(
                ServiceQuestionDef("Demo required?", false, listOf("No", "Remove drywall", "Remove plaster and lath", "Partial removal")),
                ServiceQuestionDef("Existing surface", false, listOf("Open framing", "Existing drywall", "Plaster and lath", "Mixed / unknown")),
                ServiceQuestionDef("Finish level", false, listOf("Not applicable - hang only", "Level 3", "Level 4", "Level 5")),
                ServiceQuestionDef("Insulation / vapor barrier?", false, listOf("No", "Insulation", "Vapor barrier", "Both", "Unknown")),
                ServiceQuestionDef("Ceiling work included?", false, listOf("Yes", "No")),
                ServiceQuestionDef("Special conditions", true, listOf("High ceilings", "Stairwell / vaulted", "Fire-rated assembly", "Moisture-resistant board", "Difficult access"))
            )
        ),
        "Painting" to ServiceDefinition(
            subcategories = listOf("Walls only", "Walls and ceiling", "Walls, ceiling and trim", "Ceiling only", "Trim and doors", "Cabinet painting"),
            questions = listOf(
                ServiceQuestionDef("Surface condition", false, listOf("New / unpainted", "Good condition", "Minor repairs", "Heavy repairs / peeling", "Unknown")),
                ServiceQuestionDef("Preparation", true, listOf("Wash / degrease", "Scrape loose paint", "Sand", "Caulk", "Spot prime", "Full prime", "Skim coat")),
                ServiceQuestionDef("Coats", false, listOf("1 coat", "2 coats", "3 coats", "Determine after inspection")),
                ServiceQuestionDef("Paint finish", false, listOf("Flat", "Matte", "Eggshell", "Satin", "Semi-gloss", "Gloss", "Not selected")),
                ServiceQuestionDef("Color change", false, listOf("Similar color", "Light to dark", "Dark to light", "Multiple colors")),
                ServiceQuestionDef("Special conditions", true, listOf("High ceilings", "Stairwell / vaulted", "Occupied room", "Furniture moving", "Lead-paint concern / verify"))
            )
        ),
        "Exterior Painting" to ServiceDefinition(
            subcategories = listOf("Body / siding", "Trim & fascia", "Deck / porch staining", "Fence staining"),
            questions = listOf(
                ServiceQuestionDef("Surface condition", false, listOf("Good condition", "Minor prep", "Heavy scraping / priming", "Unknown")),
                ServiceQuestionDef("Coats", false, listOf("1 coat", "2 coats", "3 coats", "Determine after inspection"))
            )
        ),
        "Flooring" to ServiceDefinition(
            subcategories = listOf("LVP / vinyl plank", "Laminate", "Hardwood", "Engineered wood", "Carpet", "Floor tile", "Subfloor only"),
            questions = listOf(
                ServiceQuestionDef("Existing flooring", false, listOf("Carpet", "Vinyl / sheet goods", "Tile", "Hardwood", "Laminate / LVP", "Bare subfloor", "Multiple layers", "Unknown")),
                ServiceQuestionDef("Removal required?", false, listOf("No", "Remove one layer", "Remove multiple layers", "Partial removal")),
                ServiceQuestionDef("Subfloor condition", false, listOf("Good", "Minor repairs", "Leveling required", "Replacement required", "Unknown - inspect")),
                ServiceQuestionDef("Installation pattern", false, listOf("Straight lay", "Diagonal", "Herringbone / specialty", "Not determined")),
                ServiceQuestionDef("Transitions / baseboards", true, listOf("New transitions", "Reuse transitions", "Remove and reinstall baseboards", "New baseboards", "Undercut jambs")),
                ServiceQuestionDef("Special conditions", true, listOf("Furniture moving", "Stairs", "Occupied home", "Moisture concern", "Uneven floor"))
            )
        ),
        "Tile" to ServiceDefinition(
            subcategories = listOf("Floor tile", "Shower walls", "Tub surround", "Backsplash", "Accent / niche", "Tile repair"),
            questions = listOf(
                ServiceQuestionDef("Tile format", false, listOf("Mosaic / small format", "Standard format", "Large format", "Slab / specialty", "Not selected")),
                ServiceQuestionDef("Substrate", false, listOf("Cement board", "Approved waterproof backer", "Existing tile", "Drywall", "Concrete", "Unknown")),
                ServiceQuestionDef("Waterproofing required?", false, listOf("Yes", "No", "Verify scope")),
                ServiceQuestionDef("Removal required?", false, listOf("No", "Remove tile", "Remove tile and substrate", "Partial removal")),
                ServiceQuestionDef("Layout", false, listOf("Straight lay", "Diagonal", "Pattern / specialty", "Not determined")),
                ServiceQuestionDef("Special conditions", true, listOf("Niche", "Bench", "Outside corners", "Many cuts", "Uneven substrate", "Shower pan / floor"))
            )
        ),
        "Demolition" to ServiceDefinition(
            subcategories = listOf("Selective demolition", "Full room demolition", "Flooring removal", "Drywall / plaster removal", "Tile removal", "Cabinet / fixture removal"),
            questions = listOf(
                ServiceQuestionDef("Material to remove", true, listOf("Drywall", "Plaster and lath", "Flooring", "Tile and backer", "Cabinets", "Fixtures", "Framing", "Other")),
                ServiceQuestionDef("Disposal", false, listOf("Contractor disposal", "Customer dumpster", "On-site pile", "Not included / TBD")),
                ServiceQuestionDef("Protection required", true, listOf("Floor protection", "Dust containment", "Plastic barriers", "Protect adjacent rooms")),
                ServiceQuestionDef("Hidden conditions", false, listOf("No known concerns", "Possible concealed utilities", "Possible water damage", "Unknown - inspect"))
            )
        ),
        "Framing" to ServiceDefinition(
            subcategories = listOf("Partition wall", "Structural framing", "Ceiling framing", "Backing / blocking", "Door or window opening"),
            questions = listOf(
                ServiceQuestionDef("Framing type", false, listOf("Non-load-bearing", "Load-bearing / engineered review", "Ceiling / soffit", "Repair / reinforcement", "Unknown")),
                ServiceQuestionDef("Material", false, listOf("2x4", "2x6", "Engineered lumber", "Mixed / specified later")),
                ServiceQuestionDef("Wall height", false, listOf("8 ft", "9 ft", "10 ft", "Over 10 ft / vaulted")),
                ServiceQuestionDef("Plans / engineering", false, listOf("Not required for known scope", "Plans provided", "Engineering required", "Verify before pricing")),
                ServiceQuestionDef("Special conditions", true, listOf("Tie-in to existing", "Uneven surfaces", "Limited access", "Structural support / shoring"))
            )
        ),
        "Fencing" to ServiceDefinition(
            subcategories = listOf("Wood fence", "Vinyl fence", "Chain link", "Fence repair", "Gate installation"),
            questions = listOf(
                ServiceQuestionDef("Fence height", false, listOf("4 ft", "5 ft", "6 ft", "Other / verify")),
                ServiceQuestionDef("Post setting", false, listOf("Soil", "Concrete / existing surface", "Remove and replace posts", "Unknown")),
                ServiceQuestionDef("Removal required?", false, listOf("No", "Remove existing fence", "Remove posts only")),
                ServiceQuestionDef("Site conditions", true, listOf("Slope", "Rocky ground", "Roots", "Limited access", "Gate / corner posts"))
            )
        ),
        "Roofing" to ServiceDefinition(
            subcategories = listOf("Shingle installation", "Roof tear-off", "Underlayment / ice barrier", "Roof repair", "Flashing"),
            questions = listOf(
                ServiceQuestionDef("Roof pitch", false, listOf("Low slope", "4/12 to 6/12", "7/12 to 9/12", "10/12 or steeper", "Unknown - measure")),
                ServiceQuestionDef("Existing layers", false, listOf("One", "Two", "Three or more", "Unknown")),
                ServiceQuestionDef("Tear-off", false, listOf("No", "One layer", "Two layers", "Three layers / verify")),
                ServiceQuestionDef("Roof details", true, listOf("Valleys", "Chimney", "Skylights", "Dormers", "Steep access", "Complex flashing"))
            )
        )
    )
}
