package com.example.moviesdiscovery.core.network.model

class ApiErrorException(message: String, val code: Int) : Exception(message)

