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