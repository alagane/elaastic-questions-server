package org.elaastic.questions.assignment.choice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import javax.persistence.AttributeConverter

/**
 * @author John Tranier
 */
class ChoiceListSpecificationConverter :
        AttributeConverter<ChoiceListSpecification, String?> {

    val mapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    override fun convertToDatabaseColumn(attribute: ChoiceListSpecification?): String? {
        return mapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String?): ChoiceListSpecification? {
        return when(dbData) {
            null -> null
            else -> mapper.readValue(dbData, ChoiceListSpecification::class.java)
        }
    }
}