<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Activity" scope="request"/>
<c:set var="active" value="activity" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container dash">
<jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>
<section class="dash-main">
<h1>Activity &amp; bookings</h1>
<div class="card"><h3>Latest bookings</h3><div class="table-wrap"><table>
  <tr><th>Reference</th><th>Attendee</th><th>Event</th><th>Amount</th><th>Status</th><th>Date</th></tr>
  <c:forEach var="b" items="${bookings}"><tr><td><c:out value="${b.reference}"/></td><td><c:out value="${b.user_name}"/></td><td><c:out value="${b.title}"/></td>
    <td>&#8377;<fmt:formatNumber value="${b.total_amount}" minFractionDigits="2"/></td><td><span class="badge b-${b.status}"><c:out value="${b.status}"/></span></td>
    <td><fmt:formatDate value="${b.created_at}" pattern="dd MMM, hh:mm a"/></td></tr></c:forEach>
  <c:if test="${empty bookings}"><tr><td colspan="6" class="muted">No bookings yet.</td></tr></c:if>
</table></div></div>
<div class="card mt"><h3>Audit log</h3><div class="table-wrap"><table>
  <tr><th>When</th><th>User</th><th>Action</th><th>Details</th></tr>
  <c:forEach var="l" items="${logs}"><tr><td><fmt:formatDate value="${l.created_at}" pattern="dd MMM, hh:mm a"/></td><td><c:out value="${l.user_name}"/></td><td><c:out value="${l.action}"/></td><td><c:out value="${l.details}"/></td></tr></c:forEach>
</table></div></div>
</section>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
