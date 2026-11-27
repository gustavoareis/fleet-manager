<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:page title="${errorTitle}">
  <p class="alert alert-error" role="alert"><c:out value="${errorMessage}"/></p>
  <p><a class="btn" href="${pageContext.request.contextPath}/admin">Voltar ao painel</a></p>
</t:page>
