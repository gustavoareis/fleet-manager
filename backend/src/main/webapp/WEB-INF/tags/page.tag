<%@ tag language="java" pageEncoding="UTF-8" body-content="scriptless" description="Common frame of every admin page" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="active" required="false" description="Nav item to highlight: dashboard, consultas, especialidades, convenios, relatorio" %>
<%@ attribute name="showNav" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="base" value="${pageContext.request.contextPath}"/>
<c:set var="withNav" value="${showNav == null ? true : showNav}"/>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title><c:out value="${title}"/> · MedAgenda Administração</title>
  <link rel="stylesheet" href="${base}/admin/assets/admin.css">
</head>
<body>
  <a class="skip-link" href="#conteudo">Pular para o conteúdo</a>

  <header class="topbar">
    <a class="brand" href="${base}/admin">MedAgenda <span>Administração</span></a>

    <c:if test="${withNav}">
      <nav aria-label="Navegação principal">
        <ul>
          <li><a href="${base}/admin" <c:if test="${active == 'dashboard'}">aria-current="page"</c:if>>Painel</a></li>
          <li><a href="${base}/admin/consultas" <c:if test="${active == 'consultas'}">aria-current="page"</c:if>>Consultas do dia</a></li>
          <li><a href="${base}/admin/especialidades" <c:if test="${active == 'especialidades'}">aria-current="page"</c:if>>Especialidades</a></li>
          <li><a href="${base}/admin/convenios" <c:if test="${active == 'convenios'}">aria-current="page"</c:if>>Convênios</a></li>
          <li><a href="${base}/admin/relatorio" <c:if test="${active == 'relatorio'}">aria-current="page"</c:if>>Relatório</a></li>
        </ul>
      </nav>
    </c:if>

    <c:if test="${not empty adminUser}">
      <%-- Logout is a POST with the CSRF token: a plain link could be triggered by another site --%>
      <form class="logout" method="post" action="${base}/admin/logout">
        <span class="who"><c:out value="${adminUser}"/></span>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <button type="submit" class="btn btn-ghost">Sair</button>
      </form>
    </c:if>
  </header>

  <%-- tabindex=-1 lets the skip link move focus here --%>
  <main id="conteudo" tabindex="-1">
    <%-- Flash messages survive the redirect that follows a successful POST --%>
    <c:if test="${not empty success}">
      <p class="alert alert-success" role="status"><c:out value="${success}"/></p>
    </c:if>
    <c:if test="${not empty error}">
      <p class="alert alert-error" role="alert"><c:out value="${error}"/></p>
    </c:if>

    <h1><c:out value="${title}"/></h1>
    <jsp:doBody/>
  </main>

  <footer class="footer">MedAgenda · módulo administrativo (legado em JSP)</footer>
</body>
</html>
