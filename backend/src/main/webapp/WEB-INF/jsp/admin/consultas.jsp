<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="base" value="${pageContext.request.contextPath}"/>
<t:page title="Consultas do dia" active="consultas">
  <form method="get" action="${base}/admin/consultas" class="toolbar" aria-label="Filtros">
    <div class="field">
      <label for="data">Data</label>
      <input type="date" id="data" name="data" value="${date}" required>
    </div>
    <div class="field">
      <label for="medico">Médico</label>
      <select id="medico" name="medico">
        <option value="">Todos os médicos</option>
        <c:forEach var="doctor" items="${doctors}">
          <option value="${doctor.id}" <c:if test="${doctor.id == doctorId}">selected</c:if>>
            <c:out value="${doctor.name}"/> · <c:out value="${doctor.specialty.name}"/>
          </option>
        </c:forEach>
      </select>
    </div>
    <button type="submit" class="btn">Filtrar</button>
    <c:if test="${not isToday}">
      <a class="btn btn-ghost" href="${base}/admin/consultas">Voltar para hoje</a>
    </c:if>
  </form>

  <ul class="stats" aria-label="Resumo do dia">
    <li class="stat"><strong>${summary.total}</strong> Consultas</li>
    <li class="stat"><strong>${summary.scheduled}</strong> Aguardando chegada</li>
    <li class="stat"><strong>${summary.arrived()}</strong> Já chegaram</li>
    <li class="stat"><strong>${summary.noShow}</strong> Faltas</li>
  </ul>

  <div class="table-wrap">
    <table>
      <caption>${page.totalElements} consulta(s) em <c:out value="${date}"/></caption>
      <thead>
        <tr>
          <th scope="col">Hora</th>
          <th scope="col">Paciente</th>
          <th scope="col">Médico</th>
          <th scope="col">Convênio</th>
          <th scope="col">Situação</th>
          <th scope="col">Ação</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="row" items="${page.content}">
          <tr>
            <td><strong>${row.time}</strong></td>
            <td><c:out value="${row.patientName}"/></td>
            <td><c:out value="${row.doctorName}"/><br><small><c:out value="${row.specialty}"/></small></td>
            <td><c:out value="${row.insurance}"/></td>
            <td><span class="badge badge-${row.status}"><c:out value="${row.statusLabel}"/></span></td>
            <td>
              <c:if test="${row.canConfirmArrival}">
                <%-- A POST that changes data: it carries the CSRF token and the filters to come back to --%>
                <form method="post" action="${base}/admin/consultas/${row.id}/chegada">
                  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                  <input type="hidden" name="data" value="${date}">
                  <input type="hidden" name="pagina" value="${page.page}">
                  <c:if test="${not empty doctorId}"><input type="hidden" name="medico" value="${doctorId}"></c:if>
                  <button type="submit" class="btn btn-primary"
                          aria-label="Confirmar chegada de <c:out value='${row.patientName}'/>, consulta das ${row.time}">
                    Confirmar chegada
                  </button>
                </form>
              </c:if>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty page.content}">
          <tr><td colspan="6" class="empty">Nenhuma consulta nesta data.</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>

  <t:pagination page="${page}" path="/admin/consultas" name1="data" value1="${date}" name2="medico" value2="${doctorId}"/>
</t:page>
