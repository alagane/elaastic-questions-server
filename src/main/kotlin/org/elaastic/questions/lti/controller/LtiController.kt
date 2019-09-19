package org.elaastic.questions.lti.controller

import org.elaastic.questions.directory.RoleService
import org.elaastic.questions.lti.LmsService
import org.elaastic.questions.lti.LmsUser
import org.elaastic.questions.lti.oauth.OauthService
import org.elaastic.questions.terms.TermsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.*
import java.util.logging.Logger
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession


@Controller
class LtiController(
        @Autowired val lmsService: LmsService,
        @Autowired val oauthService: OauthService,
        @Autowired val termsService: TermsService,
        @Autowired val roleService: RoleService
) {

    internal var logger = Logger.getLogger(LtiController::class.java.name)

    @PostMapping("/launch")
    fun launch(ltiLaunchData: LtiLaunchData,
               request: HttpServletRequest,
               response: HttpServletResponse,
               model: Model,
               locale: Locale): String {
        val session = startNewSession(request)
        return try {
            oauthService.validateOauthRequest(request)
            ltiLaunchData.roleService = roleService
            val lmsUser = lmsService.findLmsUser(
                    ltiLmsKey = ltiLaunchData.oauth_consumer_key,
                    ltiUserId = ltiLaunchData.user_id)
            if (lmsUser != null) {
                authenticateLmsUser(session, lmsUser)
                redirectToAssignment(ltiLaunchData, lmsUser)
            } else {
                setLtiLaunchDataInSession(ltiLaunchData, session)
                model.addAttribute("termsContent", termsService.getTermsContentByLanguage(locale.language))
                model.addAttribute("firstName", ltiLaunchData.lis_person_name_given)
                model.addAttribute("lastName", ltiLaunchData.lis_person_name_family)
                "/terms/lti_terms_consent_form"
            }
        } catch (e: Exception) {
            e.stackTrace.iterator().forEach {
                logger.severe(it.toString())
            }
            "redirect:${ltiLaunchData.getRedirectUrlWithErrorMessage(e.message!!)}"
        }
    }

    @GetMapping("/launch/consent")
    fun collectConsent(request: HttpServletRequest,
                       @RequestParam("withConsent") withConsent: Boolean = false): String {
        val ltiLaunchData = getLtiLaunchDataFromSession(request.session)
        return try {
            if (withConsent) {
                val lmsUser = lmsService.getLmsUser(ltiLaunchData.toLtiUser())
                authenticateLmsUser(request.session, lmsUser)
                redirectToAssignment(ltiLaunchData, lmsUser)
            } else {
                logger.severe("Consent not given")
                "redirect:${ltiLaunchData.getRedirectUrlWithErrorMessage("no_consent_given_by_user")}"
            }
        } catch (e: Exception) {
            e.stackTrace.iterator().forEach {
                logger.severe(it.toString())
            }
            "redirect:${ltiLaunchData.getRedirectUrlWithErrorMessage(e.message!!)}"
        }

    }

    private fun authenticateLmsUser(session: HttpSession, lmsUser: LmsUser) {
        UsernamePasswordAuthenticationToken(lmsUser.user, null, lmsUser.user.authorities).let {
            val secContext = SecurityContextHolder.getContext()
            secContext.authentication = it
            session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, secContext)
        }
    }

    private fun redirectToAssignment(ltiLaunchData: LtiLaunchData, lmsUser: LmsUser): String {
        val lmsAssignment = lmsService.getLmsAssignment(
                lmsUser = lmsUser,
                ltiActivity = ltiLaunchData.toLtiActivity()
        )
        // TODO Bind with assignment when player implemented
        return "redirect:/home/"
    }

    private fun startNewSession(request: HttpServletRequest): HttpSession {
        request.characterEncoding = "UTF-8"
        request.session.invalidate()
        return request.getSession(true)
    }

    private fun setLtiLaunchDataInSession(ltiLaunchData: LtiLaunchData, session: HttpSession) {
        session.setAttribute("ltiLaunchData", ltiLaunchData)
    }

    private fun getLtiLaunchDataFromSession(session: HttpSession): LtiLaunchData {
        return session.getAttribute("ltiLaunchData") as LtiLaunchData
    }

}