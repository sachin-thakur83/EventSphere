<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Admin Dashboard" scope="request"/>
<c:set var="active" value="dashboard" scope="request"/>
<c:set var="needCharts" value="true" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container dash">
<jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>
<section class="dash-main">
<h1>Admin dashboard</h1>
<div class="grid grid-4">
  <div class="card stat"><i class="fa-solid fa-users"></i><span>${stats.users}</span><small>Total users</small></div>
  <div class="card stat"><i class="fa-solid fa-user-tie"></i><span>${stats.organizers}</span><small>Organizers</small></div>
  <div class="card stat"><i class="fa-solid fa-user"></i><span>${stats.attendees}</span><small>Attendees</small></div>
  <div class="card stat"><i class="fa-solid fa-calendar"></i><span>${stats.events}</span><small>Events</small></div>
  <div class="card stat"><i class="fa-solid fa-hourglass-half"></i><span>${stats.pending}</span><small>Pending approvals</small></div>
  <div class="card stat"><i class="fa-solid fa-ticket"></i><span>${stats.bookings}</span><small>Bookings</small></div>
  <div class="card stat"><i class="fa-solid fa-indian-rupee-sign"></i><span><fmt:formatNumber value="${stats.revenue}" maxFractionDigits="0"/></span><small>Confirmed revenue (demo)</small></div>
</div>
<div class="grid grid-2 mt">
  <div class="card"><h3>Events by status</h3>
    <c:choose><c:when test="${empty statusLabels}"><p class="muted">No data yet.</p></c:when>
    <c:otherwise><canvas class="chart" data-type="doughnut" data-labels="<c:out value='${statusLabels}'/>" data-values="<c:out value='${statusData}'/>"></canvas></c:otherwise></c:choose></div>
  <div class="card"><h3>Confirmed bookings by category</h3>
    <c:choose><c:when test="${empty catLabels}"><p class="muted">No data yet.</p></c:when>
    <c:otherwise><canvas class="chart" data-type="bar" data-labels="<c:out value='${catLabels}'/>" data-values="<c:out value='${catData}'/>"></canvas></c:otherwise></c:choose></div>
</div>
<div class="card mt"><div class="row-between"><h3>Pending event approvals</h3><a class="btn btn-outline btn-sm" href="${ctx}/admin/events?status=PENDING">Review all</a></div>
  <div class="table-wrap"><table><tr><th>Event</th><th>Organizer</th><th>Date</th></tr>
  <c:forEach var="e" items="${pendingList}"><tr><td><c:out value="${e.title}"/></td><td><c:out value="${e.organizer_name}"/></td><td><fmt:formatDate value="${e.event_date}" pattern="dd MMM yyyy"/></td></tr></c:forEach>
  <c:if test="${empty pendingList}"><tr><td colspan="3" class="muted">Nothing waiting for approval.</td></tr></c:if></table></div></div>
<div class="grid grid-2 mt">
  <div class="card"><h3>Recent registrations</h3><div class="table-wrap"><table><tr><th>Name</th><th>Role</th><th>Joined</th></tr>
    <c:forEach var="u" items="${recentUsers}"><tr><td><c:out value="${u.name}"/></td><td><span class="badge b-${u.role}"><c:out value="${u.role}"/></span></td><td><fmt:formatDate value="${u.created_at}" pattern="dd MMM"/></td></tr></c:forEach></table></div></div>
  <div class="card"><h3>Recent bookings</h3><div class="table-wrap"><table><tr><th>Ref</th><th>Event</th><th>Status</th></tr>
    <c:forEach var="b" items="${recentBookings}"><tr><td><c:out value="${b.reference}"/></td><td><c:out value="${b.title}"/></td><td><span class="badge b-${b.status}"><c:out value="${b.status}"/></span></td></tr></c:forEach>
    <c:if test="${empty recentBookings}"><tr><td colspan="3" class="muted">No bookings yet.</td></tr></c:if></table></div></div>
</div>
</section>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
