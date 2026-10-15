<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Events" scope="request"/>
<c:set var="active" value="events" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container dash">
<jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>
<section class="dash-main">
<h1>Events &amp; approvals</h1>
<div class="tabs">
  <a class="${empty status?'active':''}" href="${ctx}/admin/events">All</a>
  <c:forEach var="s" items="${['PENDING','APPROVED','REJECTED','DRAFT','COMPLETED','CANCELLED']}"><a class="${status==s?'active':''}" href="${ctx}/admin/events?status=${s}"><c:out value="${s}"/></a></c:forEach>
</div>
<div class="card"><div class="table-wrap"><table>
  <tr><th>Event</th><th>Organizer</th><th>Date</th><th>Tickets</th><th>Status</th><th>Review</th></tr>
  <c:forEach var="e" items="${events}">
    <tr>
      <td><a href="${ctx}/event?id=${e.id}"><c:out value="${e.title}"/></a><br><small class="muted"><c:out value="${e.category_name}"/></small></td>
      <td><c:out value="${e.organizer_name}"/></td>
      <td><fmt:formatDate value="${e.event_date}" pattern="dd MMM yyyy"/></td>
      <td>${e.ticket_types} type(s)</td>
      <td><span class="badge b-${e.status}"><c:out value="${e.status}"/></span><c:if test="${not empty e.admin_feedback}"><br><small class="muted"><c:out value="${e.admin_feedback}"/></small></c:if></td>
      <td><c:if test="${e.status == 'PENDING'}">
        <form method="post" action="${ctx}/admin/events/review" class="review">
          <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${e.id}">
          <input name="feedback" placeholder="Feedback (required to reject)" maxlength="500">
          <button class="btn btn-sm btn-primary" name="decision" value="approve">Approve</button>
          <button class="btn btn-sm btn-danger" name="decision" value="reject">Reject</button>
        </form></c:if></td>
    </tr>
  </c:forEach>
  <c:if test="${empty events}"><tr><td colspan="6" class="muted">No events found.</td></tr></c:if>
</table></div></div>
</section>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
