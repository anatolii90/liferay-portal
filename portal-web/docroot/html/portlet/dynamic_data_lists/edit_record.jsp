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

<%@ include file="/html/portlet/dynamic_data_lists/init.jsp" %>

<%
String redirect = ParamUtil.getString(request, "redirect");

DDLRecord record = (DDLRecord)request.getAttribute(WebKeys.DYNAMIC_DATA_LISTS_RECORD);

long recordId = BeanParamUtil.getLong(record, request, "recordId");

long recordSetId = BeanParamUtil.getLong(record, request, "recordSetId");

long formDDMTemplateId = ParamUtil.getLong(request, "formDDMTemplateId");

DDLRecordVersion recordVersion = null;

if (record != null) {
	recordVersion = record.getLatestRecordVersion();
}

DDLRecordSet recordSet = DDLRecordSetServiceUtil.getRecordSet(recordSetId);

DDMStructure ddmStructure = recordSet.getDDMStructure();

Fields fields = null;

if (recordVersion != null) {
	fields = StorageEngineUtil.getFields(recordVersion.getDDMStorageId());
}

Locale[] availableLocales = new Locale[0];

if (fields != null) {
	Set<Locale> availableLocalesSet = fields.getAvailableLocales();

	availableLocales = availableLocalesSet.toArray(new Locale[availableLocalesSet.size()]);
}

String defaultLanguageId = ParamUtil.getString(request, "defaultLanguageId");

if (Validator.isNull(defaultLanguageId)) {
	defaultLanguageId = themeDisplay.getLanguageId();

	if (fields != null) {
		defaultLanguageId = LocaleUtil.toLanguageId(fields.getDefaultLocale());
	}
}

String languageId = ParamUtil.getString(request, "languageId", defaultLanguageId);

boolean translating = false;

if (!defaultLanguageId.equals(languageId)) {
	translating = true;
}

if (translating) {
	redirect = currentURL;
}
%>

<liferay-ui:header
	backURL="<%= redirect %>"
	showBackURL="<%= !translating %>"
	title='<%= (record != null) ? LanguageUtil.format(pageContext, "edit-x", ddmStructure.getName(locale), false) : LanguageUtil.format(pageContext, "new-x", ddmStructure.getName(locale), false) %>'
/>

<portlet:actionURL var="editRecordURL">
	<portlet:param name="struts_action" value="/dynamic_data_lists/edit_record" />
</portlet:actionURL>

