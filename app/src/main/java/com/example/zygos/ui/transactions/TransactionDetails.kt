package com.example.zygos.ui.transactions

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.data.database.Transaction
import com.example.zygos.data.database.TransactionType
import com.example.zygos.data.database.allTransactionTypes
import com.example.zygos.data.toFloatDollar
import com.example.zygos.data.toLongDollar
import com.example.zygos.ui.components.*
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.TestViewModel
import kotlin.reflect.KProperty1


@Composable
fun TransactionDetailsScreen(
    initialTransaction: State<Transaction>,
    accounts: SnapshotStateList<String>,
    modifier: Modifier = Modifier,
    onSave: (Transaction) -> Unit = { },
    onCancel: () -> Unit = { },
) {
    LogCompositions("Zygos", "TransactionDetails")

    fun toState(field: KProperty1<Transaction, Int>): MutableState<String> {
        val x = field.get(initialTransaction.value)
        return mutableStateOf(if (x == 0) "" else x.toString())
    }
    fun toState(field: KProperty1<Transaction, Long>, isDollar: Boolean = false): MutableState<String> {
        val x = field.get(initialTransaction.value)
        return mutableStateOf(
            if (x == 0L) ""
            else if (isDollar) x.toFloatDollar().toString()
            else x.toString()
        )
    }

    val account = remember { mutableStateOf(initialTransaction.value.account) }
    val ticker = remember { mutableStateOf(initialTransaction.value.ticker) }
    val note = remember { mutableStateOf(initialTransaction.value.note) }
    val type = remember { mutableStateOf(initialTransaction.value.type) }
    val shares = remember { toState(Transaction::shares) }
    val date = remember { toState(Transaction::date) }
    val price = remember { toState(Transaction::price, true) }
    val value = remember { toState(Transaction::value, true) }
    val fees = remember { mutableStateOf("") }
    val expiration = remember { toState(Transaction::expiration) }
    val strike = remember { toState(Transaction::strike, true) }
    val priceUnderlying = remember { toState(Transaction::priceUnderlying, true) }

    fun toValue(x: State<String>, isDollar: Boolean = false) : Long {
        return if (x.value.isBlank()) 0
        else if (isDollar) x.value.toFloat().toLongDollar()
        else x.value.toLong()
    }

    fun toTransaction(): Transaction {
        return Transaction(
            transactionId = initialTransaction.value.transactionId,
            account = account.value,
            ticker = ticker.value,
            note = note.value,
            type = type.value,
            shares = toValue(shares),
            date = toValue(date).toInt(),
            price = toValue(price, true),
            value = toValue(value, true),
//            fees = fees.value.toFloat().toIntDollar(),
            expiration = toValue(expiration).toInt(),
            strike = toValue(strike, true),
            priceUnderlying = toValue(priceUnderlying, true),
        )
    }

    val context = LocalContext.current
    fun save() {
        try {
            onSave(toTransaction())
            onCancel() // to exit
        } catch (e: NumberFormatException) {
            Toast.makeText(context, "Invalid transaction", Toast.LENGTH_SHORT).show()
        }
    }

    fun defaultFields(t: TransactionType) {
        when (t) {
            TransactionType.TRANSFER,TransactionType.INTEREST -> {
                ticker.value = "CASH"
                shares.value = ""
                price.value = ""
            }
            TransactionType.DIVIDEND -> {
                shares.value = ""
            }
            else -> { }
        }
        if (!t.isOption) {
            if (t != TransactionType.DIVIDEND) expiration.value = ""
            priceUnderlying.value = ""
            strike.value = ""
        }
    }


    Surface(
        modifier = modifier
            .fillMaxSize()
            .recomposeHighlighter()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            DropdownSelector(
                currentValue = account.value,
                allValues = ImmutableList(accounts.toList()),
                label = "Account",
                onSelection = { account.value = it },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownSelector(
                currentValue = type.value,
                allValues = allTransactionTypes,
                modifier = Modifier.fillMaxWidth(),
                label = "Type",
            ) {
                type.value = it
                defaultFields(it)
            }

            TransactionField(date, "Date (YYYYMMDD)", Modifier.fillMaxWidth())

            Row {
                when (type.value) {
                    TransactionType.TRANSFER,TransactionType.INTEREST -> { }
                    else -> {
                        TransactionField(ticker, "Ticker", Modifier.weight(1f), true)
                    }
                }

                when (type.value) {
                    TransactionType.TRANSFER, TransactionType.INTEREST,
                    TransactionType.DIVIDEND -> { }
                    else -> {
                        Spacer(Modifier.width(6.dp))
                        TransactionField(shares, "Shares", Modifier.weight(1f))
                    }
                }
            }

            Row {
                TransactionField(value, "Value", Modifier.weight(1f))
                when (type.value) {
                    TransactionType.TRANSFER, TransactionType.INTEREST -> { }
                    else -> {
                        Spacer(Modifier.width(6.dp))
                        TransactionField(price, "Price", Modifier.weight(1f))
                    }
                }
            }

            if (type.value.isOption) TransactionField(strike, "Strike", Modifier.fillMaxWidth())
            if (type.value.isOption || type.value == TransactionType.DIVIDEND)
                TransactionField(expiration, "Expiration (YYYYMMDD)", Modifier.fillMaxWidth())
            if (type.value.isOption) TransactionField(priceUnderlying, "Price of Underlying", Modifier.fillMaxWidth())

            TransactionField(note,"Note", Modifier.fillMaxWidth(), true)

            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.error,
                    )
                ) {
                    Text("Cancel")
                }

                Button(onClick = ::save) {
                    Text(if (initialTransaction.value.type == TransactionType.NONE) "Save" else "Update")
                }
            }
        }
    }
}

@Composable
fun TransactionField(
    state: MutableState<String>,
    label: String,
    modifier: Modifier = Modifier,
    isText: Boolean = false,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = state.value,
        enabled = enabled,
        label = { Text(label) },
        onValueChange = { state.value = it },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            keyboardType = if (isText) KeyboardType.Text else KeyboardType.Decimal,
            autoCorrect = false,
            imeAction = ImeAction.Next,
        ),
        modifier = modifier
    )
}


@Preview(
    widthDp = 360,
    heightDp = 740,
    showBackground = true,
)
@Composable
fun PreviewTransactionDetailsScreen() {
    val viewModel = viewModel<TestViewModel>()
    ZygosTheme {
        TransactionDetailsScreen(
            initialTransaction = viewModel.focusedTransaction,
            accounts = viewModel.accounts
        )
    }
}