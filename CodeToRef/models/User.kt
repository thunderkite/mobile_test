package backend.models

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// Сериализатор для типа Instant
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
    
    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }
}

@Serializable
data class User(
    val userId: Int,
    val firstName: String,
    val lastName: String,
    val middleName: String? = null,
    val email: String,
    val college: String,
    val group: String,
    val password: String,
    val role: String = "student",
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant = Instant.now()
)

object Users : Table() {
    val userId = integer("user_id").autoIncrement()
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val middleName = varchar("middle_name", 100).nullable()
    val email = varchar("email", 255)
    val college = varchar("college", 255)
    val group = varchar("group", 255)
    val password = varchar("password", 255)
    val role = varchar("role", 50).default("student")
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(userId)
} 