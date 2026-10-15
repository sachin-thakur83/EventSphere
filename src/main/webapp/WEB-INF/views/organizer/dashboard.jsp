<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Organizer Dashboard" scope="request"/>
<c:set var="active" value="dashboard" scope="request"/>
<c:set var="needCharts" value="true" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container dash">
<jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>
<section class="dash-main">
<div class="row-between"><h1>Organizer dashboard</h1><a class="btn btn-primary" href="${ctx}/organizer/event/new"><i class="fa-solid fa-plus"></i> Create event</a></div>
<div class="grid grid-4">
  <div class="card stat"><i class="fa-solid fa-calendar"></i><span>${stats.events}</span><small>Total events</small></div>
  <div class="card stat"><i class="fa-solid fa-calendar-day"></i><span>${stats.upcoming}</span><small>Upcoming (approved)</small></div>
  <div class="card stat"><i class="fa-solid fa-clipboard-check"></i><span>${stats.registrations}</span><small>Registrations</small></div>
  <div class="card stat"><i class="fa-solid fa-ticket"></i><span>${stats.tickets}</span><small>Tickets sold</small></div>
  <div class="card stat"><i class="fa-solid fa-indian-rupee-sign"></i><span><fmt:formatNumber value="${stats.revenue}" maxFractionDigits="0"/></span><small>Confirmed revenue (demo)</small></div>
</div>
<div class="grid grid-2 mt">
  <div class="card"><h3>Tickets sold per event</h3>
    <c:choose><c:when test="${empty chartLabels}"><p class="muted">Create an event to see sales.</p></c:when>
    <c:otherwise><canvas class="chart" data-type="bar" data-labels="<c:out value='${chartLabels}'/>" data-values="<c:out value='${chartData}'/>"></canvas></c:otherwise></c:choose></div>
  <div class="card"><div class="row-between"><h3>Notifications</h3><a href="${ctx}/notifications">View all</a></div>
    <c:forEach var="n" items="${notifications}"><p class="note-line ${n.is_read ? '' : 'unread'}"><c:out value="${n.message}"/></p></c:forEach>
    <c:if test="${empty notifications}"><p class="muted">No notifications.</p></c:if></div>
</div>
<div class="card mt"><h3>My events</h3><div class="table-wrap"><table>
  <tr><th>Event</th><th>Date</th><th>Status</th><th>Sold / Capacity</th><th>Revenue</th><th>Actions</th></tr>
  <c:forEach var="e" items="${myEvents}"><tr>
    <td><c:out value="${e.title}"/></td>
    <td><fmt:formatDate value="${e.event_date}" pattern="dd MMM yyyy"/></td>
    <td><span class="badge b-${e.status}"><c:out value="${e.status}"/></span><c:if test="${e.status=='REJECTED' && not empty e.admin_feedback}"><br><small class="muted">Admin: <c:out value="${e.admin_feedback}"/></small></c:if></td>
    <td>${e.sold} / ${e.capacity}</td>
    <td>&#8377;<fmt:formatNumber value="${e.revenue}" maxFractionDigits="0"/></td>
    <td class="actions"><a class="btn btn-sm btn-outline" href="${ctx}/organizer/event/manage?id=${e.id}">Manage</a>
      <c:if test="${e.status != 'CANCELLED' && e.status != 'COMPLETED'}"><a class="btn btn-sm btn-outline" href="${ctx}/organizer/event/edit?id=${e.id}">Edit</a></c:if></td>
  </tr></c:forEach>
  <c:if test="${empty myEvents}"><tr><td colspan="6" class="muted">You have not created any events yet.</td></tr></c:if>
</table></div></div>
</section>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
