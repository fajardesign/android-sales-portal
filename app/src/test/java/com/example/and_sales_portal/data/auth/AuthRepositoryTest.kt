package com.example.and_sales_portal.data.auth

import io.github.jan.supabase.createSupabaseClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

/** Error mapping of the login (PRD v3 "Login on mobile" error table). */
class AuthRepositoryTest {

    private val cache = object : ProfileCache {
        override var salesSession: SalesSession? = null
    }

    private fun repo(engine: MockEngine) = AuthRepository(
        supabase = createSupabaseClient("https://example.supabase.co", "key") {},
        store = cache,
        baseUrl = "https://example.supabase.co",
        publishableKey = "key",
        http = HttpClient(engine) { expectSuccess = false },
    )

    private fun errorResponse(status: HttpStatusCode, code: String) = MockEngine {
        respond("""{"error":"$code"}""", status, headersOf("Content-Type", "application/json"))
    }

    private suspend fun failureOf(engine: MockEngine) =
        (repo(engine).login("andi.pratama", "x") as LoginResult.Failure).error

    @Test
    fun mapsBackendErrorCodes() = runTest {
        assertEquals(LoginError.Invalid, failureOf(errorResponse(HttpStatusCode.Unauthorized, "INVALID")))
        assertEquals(LoginError.Locked, failureOf(errorResponse(HttpStatusCode.Locked, "LOCKED")))
        assertEquals(LoginError.Disabled, failureOf(errorResponse(HttpStatusCode.Forbidden, "DISABLED")))
        assertEquals(LoginError.NotActivated, failureOf(errorResponse(HttpStatusCode.Forbidden, "NOT_ACTIVATED")))
        assertEquals(LoginError.ForbiddenPlatform, failureOf(errorResponse(HttpStatusCode.Forbidden, "FORBIDDEN_PLATFORM")))
        assertEquals(LoginError.BadRequest, failureOf(errorResponse(HttpStatusCode.BadRequest, "BAD_REQUEST")))
    }

    @Test
    fun serverAndNetworkFailures() = runTest {
        assertEquals(LoginError.Server, failureOf(MockEngine { respondError(HttpStatusCode.BadGateway) }))
        assertEquals(LoginError.Server, failureOf(MockEngine { respond("<html>", HttpStatusCode.OK) }))
        assertEquals(LoginError.Offline, failureOf(MockEngine { throw IOException("offline") }))
    }

    @Test
    fun rejectsUserWithoutSalesRole() = runTest {
        val body = """{"session":{},"user":{"id":3,"full_name":"Admin","role":"ADMIN","username":"admin","email":"a@b.c","status":"ACTIVE"},
            "access":{"realmRole":"ADMIN","platform":"sales-app-access","features":[]}}"""
        val engine = MockEngine { respond(body, HttpStatusCode.OK, headersOf("Content-Type", "application/json")) }
        assertEquals(LoginError.ForbiddenPlatform, failureOf(engine))
    }
}
