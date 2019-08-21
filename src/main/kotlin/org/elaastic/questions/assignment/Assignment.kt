package org.elaastic.questions.assignment

import org.elaastic.questions.assignment.sequence.Sequence
import org.elaastic.questions.directory.User
import org.elaastic.questions.persistence.AbstractJpaPersistable
import org.hibernate.annotations.SortNatural
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import kotlin.collections.ArrayList


@Entity
@NamedEntityGraph(
        name = "Assignment.sequences",
        attributeNodes = [
            NamedAttributeNode(
                    value = "sequences",
                    subgraph = "Sequence.statement"
            )
        ],
        subgraphs = [
            NamedSubgraph(
                    name = "Sequence.statement",
                    attributeNodes = [NamedAttributeNode("statement")]
            )
        ]
)
@EntityListeners(AuditingEntityListener::class)
class Assignment(
        @field:NotBlank
        var title: String,

        @field:NotNull
        @field:ManyToOne(fetch = FetchType.LAZY)
        var owner: User,

        @field:NotNull
        @field:NotBlank
        var globalId: String = UUID.randomUUID().toString()
) : AbstractJpaPersistable<Long>() {

    @Version
    var version: Long? = null

    @Column(name = "date_created")
    @CreatedDate
    lateinit var dateCreated: Date

    @LastModifiedDate
    @Column(name = "last_updated")
    var lastUpdated: Date? = null

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "assignment",
            targetEntity = Sequence::class)
    @OrderBy("rank ASC")
    @SortNatural
    var sequences: MutableList<Sequence> = ArrayList()

    fun updateFrom(otherAssignment: Assignment) {
        require(id == otherAssignment.id)
        if (this.version != otherAssignment.version) {
            throw OptimisticLockException()
        }

        this.title = otherAssignment.title
    }

    fun addSequence(sequence: Sequence): Sequence {
        require(sequence.owner == owner) {
            "The owner of the assignment cannot be different from the owner of sequence"
        }

        // TODO set the rank
        // TODO Check that lastUpdated is impacted

        sequences.add(sequence)
        sequence.assignment = this
        sequence.owner = owner

        return sequence
    }
}