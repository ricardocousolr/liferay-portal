<%--
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@ include file="/html/portal/init.jsp" %>

<c:choose>
	<c:when test='<%= Objects.equals(SessionErrors.get(request, "statusCodeURI"), "urn:oasis:names:tc:SAML:2.0:status:AuthnFailed") %>'>
		<h3 class="portlet-msg-error">
			<liferay-ui:message key="authentication-failed" />
		</h3>
	</c:when>
	<c:when test='<%= Objects.equals(SessionErrors.get(request, "subjectException"), "User is a stranger and company " + themeDisplay.getCompanyId() + " does not allow strangers to create accounts") %>'>

		<%
		HttpSession httpSession = request.getSession();

		String samlSubjectScreenName = (String)httpSession.getAttribute("SAML_SUBJECT_NAME_ID");
		%>

		<aui:form action='<%= PortalUtil.getPortalURL(request) + "/c/portal/login" %>' method="post" name="fm">
			<aui:input name="p_auth" type="hidden" value="<%= AuthTokenUtil.getToken(request) %>" />
			<aui:input name="saveLastPath" type="hidden" value="<%= false %>" />
			<aui:input name="forceAuthn" type="hidden" value="true" />
			<aui:input name="idpEntityId" type="hidden" value='<%= (String)httpSession.getAttribute("SAML_SSO_ERROR_ENTITY_ID") %>' />

			<h3 class="portlet-msg-error">
				<liferay-ui:message key="only-known-users-are-allowed-to-sign-in-using-saml" />

				<a onClick="this.closest('form').submit(); return false;"><liferay-ui:message arguments='<%= "<strong>" + HtmlUtil.escapeAttribute(samlSubjectScreenName) + "</strong>" %>' key="not-x" /></a>
			</h3>
		</aui:form>
	</c:when>
	<c:otherwise>
		<h3 class="portlet-msg-error">
			<liferay-ui:message key="unable-to-process-saml-request" />
		</h3>
	</c:otherwise>
</c:choose>