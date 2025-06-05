package game.bible.common.util.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean

/**
 * Filter to verify JWT on incoming requests
 * @since 5th June 2025
 */
@Component
class TokenFilter(
    private val manager: TokenManager) : GenericFilterBean() {

    @Throws(ServletException::class)
    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain) {
        try {
            chain.doFilter(withClaims(request), response)
        } catch (e: Exception) {
            throw ServletException(e.message)
        }
    }

    /** Extract claims from token and set on request  */
    @Throws(NullPointerException::class)
    private fun withClaims(request: ServletRequest): ServletRequest {
        val token: String = manager.getTokenFrom(request)!!
        request.setAttribute("claims", manager.getClaimsFrom(token))

        return request
    }
}