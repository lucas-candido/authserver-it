import io.restassured.builder.RequestSpecBuilder
import io.restassured.config.LogConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.filter.log.LogDetail
import io.restassured.http.ContentType
import io.restassured.module.jsv.JsonSchemaValidator
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.specification.RequestSpecification
import org.apache.http.HttpStatus
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class OrdersTest {
    companion object {
        private val authServerSpec = RequestSpecBuilder()
            .setBaseUri("http://localhost")
            .setPort(8080)
            .setBasePath("/api/users")
            .setContentType(ContentType.JSON)
            .setRelaxedHTTPSValidation()
            .setConfig(
                RestAssuredConfig()
                    .logConfig(
                        LogConfig().enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)
                    ))
            .build()

        private val authServerOrdersSpec = RequestSpecBuilder()
            .setBaseUri("http://localhost")
            .setPort(8080)
            .setBasePath("/api/orders")
            .setContentType(ContentType.JSON)
            .setRelaxedHTTPSValidation()
            .setConfig(
                RestAssuredConfig()
                    .logConfig(
                        LogConfig().enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)
                    ))
            .build()

        private var id: Int = 0
        private var token: String = ""

        @JvmStatic
        @BeforeAll
        fun setup() {
            Given {
                spec(authServerSpec)
                body(mapOf(
                    "email" to "vendas@authserver.com",
                    "password" to "vendas"
                ))
            } When {
                post("/login")
            } Then {
                statusCode(HttpStatus.SC_OK)
                body(JsonSchemaValidator.matchesJsonSchemaInClasspath("login.json"))
            } Extract {
                token = jsonPath().getString("token")
                id = jsonPath().getInt("user.id")
            }
        }

        fun RequestSpecification.loggedAsSalesUser(): RequestSpecification =
            header("Authorization", "Bearer $token")
    }

    @Test
    fun `Should create a new order`() {
        Given {
            val request = "{\"userId\": 2,\"serviceId\": 2}"

            spec(authServerOrdersSpec)
            loggedAsSalesUser()
            body(request)
        } When {
            post("/")
        } Then {
            statusCode(HttpStatus.SC_CREATED)
        }
    }
}