import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import ross.rosstudent.module

class ApplicationTest {
    
    @Test
    fun testRootEndpoint() = testApplication {
        application {
            module()
        }
        
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello World!", response.bodyAsText())
    }
    
    @Test
    fun testHealthEndpoint() = testApplication {
        application {
            module()
        }
        
        val response = client.get("/api/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("API is healthy", response.bodyAsText())
    }
    
    @Test
    fun testStatusEndpoint() = testApplication {
        application {
            module()
        }
        
        val response = client.get("/api/status") 
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("API is running successfully", response.bodyAsText())
    }
    
    @Test
    fun testVersionEndpoint() = testApplication {
        application {
            module()
        }
        
        val response = client.get("/api/version")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Version: 0.0.1", response.bodyAsText())
    }
    
    @Test
    fun testNonExistentEndpoint() = testApplication {
        application {
            module()
        }
        
        val response = client.get("/api/nonexistent")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
    
    @Test
    fun testAllEndpointsWork() = testApplication {
        application {
            module()
        }
        
        val endpoints = mapOf(
            "/" to "Hello World!",
            "/api/health" to "API is healthy",
            "/api/status" to "API is running successfully",
            "/api/version" to "Version: 0.0.1"
        )
        
        for ((endpoint, expectedText) in endpoints) {
            val response = client.get(endpoint)
            assertEquals(HttpStatusCode.OK, response.status, "Failed for endpoint: $endpoint")
            assertEquals(expectedText, response.bodyAsText(), "Wrong response for endpoint: $endpoint")
        }
    }
}
