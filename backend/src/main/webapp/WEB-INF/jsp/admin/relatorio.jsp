<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="base" value="${pageContext.request.contextPath}"/>
<t:page title="Relatório de consultas" active="relatorio">
  <form method="get" action="${base}/admin/relatorio" class="toolbar" aria-label="Período do relatório">
    <div class="field">
      <label for="de">De</label>
      <input type="date" id="de" name="de" value="${from}" required>
    </div>
    <div class="field">
      <label for="ate">Até</label>
      <input type="date" id="ate" name="ate" value="${to}" required>
    </div>
    <button type="submit" class="btn btn-primary">Gerar relatório</button>
  </form>

  <h2>Resumo do período</h2>
  <ul class="stats" aria-label="Resumo do período">
    <li class="stat"><strong>${report.summary.total}</strong> Consultas</li>
    <li class="stat"><strong>${report.summary.completed}</strong> Realizadas</li>
    <li class="stat"><strong>${report.summary.noShow}</strong> Faltas</li>
    <li class="stat muted"><strong>${report.summary.cancelled}</strong> Canceladas</li>
    <li class="stat">
      <strong>
        <c:choose>
          <c:when test="${report.hasFinishedVisits()}"><fmt:formatNumber value="${report.attendanceRate()}" maxFractionDigits="1"/>%</c:when>
          <c:otherwise>–</c:otherwise>
        </c:choose>
      </strong>
      Comparecimento
    </li>
  </ul>
  <p class="empty" style="text-align:left;padding:0 0 16px">
    Comparecimento = realizadas ÷ (realizadas + faltas). Consultas agendadas, confirmadas e canceladas não entram no cálculo.
  </p>

  <h2>Por médico</h2>
  <div class="table-wrap">
    <table>
      <caption>Consultas de <c:out value="${from}"/> a <c:out value="${to}"/></caption>
      <thead>
        <tr>
          <th scope="col">Médico</th>
          <th scope="col">Especialidade</th>
          <th scope="col" class="num">Total</th>
          <th scope="col" class="num">Realizadas</th>
          <th scope="col" class="num">Faltas</th>
          <th scope="col" class="num">Canceladas</th>
          <th scope="col" class="num">Comparecimento</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="row" items="${report.doctors}">
          <tr>
            <td><c:out value="${row.doctorName}"/></td>
            <td><c:out value="${row.specialty}"/></td>
            <td class="num">${row.total}</td>
            <td class="num">${row.completed}</td>
            <td class="num">${row.noShow}</td>
            <td class="num">${row.cancelled}</td>
            <td class="num">
              <c:choose>
                <c:when test="${row.hasFinishedVisits()}"><fmt:formatNumber value="${row.attendanceRate()}" maxFractionDigits="1"/>%</c:when>
                <c:otherwise>–</c:otherwise>
              </c:choose>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty report.doctors}">
          <tr><td colspan="7" class="empty">Nenhuma consulta no período.</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>
</t:page>
