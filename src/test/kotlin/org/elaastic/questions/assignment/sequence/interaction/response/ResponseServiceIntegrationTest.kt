/*
 * Elaastic - formative assessment system
 * Copyright (C) 2019. University Toulouse 1 Capitole, University Toulouse 3 Paul Sabatier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.elaastic.questions.assignment.sequence.interaction.response

import org.elaastic.questions.assignment.AssignmentService
import org.elaastic.questions.assignment.ExecutionContext
import org.elaastic.questions.assignment.QuestionType
import org.elaastic.questions.assignment.Statement
import org.elaastic.questions.assignment.choice.ChoiceItem
import org.elaastic.questions.assignment.choice.ExclusiveChoiceSpecification
import org.elaastic.questions.assignment.choice.MultipleChoiceSpecification
import org.elaastic.questions.assignment.choice.legacy.LearnerChoice
import org.elaastic.questions.assignment.sequence.ConfidenceDegree
import org.elaastic.questions.assignment.sequence.SequenceRepository
import org.elaastic.questions.assignment.sequence.SequenceService
import org.elaastic.questions.test.TestingService
import org.elaastic.questions.test.directive.tGiven
import org.elaastic.questions.test.directive.tThen
import org.elaastic.questions.test.directive.tWhen
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import javax.transaction.Transactional

@SpringBootTest
@Transactional
internal class ResponseServiceIntegrationTest(
        @Autowired val testingService: TestingService,
        @Autowired val responseService: ResponseService,
        @Autowired val sequenceRepository: SequenceRepository,
        @Autowired val sequenceService: SequenceService,
        @Autowired val assignmentService: AssignmentService
) {

    @Test
    fun buildResponseBasedOnTeacherExpectedExplanationForASequenceOpenEndedBlended() {
        tGiven("given a sequence corresponding with an open ended question but with expected explanation") {
            assignmentService.addSequence(
                    assignment = testingService.getAnyAssignment(),
                    statement = Statement(
                            owner = testingService.getAnyAssignment().owner,
                            title = "q1",
                            content = "question 1",
                            expectedExplanation = "it is expected",
                            questionType = QuestionType.OpenEnded
                    )
            ).let {
                sequenceService.initializeInteractionsForSequence(
                        it,
                        true,
                        3,
                        ExecutionContext.Blended
                ).let { sequence ->
                    sequence.executionContext = ExecutionContext.Blended
                    sequenceRepository.save(sequence)
                }
            }
        }.tWhen("we build a response based on expected explanations") { sequence ->
            responseService.buildResponseBasedOnTeacherExpectedExplanationForASequence(
                    sequence = sequence,
                    teacher = sequence.owner
            )
        }.tThen { response ->
            assertThat(response!!.id, notNullValue())
            assertThat(response.learner, equalTo(testingService.getAnyAssignment().owner))
            assertThat(response.score, nullValue())
            assertThat(response.confidenceDegree, equalTo(ConfidenceDegree.CONFIDENT.ordinal))
            assertThat(response.explanation, equalTo(response.interaction.sequence.statement.expectedExplanation))
            assertThat(response.attempt, equalTo(2))
            assertTrue(response.isAFake)
        }
    }

    @Test
    fun buildResponseBasedOnTeacherExpectedExplanationForASequenceExclusiveChoiceBlended() {
        tGiven("given a sequence corresponding with an open ended question but with expected explanation") {
            assignmentService.addSequence(
                    assignment = testingService.getAnyAssignment(),
                    statement = Statement(
                            owner = testingService.getAnyAssignment().owner,
                            title = "q1",
                            content = "question 1",
                            expectedExplanation = "it is expected",
                            questionType = QuestionType.ExclusiveChoice,
                            choiceSpecification = ExclusiveChoiceSpecification(
                                    nbCandidateItem = 3,
                                    expectedChoice = ChoiceItem(2, 100f)
                            )
                    )
            ).let {
                sequenceService.initializeInteractionsForSequence(
                        it,
                        true,
                        3,
                        ExecutionContext.Blended
                ).let { sequence ->
                    sequence.executionContext = ExecutionContext.Blended
                    sequenceRepository.save(sequence)
                }
            }
        }.tWhen("we build a response based on expected explanations") { sequence ->
            responseService.buildResponseBasedOnTeacherExpectedExplanationForASequence(
                    sequence = sequence,
                    teacher = sequence.owner
            )
        }.tThen { response ->
            assertThat(response!!.id, notNullValue())
            assertThat(response.learner, equalTo(testingService.getAnyAssignment().owner))
            assertThat(response.score, equalTo(100f))
            assertThat(response.confidenceDegree, equalTo(ConfidenceDegree.CONFIDENT.ordinal))
            assertThat(response.explanation, equalTo(response.interaction.sequence.statement.expectedExplanation))
            assertThat(response.attempt, equalTo(2))
            assertTrue(response.isAFake)
            assertThat(response.learnerChoice, equalTo(LearnerChoice(listOf(2))))
        }
    }

    @Test
    fun buildResponseBasedOnTeacherExpectedExplanationForASequenceMultipleChoiceFaceToFace() {
        tGiven("given a sequence corresponding with an open ended question but with expected explanation") {
            assignmentService.addSequence(
                    assignment = testingService.getAnyAssignment(),
                    statement = Statement(
                            owner = testingService.getAnyAssignment().owner,
                            title = "q1",
                            content = "question 1",
                            expectedExplanation = "it is expected",
                            questionType = QuestionType.ExclusiveChoice,
                            choiceSpecification = MultipleChoiceSpecification(
                                    nbCandidateItem = 4,
                                    expectedChoiceList = listOf(
                                            ChoiceItem(4, 50f),
                                            ChoiceItem(2, 50f)
                                    )
                            )
                    )
            ).let {
                sequenceService.initializeInteractionsForSequence(
                        it,
                        true,
                        3,
                        ExecutionContext.FaceToFace
                ).let { sequence ->
                    sequence.executionContext = ExecutionContext.FaceToFace
                    sequenceRepository.save(sequence)
                }
            }
        }.tWhen("we build a response based on expected explanations") { sequence ->
            responseService.buildResponseBasedOnTeacherExpectedExplanationForASequence(
                    sequence = sequence,
                    teacher = sequence.owner
            )
        }.tThen { response ->
            assertThat(response!!.id, notNullValue())
            assertThat(response.learner, equalTo(testingService.getAnyAssignment().owner))
            assertThat(response.score, equalTo(100f))
            assertThat(response.confidenceDegree, equalTo(ConfidenceDegree.CONFIDENT.ordinal))
            assertThat(response.explanation, equalTo(response.interaction.sequence.statement.expectedExplanation))
            assertThat(response.attempt, equalTo(1))
            assertTrue(response.isAFake)
            assertThat(response.learnerChoice, equalTo(LearnerChoice(listOf(4,2))))
        }
    }
}
