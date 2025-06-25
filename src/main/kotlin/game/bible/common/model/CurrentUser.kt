package game.bible.common.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Data
import lombok.NoArgsConstructor
import java.io.Serializable

/**
 * Currently Authenticated User
 * @since 25th June 2025
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
class CurrentUser : Serializable {

    @JsonProperty
    private val userId: Long? = null

    companion object {
        private const val serialVersionUID = -406340768243842316L
    }
}