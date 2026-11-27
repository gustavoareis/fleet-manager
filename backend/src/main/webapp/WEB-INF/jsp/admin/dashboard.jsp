<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<c:set var="base" value="${pageContext.request.contextPath}"/>
<t:page title="Painel" active="dashboard">
  <h2>Hoje</h2>
  <ul class="stats" aria-label="Resumo das consultas de hoje">
    <li class="stat"><strong>${summary.total}</strong> Consultas</li>
    <li class="stat"><strong>${summary.scheduled}</strong> Aguardando chegada</li>
    <li class="stat"><strong>${summary.arrived()}</strong> Já chegaram</li>
    <li class="stat"><strong>${summary.noShow}</strong> Faltas</li>
    <li class="stat muted"><strong>${summary.cancelled}</strong> Canceladas</li>
  </ul>

  <h2>Atalhos</h2>
  <div class="shortcuts">
    <a href="${base}/admin/consultas"><strong>Consultas do dia</strong>Confirmar a chegada dos pacientes</a>
    <a href="${base}/admin/especialidades"><strong>Especialidades</strong>Cadastrar e editar</a>
    <a href="${base}/admin/convenios"><strong>Convênios</strong>Cadastrar, ativar e desativar</a>
    <a href="${base}/admin/relatorio"><strong>Relatório</strong>Consultas por médico e período</a>
  </div>
</t:page>
