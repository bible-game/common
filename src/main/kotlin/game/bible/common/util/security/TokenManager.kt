package game.bible.common.util.security

import com.fasterxml.jackson.databind.ObjectMapper
import game.bible.config.model.core.SecurityConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import jakarta.servlet.ServletRequest
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.apache.commons.lang3.StringUtils.isBlank
import org.apache.commons.lang3.StringUtils.isNotBlank
import org.apache.commons.lang3.StringUtils.startsWith
import org.apache.commons.lang3.StringUtils.substringAfter
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.time.ZonedDateTime
import java.util.Arrays
import java.util.Date
import java.util.Optional
import java.util.Base64
import javax.crypto.SecretKey

private val log = KotlinLogging.logger {}

/**
 * Utility to manage auth tokens across the platform
 * @since 5th June 2025
 */
@Component
class TokenManager(
    private val config: SecurityConfig,
    private val mapper: ObjectMapper,
    private val request: HttpServletRequest
) {

    /** Retrieve key from secret  */
    private val key: SecretKey
    get() = Keys.hmacShaKeyFor(
        Decoders.BASE64.decode(config.getJwt()!!.getSigningSecret())
    )

    fun getToken(): String? {
        val jwt = config.getJwt()
        checkNotNull(jwt) { "Security configuration is missing jwt" }

        var token: String? = null
        if (isNotBlank(jwt.getCookieName()) && request.cookies != null) {
            log.debug { "Finding auth cookie: " + jwt.getCookieName() }
            token = Arrays.stream(request.cookies)
                .filter { c: Cookie -> jwt.getCookieName().equals(c.name) }
                .findFirst()
                .map { obj: Cookie -> obj.value }
                .orElse(null)
        }

        if (isBlank(token)) {
            log.debug { "Finding auth header: Authorization" }
            token = request.getHeader("Authorization")
        }

        if (isBlank(token) && isNotBlank(jwt.getAuthTokenHeader())) {
            log.debug { "Finding auth header [${jwt.getAuthTokenHeader()}]"}
            token = request.getHeader(jwt.getAuthTokenHeader())
        }

        if (isBlank(token)) {
            log.warn { "Token not found as a header in the request!" }
            return null
        }

        if (startsWith(token, BEARER_)) {
            log.debug { "Finding auth param [$BEARER_]" }
            token = substringAfter(token, BEARER_)
        }

        return token
    }

    /** Generate a JWT token for given user  */
    fun generateFor(userId: Long): String {
        return Jwts.builder()
            .expiration(Date.from(ZonedDateTime.now().plusMinutes(config.getJwt()!!.getSessionTimeoutMins()!!.toLong()).toInstant()))
            .issuedAt(Date())
            .issuer(config.getJwt()!!.getCookieDomain())
            .signWith(key)
            .subject(userId.toString())
            .audience().add(config.getJwt()!!.getCookieDomain())
            .and().compact()
    }

    /** Retrieve the claims from a token  */
    fun getClaimsFrom(token: String?): Any {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    /** Retrieve token from [ServletRequest]  */
    fun getTokenFrom(request: ServletRequest): String? {
        val httpRequest: HttpServletRequest = request as HttpServletRequest
        val bearerToken: String = httpRequest.getHeader("Authorization")

        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_)) bearerToken.substring(
            BEARER_.length
        )
        else null
    }

    /** Retrieves a field from the [Claims] by type */
    fun <T> get(field: String, type: Class<T>?): T? {
        return get(getToken(), field, type)
    }

    /** Retrieves a field from the [Claims] by type */
    fun <T> get(token: String?, field: String, type: Class<T>?): T? {
        val claims: Claims = getClaims(token)

        val value: Optional<*> = Optional.ofNullable(claims[field])
        if (value.isEmpty) {
            return null
        }
        try {
            return mapper.convertValue<T>(value.get(), type)
        } catch (e: Exception) {
            throw IllegalStateException("Error parsing claims field: $field does not match expected type: $type", e)
        }
    }

    fun getClaims(): Claims {
        return getClaims(getToken())
    }

    fun getClaims(token: String?): Claims {
        val secretKey: SecretKey = generateSecretKey()
        val jwtParser = Jwts.parser()
            .verifyWith(secretKey)
            .build()

        return jwtParser.parseSignedClaims(token).payload
    }

    private fun generateSecretKey(): SecretKey {
        val secret = config.getJwt()?.getSigningSecret()
        val decodedKey: ByteArray = Base64.getDecoder().decode(secret)
        return Keys.hmacShaKeyFor(decodedKey)
    }

    companion object {
        private const val BEARER_ = "Bearer "
    }

}