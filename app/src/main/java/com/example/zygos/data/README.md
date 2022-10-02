# Data Handling Notes

## Database Structure

## Transactions

All transactions should have transaction type, account, and date set.

- *Transfer*: This should always have ticker == "CASH", and the value field with the amount of the transfer.
  All other fields can remain empty. The very first transaction for every account should be a positive transfer.
- *Interest*: This should always have ticker == "CASH", and the value field with the amount of the interest.
  All other fields can remain empty.

## Lots

WARNING: `realizedOpen` and `realizedClosed` have special use for CASH lots!

- *CASH*: Cash lots summarize *Transfers* and *Interest*. Every account should have exactly one CASH lot
  after initial setup. The `openTransactionId` keeps track of when the account was opened/first funded.
  `realizedOpen` is the net value sum of all transfers, while `realizedClosed` is the net sum of all
  other transactions. `sharesOpen` is always 1.