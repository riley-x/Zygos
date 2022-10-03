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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zygos.data.database.Transaction
import com.example.zygos.data.database.TransactionType
import com.example.zygos.data.toIntDollar
import com.example.zygos.ui.components.AccountSelector
import com.example.zygos.ui.components.LogCompositions
import com.example.zygos.ui.components.recomposeHighlighter
import com.example.zygos.ui.theme.ZygosTheme
import com.example.zygos.viewModel.TestViewModel


@Composable
fun TransactionDetailsScreen(
    initialTransaction: State<Transaction>,
    accounts: SnapshotStateList<String>,
    modifier: Modifier = Modifier,
    onSave: (Transaction) -> Unit = { },
    onCancel: () -> Unit = { },
) {
    LogCompositions("Zygos", "TransactionDetails")

    val focusManager = LocalFocusManager.current

    val account = remember { mutableStateOf("") }
    val ticker = remember { mutableStateOf("") }
    val note = remember { mutableStateOf("") }
    val type = remember { mutableStateOf(TransactionType.NONE) }
    val shares = remember { mutableStateOf("") }
    val date = remember { mutableStateOf("") }
    val price = remember { mutableStateOf("") }
    val value = remember { mutableStateOf("") }
    val fees = remember { mutableStateOf("") }
    val expiration = remember { mutableStateOf("") }
    val strike = remember { mutableStateOf("") }
    val priceUnderlying = remember { mutableStateOf("") }

    fun toTransaction(): Transaction {
        return Transaction(
            account = account.value,
            ticker = ticker.value,
            note = note.value,
            type = type.value,
            shares = shares.value.toInt(),
            date = date.value.toInt(),
            price = if (price.value.isBlank()) 0 else price.value.toFloat().toIntDollar(),
            value = if (value.value.isBlank()) 0 else value.value.toFloat().toIntDollar(),
//            fees = fees.value.toFloat().toIntDollar(),
            expiration = if (expiration.value.isBlank()) 0 else expiration.value.toInt(),
            strike = if (strike.value.isBlank()) 0 else strike.value.toFloat().toIntDollar(),
            priceUnderlying = if (priceUnderlying.value.isBlank()) 0 else priceUnderlying.value.toFloat().toIntDollar(),
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

    Surface(
        modifier = modifier
            .fillMaxSize()
            .recomposeHighlighter()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            AccountSelector(account = account, accounts = accounts, modifier = Modifier.fillMaxWidth())

            TransactionTypeSelector(type = type.value, modifier = Modifier.fillMaxWidth()) {
                type.value = it
                when (it) {
                    TransactionType.TRANSFER,TransactionType.INTEREST -> {
                        ticker.value = "CASH"
                        shares.value = "0"
                        price.value = "0"
                    }
                    TransactionType.DIVIDEND -> {
                        shares.value = "0"
                    }
                    else -> { }
                }
            }

            TransactionField(date, "Date (YYYYMMDD)", Modifier.fillMaxWidth())

            Row {
                TransactionField(ticker,"Ticker", Modifier.weight(1f), true,
                    enabled = when (type.value) {
                        TransactionType.TRANSFER,TransactionType.INTEREST -> false
                        else -> true
                    }
                )
                Spacer(Modifier.width(6.dp))
                TransactionField(shares, "Shares", Modifier.weight(1f),
                    enabled = when (type.value) {
                        TransactionType.TRANSFER,TransactionType.INTEREST,
                        TransactionType.DIVIDEND -> false
                        else -> true
                    }
                )
            }

            Row {
                TransactionField(value, "Value", Modifier.weight(1f))
                Spacer(Modifier.width(6.dp))
                TransactionField(price, "Price", Modifier.weight(1f),
                    enabled = when (type.value) {
                        TransactionType.TRANSFER,TransactionType.INTEREST -> false
                        else -> true
                    }
                )
            }

            if (type.value.isOption) {
                TransactionField(strike, "Strike", Modifier.fillMaxWidth())
                TransactionField(expiration, "Expiration (YYYYMMDD)", Modifier.fillMaxWidth())
                TransactionField(priceUnderlying, "Price of Underlying", Modifier.fillMaxWidth())
            }

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
                    Text("Save")
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