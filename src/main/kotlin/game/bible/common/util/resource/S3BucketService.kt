package game.bible.common.util.resource

import game.bible.config.model.integration.AwsConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.HeadObjectResponse

private val log = KotlinLogging.logger {}

/**
 * S3 Bucket Service Logic
 * @since 9th July 2025
 */
@Service
class S3BucketService(
    private val aws: AwsConfig,
    private val s3: S3Client) {

    /** Uploads audio bytes for a given passage */
    fun uploadAudio(passageKey: String, bytes: ByteArray): String {
        val bucket = aws.getS3()!!.getAudioBucket()!!
        val location = "${passageKey}.mp3"

        log.debug { "Uploading passage audio [$passageKey]" }
        return upload(bucket, location, "audio/mpeg", bytes)
    }

    /** Retrieves the audio bytes for a given passage */
    fun getAudio(passageKey: String): ByteArray? {
        log.debug { "Retrieving audio for [$passageKey]" }
        val bucket = aws.getS3()!!.getAudioBucket()!!

        return try {
            val item = retrieveItem(bucket, "${passageKey}.mp3")
            item.second

        } catch (e: Exception) {
            log.error { "Error retrieving audio content!" }
            null
        }
    }

    /** Retrieves keyed-item from given S3 bucket  */
    private fun retrieveItem(bucket: String, key: String): Pair<String, ByteArray> {
        log.debug { "Retrieving item [$bucket] [$key]"}
        try {
            val request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()

            s3.getObject(request).use { s3Object ->
                val contentType = s3Object.response().contentType()
                val content = s3Object.readAllBytes()

                return Pair(contentType, content)
            }

        } catch (e: S3Exception) {
            log.error(e) { "Error occurred!" }
            throw e
        }
    }

    /** Handles the upload to S3 */
    private fun upload(bucket: String, location: String, type: String, content: ByteArray): String {
        log.debug { "Uploading item [$bucket] [$location]" }

        try {
            log.debug { "Constructing request for object type [$type]" }
            val request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(location)
                .contentType(type)
                .contentLength(content.size.toLong())
                .build()

            val requestBody = RequestBody.fromBytes(content)

            log.debug { "Sending request to Amazon S3..." }
            s3.putObject(request, requestBody)

            log.debug { "Checking if item exists in S3..." }
            if (!doesObjectExist(bucket, location)) {
                throw Exception("Failed to save, item does not exist in S3!")
            }
            log.debug { "Item saved successfully!" }
            return location

        } catch (e: S3Exception) {
            log.error(e) { "Error parsing uploaded file!" }
            throw Exception( "Error uploading file to S3")
        }
    }

    private fun doesObjectExist(bucket: String, key: String): Boolean {
        return try {
            val request = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()
            val result: HeadObjectResponse = s3.headObject(request)
            result.contentLength() > 0

        } catch (e: NoSuchKeyException) {
            log.error(e) { "Key not found for [$key] in bucket [$bucket]" }
            false
        } catch (e: S3Exception) {
            log.error(e) { "Error checking if item exists in S3!" }
            false
        }
    }

}
