package game.bible.common.model

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Version
import jakarta.persistence.GenerationType.IDENTITY
import java.io.Serializable
import java.sql.Timestamp
import lombok.Getter
import lombok.Setter

/**
 * Base Model Entity
 *
 * @author J. R. Smith
 * @since 7th December 2024
 */
@MappedSuperclass @Getter @Setter
abstract class BaseEntity : Serializable {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private val id: Long? = null

    @Version
    @Column(name = "last_modified", nullable = false)
    protected var lastModified: Timestamp? = null

    @Column(name = "created_date", nullable = false, updatable = false)
    protected var createdDate: Timestamp = Timestamp(System.currentTimeMillis())

}