package game.bible.common.util.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import game.bible.config.model.core.SecurityConfig
import jakarta.servlet.ServletRequest
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.time.ZonedDateTime
import java.util.Date
import javax.crypto.SecretKey

/**
 * Utility to manage auth tokens across the platform
 * @since 5th June 2025
 */
@Component
class TokenManager(private val config: SecurityConfig) {

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

    private val key: SecretKey
        /** Retrieve key from secret  */
        get() = Keys.hmacShaKeyFor(Decoders.BASE64.decode(config.getJwt()!!.getSigningSecret()))

    /** Retrieve the claims from a token  */
    fun getClaimsFrom(token: String?): Any {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload()
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

    companion object {
        private const val BEARER_ = "Bearer "
    }
}