package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.EstimatorEngine
import com.example.model.Customer
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.ThdrCard
import com.example.ui.components.ThdrDangerButton
import com.example.ui.components.ThdrPrimaryButton
import com.example.ui.components.ThdrSecondaryButton
import com.example.ui.components.ThdrSubCard
import com.example.ui.components.ThdrTextField
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.LocalThdrPalette
import com.example.viewmodel.EstimatorUiState
import com.example.viewmodel.EstimatorViewModel

@Composable
fun CustomersScreen(
    state: EstimatorUiState,
    viewModel: EstimatorViewModel,
    onStartEstimateForCustomer: (Customer) -> Unit,
    onStartProposalForCustomer: (Customer) -> Unit
) {
    val palette = LocalThdrPalette.current
    val currency = state.settings.currencySymbol
    var searchQuery by remember { mutableStateOf("") }
    var editingCustomer by remember { mutableStateOf<Customer?>(null) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }

    val filteredCustomers = remember(state.customers, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) state.customers
        else state.customers.filter {
            it.name.contains(q, ignoreCase = true) ||
                it.company.contains(q, ignoreCase = true) ||
                it.phone.contains(q, ignoreCase = true) ||
                it.email.contains(q, ignoreCase = true) ||
                it.address.contains(q, ignoreCase = true)
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
                    Text(
                        text = "Customers",
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Save homeowner and commercial client profiles for one-tap Estimate and Proposal creation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ThdrPrimaryButton(
                        text = "+ Add Customer",
                        onClick = { editingCustomer = Customer(name = "") },
                        testTag = "add_customer_btn",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ThdrTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = "Search customers by name, company, phone, or address",
                        testTag = "customers_search"
                    )
                }
            }
        }

        if (filteredCustomers.isEmpty()) {
            item {
                Box(modifier = Modifier.widthIn(max = 980.dp)) {
                    ThdrCard {
                        ThdrSubCard {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.thdr_mascot_badge),
                                    contentDescription = "House DR Mascot",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(68.dp)
                                )
                                Text(
                                    text = if (state.customers.isEmpty()) {
                                        "No customers added yet. Tap \"+ Add Customer\" to build your client directory."
                                    } else {
                                        "No customers match \"$searchQuery\"."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.textMuted,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            items(filteredCustomers, key = { it.id }) { customer ->
                val linkedDocs = remember(state.savedProposals, customer.name) {
                    state.savedProposals.filter {
                        it.customer.equals(customer.name, ignoreCase = true)
                    }
                }
                val totalQuoted = remember(linkedDocs) { linkedDocs.sumOf { it.total } }

                Box(modifier = Modifier.widthIn(max = 980.dp)) {
                    ThdrCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = customer.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = palette.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (customer.company.isNotBlank()) {
                                    Text(
                                        text = customer.company,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = palette.brandHighlight
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${linkedDocs.size} doc(s)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textMuted
                                )
                                Text(
                                    text = EstimatorEngine.formatMoney(totalQuoted, currency),
                                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMono),
                                    color = palette.brandHighlight
                                )
                            }
                        }

                        val contactLines = listOfNotNull(
                            customer.phone.takeIf { it.isNotBlank() }?.let { "Phone: $it" },
                            customer.email.takeIf { it.isNotBlank() }?.let { "Email: $it" },
                            customer.address.takeIf { it.isNotBlank() }?.let { "Address: $it" },
                            customer.notes.takeIf { it.isNotBlank() }?.let { "Notes: $it" }
                        )
                        if (contactLines.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ThdrSubCard {
                                contactLines.forEach { line ->
                                    Text(
                                        text = line,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.textMuted
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThdrPrimaryButton(
                                text = "New Estimate",
                                onClick = { onStartEstimateForCustomer(customer) },
                                testTag = "customer_new_estimate_${customer.id}",
                                modifier = Modifier.weight(1f)
                            )
                            ThdrPrimaryButton(
                                text = "New Proposal",
                                onClick = { onStartProposalForCustomer(customer) },
                                orangeAccent = true,
                                testTag = "customer_new_proposal_${customer.id}",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThdrSecondaryButton(
                                text = "Edit Details",
                                onClick = { editingCustomer = customer },
                                testTag = "customer_edit_${customer.id}",
                                modifier = Modifier.weight(1f)
                            )
                            ThdrDangerButton(
                                text = "Delete",
                                onClick = { customerToDelete = customer },
                                testTag = "customer_delete_${customer.id}",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

    editingCustomer?.let { draftCustomer ->
        CustomerEditorDialog(
            initial = draftCustomer,
            onSave = { saved ->
                viewModel.saveCustomer(saved)
                editingCustomer = null
            },
            onDismiss = { editingCustomer = null }
        )
    }

    customerToDelete?.let { target ->
        ConfirmActionDialog(
            title = "Delete Customer?",
            message = "Remove \"${target.name}\" from your Customers directory? Saved estimates and proposals will remain untouched.",
            confirmText = "Delete",
            onConfirm = { viewModel.deleteCustomer(target.id) },
            onDismiss = { customerToDelete = null }
        )
    }
}

@Composable
private fun CustomerEditorDialog(
    initial: Customer,
    onSave: (Customer) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalThdrPalette.current
    var name by remember(initial) { mutableStateOf(initial.name) }
    var company by remember(initial) { mutableStateOf(initial.company) }
    var phone by remember(initial) { mutableStateOf(initial.phone) }
    var email by remember(initial) { mutableStateOf(initial.email) }
    var address by remember(initial) { mutableStateOf(initial.address) }
    var notes by remember(initial) { mutableStateOf(initial.notes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        titleContentColor = palette.textPrimary,
        textContentColor = palette.textPrimary,
        title = {
            Text(
                text = if (initial.id == 0L) "Add Customer" else "Edit Customer",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThdrTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Customer full name *",
                    testTag = "customer_name_field"
                )
                ThdrTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = "Company / property manager",
                    testTag = "customer_company_field"
                )
                ThdrTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Phone number",
                    testTag = "customer_phone_field"
                )
                ThdrTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email address",
                    testTag = "customer_email_field"
                )
                ThdrTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Job site / billing address",
                    testTag = "customer_address_field"
                )
                ThdrTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Customer notes (gate code, preferences)",
                    singleLine = false,
                    minLines = 2,
                    testTag = "customer_notes_field"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            initial.copy(
                                name = name.trim(),
                                company = company.trim(),
                                phone = phone.trim(),
                                email = email.trim(),
                                address = address.trim(),
                                notes = notes.trim()
                            )
                        )
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                modifier = Modifier.testTag("customer_dialog_save_btn")
            ) {
                Text("Save Customer", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = palette.textMuted)
            }
        }
    )
}
