# Developer Notes

## App Architecture

Following Android guidelines, this app consists of 4 main packages:
* `ui`: All composables exist here, which manifestly include the ui logic thanks to Jetpack Compose.
* `viewModel`: The main interfacing logic between the ui and business logic.  
* `network`: Utilities to fetch market data from the internet
* `data`: Data handling and interface to local storage

### UI Layer

The main entry point is `ZygosApp`. This is the top-level composable that handles navigation and
interactions with the `viewModel`. Remember that the `viewModel` should not be passed down into 
child composables. It also includes bottom sheet and popup dialogs, which need to command the full
screen when they appear. The main composable delegates different tabs using the Android Navigation 
Component. Use the bottom navigation implemented by `ZygosNav` to switch between tabs. There are 4
main tabs:
* `PerformanceScreen`: Shows historical account performance along with a watchlist
* `HoldingsScreen`: Shows current holdings as a pie chart and in a list
* `ChartScreen`: Displays a candlestick graph and statistics for a specific ticker
* `AnalyticsScreen`: For all remaining miscellaneous tasks, such as setting api keys, viewing 
  transaction history, etc.

In addition there are several auxiliary screens. These are not located in the bottom navigation and
are navigated to by click events.
* `ColorSelectorScreen`: Selects a color for a chosen ticker. 
* `TransactionDetails`: Add or edit the details of a single transaction.
* `TransactionsScreen`: A list of all transactions.


### ViewModel Layer

This app uses a single view model of class `ZygosViewModel`. 

### Network Layer


### Data Layer

User data is mostly all stored in a single Room database in `data/database/ZygosDatabase`. The other
classes in that subpackage define the respective SQL tables (Room entities) and DAOs. These are then
processed into holdings, equity, etc. Note that dates are stored as integers in YYYYMMDD format, and dollar values are stored as `Long` in 1/100ths of a cent.

In general, the flow of data occurs as follows:
1. `Transaction`: Created when the user inputs a new transaction
2. `Lot`: Transactions create/update lots. The `lot` table is fully derived from the full list of transactions, and merely caches the computations. The main logic is implemented in `data/TransactionHandler.kt`. Note that each lot is opened by exactly one transaction (and each open transaction matches exactly one lot). However, close and dividend transactions can map to several lots. Thus this is a many-to-many relationship, and is implemented with `LotTransactionCrossRef`. Note that the `LotDao` can return all matching transactions with a lot. Lots are never deleted, even when fully closed.
3. `Position`: Lots are collated to Positions, which represent the current holdings in an account. This is handled by `data/LotsToPositions.kt` Closed lots are simply aggregated by their realized returns. Open lots are combined together by ticker, and lots are combined into a unified position. Option spreads are also created here; in the transactions/lots, they are listed only as single legs. At this level, there is no current market info used. Instead, market-dependent variables like unrealized return are implemented as functions.
4. `PricedPosition`: This is a wrapper around `Position` after snapshotting the market-dependent variables at a specific price. This is what is passed into the UI layer. 

There are also some auxiliary tables stored in the database:
- `ColorSettings`: This stores user settings for the color of a ticker.
- `EquityHistory`: Equity history of each account.
- `Accounts`: These are currently saved in a text file.
- `Ohlc`: Local-cached historical price data?