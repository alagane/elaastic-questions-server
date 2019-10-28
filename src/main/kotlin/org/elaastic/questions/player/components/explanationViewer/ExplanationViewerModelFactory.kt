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
package org.elaastic.questions.player.components.explanationViewer

import org.elaastic.questions.assignment.choice.ChoiceSpecification
import org.elaastic.questions.assignment.sequence.interaction.response.Response

object ExplanationViewerModelFactory {

    fun buildOpen(responseList: List<Response>) =
            OpenExplanationViewerModel(
                    responseList.map { ExplanationData(it) },
                    true
            )

    fun buildChoice(teacher: Boolean,
                    responseList: List<Response>,
                    choiceSpecification: ChoiceSpecification): ExplanationViewerModel =
            ChoiceExplanationViewerModel(
                    // TODO I should simplify (merge ChoiceExplanationViewerModel & ChoiceExplanationStore)
                    explanationsByResponse = ChoiceExplanationStore(
                            choiceSpecification,
                            true,
                            responseList
                    ),
                    alreadySorted = true,
                    showOnlyCorrectResponse = !teacher
            )

}