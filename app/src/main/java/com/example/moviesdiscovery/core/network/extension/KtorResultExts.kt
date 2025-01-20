package com.example.moviesdiscovery.core.network.extension

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.options
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext

/**
 * Performs an HTTP GET request synchronously and returns the result as a [Result] of type [T].
 *
 * @param urlString The URL for the GET request.
 * @param block Optional, allows customization of the request using HttpRequestBuilder.
 * @return Result<T> representing the synchronous operation.
 */
suspend inline fun <reified T> HttpClient.getResult(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = suspendRunCatching { get(urlString, block).body<T>() }

/**
 * Performs an HTTP POST request synchronously and returns the result as a [Result] of type [T].
 *
 * @param urlString The URL for the POST request.
 * @param block Optional, allows customization of the request using HttpRequestBuilder.
 * @return Result<T> representing the synchronous operation.
 */
suspend inline fun <reified T> HttpClient.postResult(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = suspendRunCatching { post(urlString, block).body() }

/**
 * Performs an HTTP PUT request synchronously and returns the result as a [Result] of type [T].
 *
 * @param urlString The URL for the PUT request.
 * @param block Optional, allows customization of the request using HttpRequestBuilder.
 * @return Result<T> representing the synchronous operation.
 */
suspend inline fun <reified T> HttpClient.putResult(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = suspendRunCatching { put(urlString, block).body() }

/**
 * Performs an HTTP DELETE request synchronously and returns the result as a [Result] of type [T].
 *
 * @param urlString The URL for the DELETE request.
 * @param block Optional, allows customization of the request using HttpRequestBuilder.
 * @return Result<T> representing the synchronous operation.
 */
suspend inline fun <reified T> HttpClient.deleteResult(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = suspendRunCatching { delete(urlString, block).body() }

/**
 * Performs an HTTP PATCH request synchronously and returns the result as a [Result] of type [T].
 *
 * @param urlString The URL for the PATCH request.
 * @param block Optional, allows customization of the request using HttpRequestBuilder.
 * @return Result<T> representing the synchronous operation.
 */
suspend inline fun <reified T> HttpClient.patchResult(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = suspendRunCatching { patch(urlString, block).body() }

/**
 * Performs an HTTP HEAD request synchronously and returns the result as a [Result] of type [T].
 *
 * @param urlString The URL for the HEAD request.
 * @param block Optional, allows customization of the request using HttpRequestBuilder.
 * @return Result<T> representing the synchronous operation.
 */
suspend inline fun <reified T> HttpClient.headResult(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = suspendRunCatching { head(urlString, block).body() }

/**
 * Performs an HTTP OPTIONS request synchronously and returns the result as a [Result] of type [T].
 *
 * @param urlString The URL for the OPTIONS request.
 * @param block Optional, allows customization of the request using HttpRequestBuilder.
 * @return Result<T> representing the synchronous operation.
 */
suspend inline fun <reified T> HttpClient.optionsResult(
    urlString: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): Result<T> = suspendRunCatching { options(urlString, block).body() }

/**
 * Runs a suspending function [block] safely, catching any exceptions that occur during its execution.
 * Returns a [Result] indicating success or failure of the function.
 *
 * @param block the suspending function to be executed safely
 * @return a [Result] indicating success ([Result.success]) or failure ([Result.failure]) of the function
 * @throws CancellationException if the coroutine is cancelled during the execution of [block]
 */
suspend inline fun <R> suspendRunCatching(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (exception: Exception) {
        coroutineContext.ensureActive()
        Log.i(
            "suspendRunCatching",
            "Failed to execute a suspendRunCatchingBlock. Returning failure Result",
            exception
        )
        Result.failure(exception)
    }
}
