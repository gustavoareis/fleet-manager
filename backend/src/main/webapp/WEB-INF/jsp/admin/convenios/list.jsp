<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="base" value="${pageContext.request.contextPath}"/>
<t:page title="Convênios" active="convenios">
  <div class="toolbar">
    <form method="get" action="${base}/admin/convenios" role="search" class="toolbar grow">
      <div class="field">
        <label for="busca">Buscar por nome</label>
        <input type="text" id="busca" name="busca" value="<c:out value='${busca}'/>">
      </div>
      <button type="submit" class="btn">Buscar</button>
    </form>
    <a class="btn btn-primary" href="${base}/admin/convenios/novo">Novo convênio</a>
  </div>

  <div class="table-wrap">
    <table>
      <caption>${page.totalElements} convênio(s)</caption>
      <thead>
        <tr><th scope="col">Nome</th><th scope="col">Situação</th><th scope="col">Ações</th></tr>
      </thead>
      <tbody>
        <c:forEach var="insurance" items="${page.content}">
          <tr>
            <td><c:out value="${insurance.name}"/></td>
            <td>
              <c:choose>
                <c:when test="${insurance.active}"><span class="badge badge-active">Ativo</span></c:when>
                <c:otherwise><span class="badge badge-inactive">Inativo</span></c:otherwise>
              </c:choose>
            </td>
            <td>
              <div class="actions">
                <a class="btn" href="${base}/admin/convenios/${insurance.id}/editar"
                   aria-label="Editar <c:out value='${insurance.name}'/>">Editar</a>
                <form method="post" action="${base}/admin/convenios/${insurance.id}/excluir"
                      onsubmit="return confirm('Excluir este convênio?');">
                  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                  <button type="submit" class="btn btn-danger"
                          aria-label="Excluir <c:out value='${insurance.name}'/>">Excluir</button>
                </form>
              </div>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty page.content}">
          <tr><td colspan="3" class="empty">Nenhum convênio encontrado.</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>

  <t:pagination page="${page}" path="/admin/convenios" name1="busca" value1="${busca}"/>
</t:page>
