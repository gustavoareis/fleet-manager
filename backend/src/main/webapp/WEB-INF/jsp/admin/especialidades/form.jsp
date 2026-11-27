<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="base" value="${pageContext.request.contextPath}"/>
<t:page title="Especialidade" active="especialidades">
  <div class="card">
    <%-- form:form also writes the CSRF hidden field automatically (Spring Security integration) --%>
    <form:form method="post" modelAttribute="form" action="${base}${action}">
      <div class="field">
        <label for="name">Nome</label>
        <form:input path="name" cssErrorClass="error" maxlength="80" required="required"
                    aria-describedby="name-error"/>
        <form:errors path="name" id="name-error" element="p" cssClass="field-error" role="alert"/>
      </div>
      <div class="form-actions">
        <button type="submit" class="btn btn-primary">Salvar</button>
        <a class="btn" href="${base}/admin/especialidades">Cancelar</a>
      </div>
    </form:form>
  </div>
</t:page>
