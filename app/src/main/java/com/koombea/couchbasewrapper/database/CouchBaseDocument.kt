package com.koombea.couchbasewrapper.database

data class CouchBaseDocument <T>(
    val id: String,
    val attributes: T
)
