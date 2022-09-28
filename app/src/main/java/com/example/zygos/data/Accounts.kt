package com.example.zygos.data

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


const val accountNamesFile = "accounts.txt"


fun readAccounts(fileDir: File, filename: String = accountNamesFile): List<String> {
    val file = File(fileDir, filename)
    return if (file.exists()) {
        FileInputStream(file).bufferedReader().readLines()
    } else {
        emptyList()
    }
    // See useLines() to read line by line
}


fun writeAccounts(fileDir: File, accounts: List<String>, filename: String = accountNamesFile) {
    val file = File(fileDir, filename)
    file.bufferedWriter().use { writer ->
        accounts.forEach { account ->
            writer.write(account)
            writer.newLine()
        }
    }
}