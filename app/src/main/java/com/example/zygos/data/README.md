# Data Handling Notes

## Database Structure

## Transactions

All transactions should have transaction type, account, and date set.

- *Transfer*: This should always have ticker == "CASH", and the value field with the amount of the transfer.
  All other fields can remain empty. The very first transaction for every account should be a positive transfer.
- *Interest*: This should always have ticker == "CASH", and the value field with the amount of the interest.
  All other fields can remain empty.


## Cash
Cash lots summarize *Transfers* and *Interest*. Every account should have exactly one ticker = "CASH"
lot and position after initial setup.

Lots:
- `openTransactionId` keeps track of when the account was opened/first funded.
- `sharesOpen` is the net value sum of all transfers
- `dividendsPerShare` is all interest
- `realizedClosed` is all cash effects from other transactions

LotPosition:
- `shares` is net transfers
- `price` is -1, to make cash effect come out correct
- `realizedOpen` is all interest
- `realizedClosed` is net cash effect from all stocks
- `returns == realizedOpen`
- `unrealized == 0` since price is fixed
- `returnPercent == 0`
- `cashEffect == equity` is the total cash on hand after all other transactions