<aui:form action="<%= editRecordURL %>" cssClass="lfr-dynamic-form" enctype="multipart/form-data" method="post" name="fm" onSubmit='<%= "event.preventDefault(); submitForm(event.target);" %>'>
	<aui:input name="<%= Constants.CMD %>" type="hidden" />
	<aui:input name="redirect" type="hidden" value="<%= redirect %>" />
	<aui:input name="recordSetId" type="hidden" value="<%= recordSetId %>" />
	<aui:input name="recordId" type="hidden" value="<%= recordId %>" />
	<aui:input name="defaultLanguageId" type="hidden" value="<%= defaultLanguageId %>" />
	<aui:input name="languageId" type="hidden" value="<%= languageId %>" />
	<aui:input name="workflowAction" type="hidden" value="<%= WorkflowConstants.ACTION_PUBLISH %>" />

	<liferay-ui:error exception="<%= FileSizeException.class %>">

		<%
		long fileMaxSize = PrefsPropsUtil.getLong(PropsKeys.DL_FILE_MAX_SIZE);

		if (fileMaxSize == 0) {
			fileMaxSize = PrefsPropsUtil.getLong(PropsKeys.UPLOAD_SERVLET_REQUEST_IMPL_MAX_SIZE);
		}
		%>

		<liferay-ui:message arguments="<%= TextFormatter.formatStorageSize(fileMaxSize, locale) %>" key="please-enter-a-file-with-a-valid-file-size-no-larger-than-x" translateArguments="<%= false %>" />
	</liferay-ui:error>

	<liferay-ui:error exception="<%= StorageFieldRequiredException.class %>" message="please-fill-out-all-required-fields" />

	<c:if test="<%= !translating %>">
		<c:if test="<%= recordVersion != null %>">
			<aui:model-context bean="<%= recordVersion %>" model="<%= DDLRecordVersion.class %>" />

			<aui:workflow-status model="<%= DDLRecord.class %>" status="<%= recordVersion.getStatus() %>" version="<%= recordVersion.getVersion() %>" />
		</c:if>

		<liferay-util:include page="/html/portlet/dynamic_data_lists/record_toolbar.jsp" />
	</c:if>

	<aui:fieldset>
		<c:if test="<%= !translating %>">
			<aui:translation-manager
				availableLocales="<%= availableLocales %>"
				defaultLanguageId="<%= defaultLanguageId %>"
				id="translationManager"
				readOnly="<%= recordId <= 0 %>"
			/>

			<aui:script use="liferay-translation-manager">
				var translationManager = Liferay.component('<portlet:namespace />translationManager');

				translationManager.on(
					'defaultLocaleChange',
					function(event) {
						if (!confirm('<%= UnicodeLanguageUtil.get(pageContext, "changing-the-default-language-will-delete-all-unsaved-content") %>')) {
							event.preventDefault();
						}
					}
				);

				translationManager.after(
					{
						defaultLocaleChange: function(event) {
							<liferay-portlet:renderURL copyCurrentRenderParameters="<%= true %>" var="updateDefaultLanguageURL">
								<portlet:param name="struts_action" value="/dynamic_data_lists/edit_record" />
							</liferay-portlet:renderURL>

							var url = '<%= updateDefaultLanguageURL %>' + '&<portlet:namespace />defaultLanguageId=' + event.newVal;

							window.location.href = url;
						},
						deleteAvailableLocale: function(event) {
							var locale = event.locale;

							Liferay.Service(
								'/ddlrecord/delete-record-locale',
								{
									locale: locale,
									recordId: <%= recordId %>,
									serviceContext: JSON.stringify(
										{
											scopeGroupId: themeDisplay.getScopeGroupId(),
											userId: themeDisplay.getUserId()
										}
									)
								}
							);
						},
						editingLocaleChange: function(event) {
							var editingLocale = event.newVal;

							var defaultLocale = translationManager.get('defaultLocale');

							if (editingLocale !== defaultLocale) {
								Liferay.Util.openWindow(
									{
										cache: false,
										id: event.newVal,
										title: '<%= UnicodeLanguageUtil.get(pageContext, "record-translation") %>',

										<liferay-portlet:renderURL copyCurrentRenderParameters="<%= true %>" var="translateRecordURL" windowState="<%= LiferayWindowState.POP_UP.toString() %>">
											<portlet:param name="struts_action" value="/dynamic_data_lists/edit_record" />
										</liferay-portlet:renderURL>

										uri: '<%= translateRecordURL %>' + '&<portlet:namespace />languageId=' + editingLocale
									},
									function(translationWindow) {
										translationWindow.once(
											'visibleChange',
											function(event) {
												if (!event.newVal) {
													translationManager.set('editingLocale', defaultLocale);
												}
											}
										);
									}
								);
							}
						}
					}
				);
			</aui:script>
		</c:if>

		<%
		long classNameId = PortalUtil.getClassNameId(DDMStructure.class);

		long classPK = recordSet.getDDMStructureId();

		if (formDDMTemplateId > 0) {
			classNameId = PortalUtil.getClassNameId(DDMTemplate.class);

			classPK = formDDMTemplateId;
		}
		%>

		<liferay-ddm:html
			classNameId="<%= classNameId %>"
			classPK="<%= classPK %>"
			fields="<%= fields %>"
			repeatable="<%= translating ? false : true %>"
			requestedLocale="<%= LocaleUtil.fromLanguageId(languageId) %>"
		/>

		<%
		boolean pending = false;

		if (recordVersion != null) {
			pending = recordVersion.isPending();
		}
		%>

		<c:if test="<%= pending %>">
			<div class="alert alert-info">
				<liferay-ui:message key="there-is-a-publication-workflow-in-process" />
			</div>
		</c:if>

		<aui:button-row>
			<c:choose>
				<c:when test="<%= translating %>">
					<aui:button name="saveTranslationButton" onClick='<%= renderResponse.getNamespace() + "setWorkflowAction(false);" %>' type="submit" value="add-translation" />

					<aui:button href="<%= redirect %>" name="cancelButton" type="cancel" />
				</c:when>
				<c:otherwise>

					<%
					String saveButtonLabel = "save";

					if ((recordVersion == null) || recordVersion.isDraft() || recordVersion.isApproved()) {
						saveButtonLabel = "save-as-draft";
					}

					String publishButtonLabel = "publish";

					if (WorkflowDefinitionLinkLocalServiceUtil.hasWorkflowDefinitionLink(themeDisplay.getCompanyId(), scopeGroupId, DDLRecordSet.class.getName(), recordSetId)) {
						publishButtonLabel = "submit-for-publication";
					}
					%>

					<aui:button name="saveButton" onClick='<%= renderResponse.getNamespace() + "setWorkflowAction(true);" %>' primary="<%= false %>" type="submit" value="<%= saveButtonLabel %>" />

					<aui:button disabled="<%= pending %>" name="publishButton" onClick='<%= renderResponse.getNamespace() + "setWorkflowAction(false);" %>' type="submit" value="<%= publishButtonLabel %>" />

					<aui:button href="<%= redirect %>" name="cancelButton" type="cancel" />
				</c:otherwise>
			</c:choose>
		</aui:button-row>
	</aui:fieldset>
</aui:form>

<aui:script>
	function <portlet:namespace />setWorkflowAction(draft) {
		document.<portlet:namespace />fm.<portlet:namespace /><%= Constants.CMD %>.value = '<%= (record == null) ? Constants.ADD : Constants.UPDATE %>';

		if (draft) {
			document.<portlet:namespace />fm.<portlet:namespace />workflowAction.value = <%= WorkflowConstants.ACTION_SAVE_DRAFT %>;
		}
		else {
			document.<portlet:namespace />fm.<portlet:namespace />workflowAction.value = <%= WorkflowConstants.ACTION_PUBLISH %>;
		}
	}
</aui:script>

<%
PortletURL portletURL = renderResponse.createRenderURL();

portletURL.setParameter("struts_action", "/dynamic_data_lists/view_record_set");
portletURL.setParameter("recordSetId", String.valueOf(recordSetId));

PortalUtil.addPortletBreadcrumbEntry(request, recordSet.getName(locale), portletURL.toString());

if (record != null) {
	PortalUtil.addPortletBreadcrumbEntry(request, LanguageUtil.format(pageContext, "edit-x", ddmStructure.getName(locale), false), currentURL);
}
else {
	PortalUtil.addPortletBreadcrumbEntry(request, LanguageUtil.format(pageContext, "add-x", ddmStructure.getName(locale), false), currentURL);
}
%>