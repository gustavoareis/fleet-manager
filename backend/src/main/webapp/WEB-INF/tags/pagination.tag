<%@ tag language="java" pageEncoding="UTF-8" body-content="empty" description="Previous/next links for a PageResponse" %>
<%@ attribute name="page" required="true" type="com.medagenda.common.web.PageResponse" %>
<%@ attribute name="path" required="true" description="Page URL without query string" %>
<%@ attribute name="name1" required="false" %>
<%@ attribute name="value1" required="false" %>
<%@ attribute name="name2" required="false" %>
<%@ attribute name="value2" required="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${page.totalPages > 1}">
  <nav class="pagination" aria-label="Paginação">
    <c:if test="${page.page > 0}">
      <c:url var="prev" value="${path}">
        <c:param name="pagina" value="${page.page - 1}"/>
        <c:if test="${not empty value1}"><c:param name="${name1}" value="${value1}"/></c:if>
        <c:if test="${not empty value2}"><c:param name="${name2}" value="${value2}"/></c:if>
      </c:url>
      <a class="btn" href="${prev}" rel="prev">Anterior</a>
    </c:if>

    <span class="pagination-info">Página ${page.page + 1} de ${page.totalPages} · ${page.totalElements} registro(s)</span>

    <c:if test="${page.page + 1 < page.totalPages}">
      <c:url var="next" value="${path}">
        <c:param name="pagina" value="${page.page + 1}"/>
        <c:if test="${not empty value1}"><c:param name="${name1}" value="${value1}"/></c:if>
        <c:if test="${not empty value2}"><c:param name="${name2}" value="${value2}"/></c:if>
      </c:url>
      <a class="btn" href="${next}" rel="next">Próxima</a>
    </c:if>
  </nav>
</c:if>
