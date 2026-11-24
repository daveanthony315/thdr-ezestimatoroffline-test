package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.EstimatorEngine
import com.example.model.EstimateCatalog
import com.example.model.EstimateRoom
import com.example.model.EstimateSide
import com.example.model.PriceBookItem
import com.example.model.RoomDimensions
import com.example.model.RoomServiceSelection
import com.example.ui.components.SelectablePill
import com.example.ui.components.ThdrDangerButton
import com.example.ui.components.ThdrDoubleField
import com.example.ui.components.ThdrDropdownField
import com.example.ui.components.ThdrSecondaryButton
import com.example.ui.components.ThdrSubCard
import com.example.ui.components.ThdrTextField
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.BlueprintSurfaceHigh
import com.example.ui.theme.BlueprintSurfaceVariant
import com.example.ui.theme.BrandCobalt
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.OutlineBlue
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.WarningAmber
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoomEditorCard(
    roomIndex: Int,
    room: EstimateRoom,
    priceBook: List<PriceBookItem>,
    onToggleExpand: () -> Unit,
    onRemove: () -> Unit,
    onNameChange: (String) -> Unit,
    onDimChange: (Double?, Double?, Double?) -> Unit,
    onFenceLfChange: (Double) -> Unit,
    onOpeningsChange: (Int?, Int?) -> Unit,
    onConditionChange: (String) -> Unit,
    onRoomNotesChange: (String) -> Unit,
    onToggleService: (String) -> Unit,
    onSelectPbItem: (String, String) -> Unit,
    onServiceQtyChange: (String, Double) -> Unit,
    onResetQty: (String) -> Unit,
    onServiceRateChange: (String, Double) -> Unit,
    onServiceScopeNotesChange: (String, String) -> Unit,
    onToggleServiceSub: (String, String) -> Unit,
    onSetQuestionAnswer: (String, String, String, Boolean) -> Unit
) {
    val dims = remember(room.lengthFt, room.widthFt, room.heightFt) {
        EstimatorEngine.computeDimensions(room)
    }
    val exterior = room.side == EstimateSide.EXTERIOR
    val serviceList = if (exterior) EstimateCatalog.EXTERIOR_SERVICES else EstimateCatalog.INTERIOR_SERVICES

    ThdrSubCard(modifier = Modifier.testTag("room_card_$roomIndex")) {
        // Collapsible header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, OutlineBlue),
                    modifier = Modifier.padding(end = 10.dp)
                ) {
                    Icon(
                        imageVector = if (room.expanded) Icons.Default.Remove else Icons.Default.Add,
                        contentDescription = if (room.expanded) "Collapse area" else "Expand area",
                        tint = BrandCyan,
                        modifier = Modifier.padding(4.dp)
                    )
                }
                Text(
                    text = room.name.ifBlank { "Area ${roomIndex + 1}" },
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
            ThdrDangerButton(
                text = "Remove",
                onClick = onRemove,
                testTag = "remove_room_$roomIndex"
            )
        }

        if (room.expanded) {
            Spacer(modifier = Modifier.height(12.dp))

            if (exterior) {
                ExteriorServicePicker(
                    room = room,
                    priceBook = priceBook,
                    serviceList = serviceList,
                    onToggleService = onToggleService,
                    onSelectPbItem = onSelectPbItem
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            ThdrTextField(
                value = room.name,
                onValueChange = onNameChange,
                label = "Room / area name",
                placeholder = "Name this room",
                testTag = "room_name_input_$roomIndex"
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThdrDoubleField(
                    value = room.lengthFt,
                    onValueChange = { onDimChange(it, null, null) },
                    label = "Length (FT)",
                    modifier = Modifier.weight(1f),
                    testTag = "room_length_$roomIndex"
                )
                ThdrDoubleField(
                    value = room.widthFt,
                    onValueChange = { onDimChange(null, it, null) },
                    label = "Width (FT)",
                    modifier = Modifier.weight(1f),
                    testTag = "room_width_$roomIndex"
                )
                ThdrDoubleField(
                    value = room.heightFt,
                    onValueChange = { onDimChange(null, null, it) },
                    label = "Height (FT)",
                    modifier = Modifier.weight(1f),
                    testTag = "room_height_$roomIndex"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val measurementLine = String.format(
                Locale.US,
                "%s %.1f SF · %s %.1f SF · %s %.1f SF · Perimeter %.1f LF · %.2f SQ",
                if (exterior) "Footprint" else "Floor",
                dims.floor,
                if (exterior) "Exterior wall area" else "Walls",
                dims.wall,
                if (exterior) "Roof / overhead footprint" else "Ceiling",
                dims.ceiling,
                dims.perim,
                maxOf(0.0, dims.squares)
            )
            Text(
                text = measurementLine,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.SemiBold
                ),
                color = BrandCyan,
                modifier = Modifier.testTag("room_measurements_$roomIndex")
            )

            if (exterior) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThdrDoubleField(
                        value = room.fenceLF,
                        onValueChange = onFenceLfChange,
                        label = "Fence length (LF)",
                        modifier = Modifier.weight(1f),
                        testTag = "room_fence_lf_$roomIndex"
                    )
                    ThdrDoubleField(
                        value = room.doors.toDouble(),
                        onValueChange = { onOpeningsChange(it.toInt(), null) },
                        label = "Doors",
                        isInteger = true,
                        modifier = Modifier.weight(1f),
                        testTag = "room_ext_doors_$roomIndex"
                    )
                    ThdrDoubleField(
                        value = room.windows.toDouble(),
                        onValueChange = { onOpeningsChange(null, it.toInt()) },
                        label = "Windows",
                        isInteger = true,
                        modifier = Modifier.weight(1f),
                        testTag = "room_ext_windows_$roomIndex"
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThdrDoubleField(
                        value = room.doors.toDouble(),
                        onValueChange = { onOpeningsChange(it.toInt(), null) },
                        label = "Doors (count)",
                        isInteger = true,
                        modifier = Modifier.weight(1f),
                        testTag = "room_doors_$roomIndex"
                    )
                    ThdrDoubleField(
                        value = room.windows.toDouble(),
                        onValueChange = { onOpeningsChange(null, it.toInt()) },
                        label = "Windows (count)",
                        isInteger = true,
                        modifier = Modifier.weight(1f),
                        testTag = "room_windows_$roomIndex"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Overall condition",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EstimateCatalog.ROOM_CONDITIONS.forEach { cond ->
                    SelectablePill(
                        text = cond,
                        selected = room.condition == cond,
                        onClick = { onConditionChange(cond) },
                        testTag = "condition_${roomIndex}_$cond"
                    )
                }
            }

            if (!exterior) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Select services",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    serviceList.forEach { svc ->
                        SelectablePill(
                            text = svc,
                            selected = room.services.contains(svc),
                            onClick = { onToggleService(svc) },
                            testTag = "service_pill_${roomIndex}_$svc"
                        )
                    }
                }
            }

            room.services.forEachIndexed { svcIdx, service ->
                Spacer(modifier = Modifier.height(12.dp))
                ServicePricingCard(
                    roomIndex = roomIndex,
                    serviceIndex = svcIdx,
                    room = room,
                    dims = dims,
                    service = service,
                    priceBook = priceBook,
                    onSelectPbItem = { onSelectPbItem(service, it) },
                    onQtyChange = { onServiceQtyChange(service, it) },
                    onResetQty = { onResetQty(service) },
                    onRateChange = { onServiceRateChange(service, it) },
                    onScopeNotesChange = { onServiceScopeNotesChange(service, it) },
                    onToggleSub = { onToggleServiceSub(service, it) },
                    onSetQuestionAnswer = { qKey, opt, isMulti ->
                        onSetQuestionAnswer(qKey, service, opt, isMulti)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            ThdrTextField(
                value = room.roomNotes,
                onValueChange = onRoomNotesChange,
                label = "Room notes",
                placeholder = "Additional room details...",
                singleLine = false,
                minLines = 2,
                testTag = "room_notes_$roomIndex"
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Accuracy check warning",
                    tint = WarningAmber,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "Accuracy check: unknown conditions, unverified dimensions, structural work, and concealed damage should be verified on site before final pricing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarningAmber
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExteriorServicePicker(
    room: EstimateRoom,
    priceBook: List<PriceBookItem>,
    serviceList: List<String>,
    onToggleService: (String) -> Unit,
    onSelectPbItem: (String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BlueprintSurfaceHigh.copy(alpha = 0.7f))
            .border(1.dp, OutlineBlue, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Exterior services",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary
        )
        Text(
            text = "Choose the services for this work area. Select a service to reveal its Price Book subcategories.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            serviceList.forEach { s ->
                val label = if (s == "Decks") "Decks & Porches" else s
                SelectablePill(
                    text = label,
                    selected = room.services.contains(s),
                    onClick = { onToggleService(s) }
                )
            }
        }
        room.services.forEach { s ->
            val items = EstimatorEngine.pbItemsForService(s, priceBook)
            val selected = room.serviceSelections[s]?.selectedPbItemName.orEmpty()
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "${if (s == "Decks") "Decks & Porches" else s} subcategories",
                style = MaterialTheme.typography.labelMedium,
                color = BrandCyan
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items.forEach { item ->
                    SelectablePill(
                        text = "${item.item} · ${item.unit}",
                        selected = selected == item.item,
                        onClick = { onSelectPbItem(s, item.item) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ServicePricingCard(
    roomIndex: Int,
    serviceIndex: Int,
    room: EstimateRoom,
    dims: RoomDimensions,
    service: String,
    priceBook: List<PriceBookItem>,
    onSelectPbItem: (String) -> Unit,
    onQtyChange: (Double) -> Unit,
    onResetQty: () -> Unit,
    onRateChange: (Double) -> Unit,
    onScopeNotesChange: (String) -> Unit,
    onToggleSub: (String) -> Unit,
    onSetQuestionAnswer: (String, String, Boolean) -> Unit
) {
    val items = remember(service, priceBook) { EstimatorEngine.pbItemsForService(service, priceBook) }
    val sel = room.serviceSelections[service] ?: RoomServiceSelection(serviceName = service)
    var showScopeQuestions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BlueprintSurfaceHigh.copy(alpha = 0.85f))
            .border(1.dp, OutlineBlue, RoundedCornerShape(12.dp))
            .padding(12.dp)
            .testTag("service_box_${roomIndex}_$serviceIndex")
    ) {
        if (items.isEmpty()) {
            Text(
                text = service,
                style = MaterialTheme.typography.titleSmall,
                color = BrandCyan
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "No matching Price Book line items are currently saved for this service. Add or assign a line item in the Price Book.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(8.dp))
            ThdrTextField(
                value = sel.scopeNotes,
                onValueChange = onScopeNotesChange,
                label = "Scope notes",
                singleLine = false,
                minLines = 2
            )
            return@Column
        }

        val chosen = items.firstOrNull { it.item == sel.selectedPbItemName } ?: items.first()
        val basis = chosen.effectiveBasis()
        val isHr = chosen.unit.equals("HR", ignoreCase = true)
        val qty = if (sel.quantityManual) sel.quantity else EstimatorEngine.defaultQty(chosen.unit, dims, room, service, basis)
        val rate = if (sel.rateInitialized) sel.rate else chosen.defaultEstimateRate()
        val lineTotal = qty * rate

        Text(
            text = "$service — Price Book pricing",
            style = MaterialTheme.typography.titleSmall,
            color = BrandCyan
        )
        Spacer(modifier = Modifier.height(8.dp))

        ThdrDropdownField(
            label = "Price Book line item",
            selectedLabel = "${chosen.item} · ${chosen.unit}",
            options = items.map { "${it.item} · ${it.unit}" to it.item },
            onOptionSelected = onSelectPbItem,
            testTag = "pb_item_picker_${roomIndex}_$serviceIndex"
        )

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThdrDoubleField(
                value = qty,
                onValueChange = onQtyChange,
                label = if (isHr) "Hours" else "Qty (${chosen.unit})",
                modifier = Modifier.weight(1f),
                testTag = "service_qty_${roomIndex}_$serviceIndex"
            )
            ThdrDoubleField(
                value = rate,
                onValueChange = onRateChange,
                label = if (isHr) "Hourly rate ($)" else "Price/${chosen.unit} ($)",
                modifier = Modifier.weight(1f),
                testTag = "service_rate_${roomIndex}_$serviceIndex"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${basis.label} · ${chosen.unit}",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Line total: ${EstimatorEngine.formatMoney(lineTotal)}",
                style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMono),
                color = BrandCyan
            )
        }

        if (sel.quantityManual) {
            Spacer(modifier = Modifier.height(8.dp))
            ThdrSecondaryButton(
                text = "Reset quantity to measurement",
                onClick = onResetQty,
                testTag = "reset_qty_${roomIndex}_$serviceIndex"
            )
        }

        if (rate <= 0.0) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "No price is configured for this line item. Set a Price Book rate or enter a rate above.",
                style = MaterialTheme.typography.bodySmall,
                color = WarningAmber
            )
        }

        // Scope subcategories & questionnaire (for Drywall, Painting, Tile, etc.)
        val def = EstimateCatalog.SERVICE_DEFINITIONS[service]
        if (def != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showScopeQuestions = !showScopeQuestions }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Scope options & takeoff details",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentOrange
                )
                Icon(
                    imageVector = if (showScopeQuestions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle scope options",
                    tint = AccentOrange
                )
            }

            if (showScopeQuestions) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Subcategories",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    def.subcategories.forEach { sub ->
                        SelectablePill(
                            text = sub,
                            selected = sel.selectedSubcategories.contains(sub),
                            onClick = { onToggleSub(sub) }
                        )
                    }
                }
                def.questions.forEachIndexed { qIdx, q ->
                    val qKey = "${service}_$qIdx"
                    val current = sel.questionAnswers[qKey].orEmpty()
                    Spacer(modifier = Modifier.height(8.dp))
                    if (q.isMulti) {
                        Text(
                            text = q.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            q.options.forEach { opt ->
                                val checked = current.contains(opt)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BlueprintSurfaceVariant)
                                        .clickable { onSetQuestionAnswer(qKey, opt, true) }
                                        .padding(end = 8.dp)
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { onSetQuestionAnswer(qKey, opt, true) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = BrandCobalt,
                                            checkmarkColor = TextPrimary
                                        )
                                    )
                                    Text(opt, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                }
                            }
                        }
                    } else {
                        ThdrDropdownField(
                            label = q.label,
                            selectedLabel = current.firstOrNull() ?: "Select...",
                            options = listOf("Select..." to "") + q.options.map { it to it },
                            onOptionSelected = { chosenOpt ->
                                onSetQuestionAnswer(qKey, chosenOpt, false)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        ThdrTextField(
            value = sel.scopeNotes,
            onValueChange = onScopeNotesChange,
            label = "Scope notes / exclusions",
            placeholder = "Specific scope, unusual conditions, exclusions...",
            singleLine = false,
            minLines = 2
        )
    }
}
