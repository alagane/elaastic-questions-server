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

package org.elaastic.questions.lti

import org.elaastic.questions.directory.RoleService
import org.elaastic.questions.directory.User
import org.elaastic.questions.directory.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.text.Normalizer
import java.util.*
import java.util.logging.Logger
import java.util.regex.Pattern
import javax.persistence.EntityManager


@Service
class LmsUserAccountCreationService(
        @Autowired val userService: UserService,
        @Autowired val roleService: RoleService,
        @Autowired val entityManager: EntityManager
) {

    internal val logger = Logger.getLogger(LmsUserAccountCreationService::class.java.name)



    /**
     * Generate username from firstname and lastname
     * @param sql the sql connection to check existing username
     * @param firstName the firstname
     * @param lastName the lastname
     * @return the username
     */
    fun generateUsername(firstName: String, lastName: String): String {
        val indexLastname = MAX_INDEX_LASTNAME.coerceAtMost(lastName.length)
        val indexFirstName = MAX_INDEX_FIRSTNAME.coerceAtMost(firstName.length)
        var username = replaceAccent(firstName.replace("\\s".toRegex(), "").toLowerCase().substring(0, indexFirstName)) +
                replaceAccent(lastName.replace("\\s".toRegex(), "").toLowerCase().substring(0, indexLastname))
        val existingUsername = findMostRecentUsernameStartingWithUsername(username)
        if (existingUsername != null) {
            "[0-9]+".toRegex().let {
                it.findAll(existingUsername)
            }.let {
                username += if (it.count() == 0) {
                    2
                } else {
                    Integer.parseInt(it.iterator().next().value) + 1
                }
            }
        }
        return username
    }

    private val MAX_INDEX_LASTNAME = 4
    private val MAX_INDEX_FIRSTNAME = 3

    /**
     * Replace accents in a string
     * @param str the string to modify
     * @return
     */
    fun replaceAccent(str: String): String {
        val nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    /**
     * Get the most recent user who begin with username param
     * @param sql the sql object
     * @param username the username
     * @return a username if found else null
     */
    fun findMostRecentUsernameStartingWithUsername(username: String): String? {
        val userNameLike = "^$username[0-9]*$"
        val queryString = "SELECT username FROM user WHERE username RLIKE '$userNameLike' ORDER BY username DESC"
        logger.finest("Generated query string: $queryString")
        val query = entityManager.createNativeQuery(queryString)
        val result = query.resultList
        return when (result.isEmpty()) {
            true -> null
            false -> result.first().toString()
        }
    }

    /**
     * Create User from lti data
     * @param ltiFirstName first name
     * @param ltiLastName last name
     * @param ltiEmail email
     * @param ltiRole the role to be assigned to the user
     * @return the created user
     */
    fun createUserFromLtiData(ltiUser: LtiUser): User {
        User(
                firstName = ltiUser.firstName,
                lastName = ltiUser.lastName,
                username = generateUsername(ltiUser.firstName, ltiUser.lastName),
                plainTextPassword = userService.generatePassword(),
                email = ltiUser.email
        ).let {
            it.addRole(ltiUser.role)
        }.let {
            userService.addUser(
                    it,
                    "fr",
                    checkEmailAccount = false,
                    enable = true,
                    addUserConsent = true)
            return it
        }
    }

}
