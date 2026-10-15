<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="My Dashboard" scope="request"/>
<c:set var="active" value="dashboard" scope="request"/>
<%@ taglib prefix="es" tagdir="/WEB-INF/tags" %>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container dash">
<jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>
<section class="dash-main">
<h1>Hello, <c:out value="${sessionScope.user.name}"/></h1>
<div class="grid grid-3">
  <div class="card stat"><i class="fa-solid fa-calendar-day"></i><span>${stats.upcoming}</span><small>Upcoming bookings</small></div>
  <div class="card stat"><i class="fa-solid fa-ticket"></i><span>${stats.total}</span><small>Total bookings</small></div>
  <div class="card stat"><i class="fa-regular fa-bell"></i><span>${stats.unread}</span><small>Unread notifications</small></div>
</div>

<div class="card mt"><h3>Upcoming events</h3><div class="table-wrap"><table>
  <tr><th>Event</th><th>Date</th><th>Reference</th><th>Status</th><th>Actions</th></tr>
  <c:forEach var="b" items="${upcoming}"><tr>
    <td><a href="${ctx}/event?id=${b.event_id}"><c:out value="${b.title}"/></a></td>
    <td><fmt:formatDate value="${b.event_date}" pattern="dd MMM yyyy"/> <fmt:formatDate value="${b.start_time}" type="time" pattern="hh:mm a"/></td>
    <td><c:out value="${b.reference}"/></td><td><span class="badge b-${b.status}"><c:out value="${b.status}"/></span></td>
    <td class="actions"><c:choose><c:when test="${b.status=='PENDING'}"><a class="btn btn-sm btn-primary" href="${ctx}/attendee/pay?ref=${b.reference}">Pay (demo)</a></c:when>
      <c:otherwise><a class="btn btn-sm btn-outline" href="${ctx}/attendee/booking?ref=${b.reference}">Ticket</a></c:otherwise></c:choose>
      <form method="post" action="${ctx}/attendee/cancel" class="inline" data-confirm="Cancel this booking?"><input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="ref" value="${b.reference}"><button class="btn btn-sm btn-danger">Cancel</button></form></td>
  </tr></c:forEach>
  <c:if test="${empty upcoming}"><tr><td colspan="5" class="muted">No upcoming bookings. <a href="${ctx}/events">Browse events</a></td></tr></c:if>
</table></div></div>

<div class="grid grid-2 mt">
  <div class="card"><div class="row-between"><h3>Recent notifications</h3><a href="${ctx}/notifications">View all</a></div>
    <c:forEach var="n" items="${notifications}"><p class="note-line ${n.is_read ? '' : 'unread'}"><c:out value="${n.message}"/></p></c:forEach>
    <c:if test="${empty notifications}"><p class="muted">No notifications.</p></c:if></div>
  <div class="card" id="profile"><h3>Profile settings</h3>
    <form method="post" action="${ctx}/attendee/profile" class="form">
      <input type="hidden" name="_csrf" value="${csrf}">
      <label>Name<input name="name" value="<c:out value='${profile.name}'/>" required maxlength="100"></label>
      <label>Email (cannot be changed)<input value="<c:out value='${profile.email}'/>" disabled></label>
      <label>Phone<input name="phone" value="<c:out value='${profile.phone}'/>" maxlength="15"></label>
      <label>Current password (only to change it)<input type="password" name="current"></label>
      <label>New password<input type="password" name="newpass" minlength="8"></label>
      <button class="btn btn-primary">Save profile</button>
    </form></div>
</div>

<div class="card mt"><h3>Booking history</h3><div class="table-wrap"><table>
  <tr><th>Reference</th><th>Event</th><th>Amount</th><th>Status</th><th>Booked on</th></tr>
  <c:forEach var="b" items="${history}"><tr><td><a href="${ctx}/attendee/booking?ref=${b.reference}"><c:out value="${b.reference}"/></a></td><td><c:out value="${b.title}"/></td>
    <td>&#8377;<fmt:formatNumber value="${b.total_amount}" minFractionDigits="2"/></td><td><span class="badge b-${b.status}"><c:out value="${b.status}"/></span></td>
    <td><fmt:formatDate value="${b.created_at}" pattern="dd MMM yyyy"/></td></tr></c:forEach>
  <c:if test="${empty history}"><tr><td colspan="5" class="muted">No bookings yet.</td></tr></c:if>
</table></div></div>

<h2>Recommended for you</h2>
<div class="grid grid-3"><c:forEach var="e" items="${recs}"><es:eventCard ev="${e}"/></c:forEach></div>
</section>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
