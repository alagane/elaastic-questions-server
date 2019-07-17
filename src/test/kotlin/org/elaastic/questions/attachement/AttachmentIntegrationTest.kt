package org.elaastic.questions.attachement

import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import javax.persistence.EntityManager
import javax.transaction.Transactional
import javax.validation.ConstraintViolationException

@SpringBootTest
@Transactional
internal class AttachmentIntegrationTest(@Autowired val attachmentRepository: AttachmentRepository,
                                         @Autowired val em: EntityManager) {

    @Test fun `test save of a valid attachment`() {
        // given a valid attachment
        val attachment = Attachment(path = "/to/path", name = "MyAttach")
        attachment.originalName = "originalName"
        attachment.mimeType = MimeType()
        attachment.dimension = Dimension(width = 100, height = 100)
        attachment.size = 1024
        // when saving the attachment
        attachmentRepository.save(attachment)
        // then id and version are initialized
        assertThat("id should not be null", attachment.id, notNullValue())
        assertThat("version should be initialised", attachment.version, equalTo(0L))
    }

    @Test fun `test save of a non valid attachment`() {
        // given a valid attachment
        val attachment = Attachment(path = "/to/path", name = "MyAttach")
        attachment.originalName = ""
        // expect an exception is thrown when saving the attachment
        assertThrows<ConstraintViolationException> { attachmentRepository.save(attachment) }
    }

    @Test fun `test fetch of a save attachment`() {
        // given a valid saved attachment
        val attachment = Attachment(path = "/to/path", name = "MyAttach")
        attachment.mimeType = MimeType(MimeType.MimeTypesOfDisplayableImage.png.label)
        attachment.dimension = Dimension(width = 100, height = 150)
        attachmentRepository.saveAndFlush(attachment)
        // when refreshing the saved attachment
        em.refresh(attachment)
        // then it has the expected value properties
        assertThat("mime type is not as expected", attachment.mimeType?.label, equalTo(MimeType.MimeTypesOfDisplayableImage.png.label))
        assertThat("dimension width is not as expected", attachment.dimension?.width, equalTo(100))
        assertThat("dimension height is not as expected", attachment.dimension?.height, equalTo(150))

    }

}