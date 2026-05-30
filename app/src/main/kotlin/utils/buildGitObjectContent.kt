package utils

import model.ByteArrayOutputStreamInput
import model.GitObjectType

fun buildGitObjectContent(
    objectType: GitObjectType,
    contentLength: Int,
    content: ByteArray
): ByteArray {
    return buildByteArrayFromInputs(
        inputs = listOf(
            ByteArrayOutputStreamInput.ByteArrayInput(objectType.type.encodeToByteArray()),
            ByteArrayOutputStreamInput.IntInput(' '.code),
            ByteArrayOutputStreamInput.ByteArrayInput(contentLength.toString().encodeToByteArray()),
            ByteArrayOutputStreamInput.IntInput(Constants.NULL_BYTE.code),
            ByteArrayOutputStreamInput.ByteArrayInput(content)
        )
    )
}