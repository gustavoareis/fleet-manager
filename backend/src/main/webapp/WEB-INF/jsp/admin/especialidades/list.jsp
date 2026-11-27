<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="base" value="${pageContext.request.contextPath}"/>
<t:page title="Especialidades" active="especialidades">
  <div class="toolbar">
    <form method="get" action="${base}/admin/especialidades" role="search" class="toolbar grow">
      <div class="field">
        <label for="busca">Buscar por nome</label>
        <input type="text" id="busca" name="busca" value="<c:out value='${busca}'/>">
      </div>
      <button type="submit" class="btn">Buscar</button>
    </form>
    <a class="btn btn-primary" href="${base}/admin/especialidades/nova">Nova especialidade</a>
  </div>

  <div class="table-wrap">
    <table>
      <caption>${page.totalElements} especialidade(s)</caption>
      <thead>
        <tr><th scope="col">Nome</th><th scope="col">Ações</th></tr>
      </thead>
      <tbody>
        <c:forEach var="specialty" items="${page.content}">
          <tr>
            <td><c:out value="${specialty.name}"/></td>
            <td>
              <div class="actions">
                <a class="btn" href="${base}/admin/especialidades/${specialty.id}/editar"
                   aria-label="Editar <c:out value='${specialty.name}'/>">Editar</a>
                <form method="post" action="${base}/admin/especialidades/${specialty.id}/excluir"
                      onsubmit="return confirm('Excluir esta especialidade?');">
                  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                  <button type="submit" class="btn btn-danger"
                          aria-label="Excluir <c:out value='${specialty.name}'/>">Excluir</button>
                </form>
              </div>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty page.content}">
          <tr><td colspan="2" class="empty">Nenhuma especialidade encontrada.</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>

  <t:pagination page="${page}" path="/admin/especialidades" name1="busca" value1="${busca}"/>
</t:page>
