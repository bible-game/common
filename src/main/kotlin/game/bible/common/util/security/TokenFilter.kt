package game.bible.common.util.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.startsWith
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filter to verify JWT on incoming requests
 * @since 5th June 2025
 */
@Component
class TokenFilter(
    private val manager: TokenManager) : OncePerRequestFilter() {

    private val excludes: MutableSet<String> = mutableSetOf()

    fun excludes(exclusions: List<String>?): TokenFilter {
        if (exclusions != null) {
            for (exclusion in exclusions)
                this.excludes.add(exclusion)
        }
        return this
    }

    @Throws(ServletException::class)
    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        try {
            val url = StringUtils.substringAfter(req.requestURI, req.contextPath)
            if (excludes.stream().anyMatch { exclusion -> startsWith(url, exclusion) }) {
                chain.doFilter(req, res)
                return
            }

            val token = manager.getTokenFrom(req)!!
            val claims = manager.getClaimsFrom(token)
            req.setAttribute("claims", claims)

            val auth = SecurityContextHolder.getContext().authentication
            val userId = (claims as Map<*, *>)["sub"]

            if (userId != null && auth == null) {
                val authToken = UsernamePasswordAuthenticationToken(userId, null, listOf())
                SecurityContextHolder.getContext().authentication = authToken
            }

            chain.doFilter(req, res)

        } catch (e: Exception) {
            throw ServletException(e.message)
        }
    }

}