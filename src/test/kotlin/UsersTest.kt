import io.restassured.builder.RequestSpecBuilder
import io.restassured.config.LogConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.filter.log.LogDetail
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.hamcrest.Matchers.*
import io.restassured.module.jsv.JsonSchemaValidator.*
import io.restassured.specification.RequestSpecification
import org.apache.http.HttpStatus.SC_OK

class UsersTest {
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

        private var id: Int = 0
        private var token: String = ""

        @JvmStatic
        @BeforeAll
        fun setup() {
            Given {
                spec(authServerSpec)
                body(mapOf(
                    "email" to "admin@authserver.com",
                    "password" to "admin"
                ))
            } When {
                post("/login")
            } Then {
                statusCode(SC_OK)
                body(matchesJsonSchemaInClasspath("login.json"))
            } Extract {
                token = jsonPath().getString("token")
                id = jsonPath().getInt("user.id")
            }
        }

        fun RequestSpecification.loggedAsAdmin(): RequestSpecification =
            header("Authorization", "Bearer $token")
    }

    @Test
    fun `GET must return a valid response`() {
        Given {
            spec(authServerSpec)
        } When {
            get()
        } Then {
            body("size()", greaterThan(0))
            body(matchesJsonSchemaInClasspath("get-users.json"))
        }
    }

    @Test
    fun `GET me should return the logged user`() {
        Given {
            spec(authServerSpec)
            loggedAsAdmin()
        } When {
            get("/me")
        } Then {
            body(matchesJsonSchemaInClasspath("user.json"))
            body("id", `is`(id))
        }
    }
}