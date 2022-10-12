# Developer Notes

## App Architecture

Following Android guidelines, this app consists of 4 main packages:
* `ui`: All composables exist here, which manifestly include the ui logic thanks to Jetpack Compose.
* `viewModel`: The main interfacing logic between the ui and business logic.  
* `network`: Utilities to fetch market data from the internet
* `data`: Data handling and interface to local storage

### UI Layer



### ViewModel Layer


This app uses a single view model of class `ZygosViewModel`. 