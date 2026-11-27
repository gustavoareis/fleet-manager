<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:page title="Entrar" showNav="${false}">
  <div class="login-wrap">
    <div class="card">
      <c:if test="${param.erro != null}">
        <p class="alert alert-error" role="alert">E-mail ou senha inválidos.</p>
      </c:if>
      <c:if test="${param.saiu != null}">
        <p class="alert alert-success" role="status">Você saiu do sistema.</p>
      </c:if>

      <%-- Spring Security processes this POST. Its default field names are "username" and "password". --%>
      <form method="post" action="${pageContext.request.contextPath}/admin/login">
        <div class="field">
          <label for="username">E-mail</label>
          <input type="email" id="username" name="username" autocomplete="username" required autofocus>
        </div>
        <div class="field">
          <label for="password">Senha</label>
          <input type="password" id="password" name="password" autocomplete="current-password" required>
        </div>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <button type="submit" class="btn btn-primary">Entrar</button>
      </form>
    </div>
    <p class="empty">Acesso restrito à recepção e à administração.</p>
  </div>
</t:page>
