package game.bible.common.model

import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType.IDENTITY
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import java.io.Serializable
import java.sql.Timestamp
import lombok.Getter
import lombok.Setter
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate

/**
 * Base Model Entity
 *
 * @author J. R. Smith
 * @since 7th December 2024
 */
@MappedSuperclass @Getter @Setter
abstract class BaseEntity : Serializable {

    @Id @GeneratedValue(strategy = IDENTITY)
    private var id: Long? = null

    @LastModifiedDate
    @Column(name = "last_modified", nullable = false)
    protected var lastModified: Timestamp = Timestamp(System.currentTimeMillis())

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    protected var createdDate: Timestamp = Timestamp(System.currentTimeMillis())

}