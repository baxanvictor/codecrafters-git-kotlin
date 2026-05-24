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
            ByteArrayOutputStreamInput.ByteArrayInput(objectType.type.toByteArray()),
            ByteArrayOutputStreamInput.IntInput(' '.code),
            ByteArrayOutputStreamInput.ByteArrayInput(contentLength.toString().toByteArray()),
            ByteArrayOutputStreamInput.IntInput(Constants.NULL_BYTE.code),
            ByteArrayOutputStreamInput.ByteArrayInput(content)
        )
    )
}