<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:page title="Acesso negado" showNav="${false}">
  <c:choose>
    <c:when test="${param.motivo == 'csrf'}">
      <p>Não foi possível validar o formulário. Isso costuma acontecer quando a sessão expira ou a página ficou aberta por muito tempo.</p>
      <p>Volte ao painel e tente novamente.</p>
      <p><a class="btn btn-primary" href="${pageContext.request.contextPath}/admin">Voltar ao painel</a></p>
    </c:when>
    <c:otherwise>
      <p>Seu perfil não tem permissão para usar o módulo administrativo. Ele é exclusivo da recepção e da administração.</p>
      <p>Pacientes e médicos usam o aplicativo principal do MedAgenda.</p>
    </c:otherwise>
  </c:choose>
  <form method="post" action="${pageContext.request.contextPath}/admin/logout">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
    <button type="submit" class="btn btn-primary">Sair e entrar com outra conta</button>
  </form>
</t:page>
