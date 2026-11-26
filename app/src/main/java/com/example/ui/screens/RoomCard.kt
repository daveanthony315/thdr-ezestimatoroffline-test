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
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.LocalThdrPalette
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
    onSelectPricingUnit: (String, String) -> Unit,
    onSelectMeasurementBasis: (String, String) -> Unit,
    onServiceQtyChange: (String, Double) -> Unit,
    onResetQty: (String) -> Unit,
    onServiceRateChange: (String, Double) -> Unit,
    onServiceScopeNotesChange: (String, String) -> Unit,
    onToggleServiceSub: (String, String) -> Unit,
    onSetQuestionAnswer: (String, String, String, Boolean) -> Unit
) {
    val palette = LocalThdrPalette.current
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
                    border = BorderStroke(1.dp, palette.outline),
                    modifier = Modifier.padding(end = 10.dp)
                ) {
                    Icon(
                        imageVector = if (room.expanded) Icons.Default.Remove else Icons.Default.Add,
                        contentDescription = if (room.expanded) "Collapse area" else "Expand area",
                        tint = palette.brandHighlight,
                        modifier = Modifier.padding(4.dp)
                    )
                }
                Text(
                    text = room.name.ifBlank { "Area ${roomIndex + 1}" },
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary
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
                color = palette.brandHighlight,
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
                color = palette.textMuted
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
                    color = palette.textPrimary
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
                    onSelectPricingUnit = { onSelectPricingUnit(service, it) },
                    onSelectMeasurementBasis = { onSelectMeasurementBasis(service, it) },
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
                    tint = palette.warning,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "Accuracy check: unknown conditions, unverified dimensions, structural work, and concealed damage should be verified on site before final pricing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.warning
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
    val palette = LocalThdrPalette.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.surfaceHigh.copy(alpha = 0.72f))
            .border(1.dp, palette.outline, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Exterior services",
            style = MaterialTheme.typography.titleSmall,
            color = palette.textPrimary
        )
        Text(
            text = "Choose the services for this work area. Select a service to reveal its Price Book subcategories.",
            style = MaterialTheme.typography.bodySmall,
            color = palette.textMuted
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
                color = palette.brandHighlight
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
