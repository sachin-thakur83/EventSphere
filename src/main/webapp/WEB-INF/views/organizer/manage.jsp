<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Manage Event" scope="request"/>
<c:set var="active" value="dashboard" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container dash">
<jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>
<section class="dash-main">
<div class="row-between">
  <div><a href="${ctx}/organizer/dashboard">&larr; Dashboard</a><h1><c:out value="${ev.title}"/> <span class="badge b-${ev.status}"><c:out value="${ev.status}"/></span></h1></div>
  <div class="actions">
    <a class="btn btn-outline" href="${ctx}/event?id=${ev.id}">Public page</a>
    <c:if test="${ev.status != 'CANCELLED' && ev.status != 'COMPLETED'}"><a class="btn btn-outline" href="${ctx}/organizer/event/edit?id=${ev.id}">Edit</a></c:if>
    <c:if test="${ev.status == 'DRAFT' || ev.status == 'REJECTED'}">
      <form method="post" action="${ctx}/organizer/event/submit" data-confirm="Submit this event for admin approval?"><input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${ev.id}"><button class="btn btn-primary">Submit for approval</button></form></c:if>
    <c:if test="${ev.status != 'CANCELLED' && ev.status != 'COMPLETED'}">
      <form method="post" action="${ctx}/organizer/event/cancel" data-confirm="Cancel this event? All bookings will be cancelled."><input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${ev.id}"><button class="btn btn-danger">Cancel event</button></form></c:if>
  </div>
</div>
<c:if test="${not empty ev.admin_feedback}"><div class="alert err"><strong>Administrator feedback:</strong> <c:out value="${ev.admin_feedback}"/></div></c:if>

<div class="card"><h3>Ticket types <small class="muted">(capacity ${ev.capacity}, reserved ${ev.sold})</small></h3>
  <c:forEach var="t" items="${tickets}"><form method="post" action="${ctx}/organizer/ticket/save" id="t${t.id}"></form></c:forEach>
  <div class="table-wrap"><table>
    <tr><th>Name</th><th>Price (&#8377;)</th><th>Total qty</th><th>Sold</th><th>Action</th></tr>
    <c:forEach var="t" items="${tickets}">
      <tr>
        <td><input form="t${t.id}" name="name" value="<c:out value='${t.name}'/>" required maxlength="60"></td>
        <td><input form="t${t.id}" name="price" type="number" step="0.01" min="0" value="${t.price}" ${t.quantity_sold > 0 ? 'readonly title="Price is locked after sales"' : ''}></td>
        <td><input form="t${t.id}" name="quantity" type="number" min="${t.quantity_sold > 0 ? t.quantity_sold : 1}" value="${t.quantity_total}" required></td>
        <td>${t.quantity_sold}</td>
        <td class="actions">
          <input form="t${t.id}" type="hidden" name="_csrf" value="${csrf}"><input form="t${t.id}" type="hidden" name="id" value="${t.id}"><input form="t${t.id}" type="hidden" name="event_id" value="${ev.id}">
          <button form="t${t.id}" class="btn btn-sm btn-outline">Save</button>
          <form method="post" action="${ctx}/organizer/ticket/delete" data-confirm="Delete this ticket type?" class="inline"><input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${t.id}"><input type="hidden" name="event_id" value="${ev.id}"><button class="btn btn-sm btn-danger" ${t.quantity_sold > 0 ? 'disabled' : ''}>Delete</button></form>
        </td></tr>
    </c:forEach>
  </table></div>
  <form method="post" action="${ctx}/organizer/ticket/save" class="filters">
    <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="event_id" value="${ev.id}">
    <input name="name" placeholder="Name (General, VIP...)" required maxlength="60">
    <input name="price" type="number" step="0.01" min="0" placeholder="Price (0 = free)" required>
    <input name="quantity" type="number" min="1" placeholder="Quantity" required>
    <button class="btn btn-primary">Add ticket type</button>
  </form>
</div>

<div class="card mt"><h3>Attendees &amp; registrations</h3>
  <form method="get" action="${ctx}/organizer/event/manage" class="filters">
    <input type="hidden" name="id" value="${ev.id}">
    <input name="q" value="<c:out value='${q}'/>" placeholder="Search name, email or reference">
    <select name="st"><option value="">All statuses</option>
      <c:forEach var="s" items="${['CONFIRMED','PENDING','CANCELLED','FAILED']}"><option ${st==s?'selected':''}>${s}</option></c:forEach></select>
    <button class="btn btn-outline">Filter</button>
  </form>
  <div class="table-wrap"><table>
    <tr><th>Reference</th><th>Name</th><th>Email</th><th>Tickets</th><th>Amount</th><th>Status</th><th>Booked</th></tr>
    <c:forEach var="a" items="${attendees}"><tr><td><c:out value="${a.reference}"/></td><td><c:out value="${a.name}"/></td><td><c:out value="${a.email}"/></td><td><c:out value="${a.tickets}"/></td>
      <td>&#8377;<fmt:formatNumber value="${a.total_amount}" minFractionDigits="2"/></td><td><span class="badge b-${a.status}"><c:out value="${a.status}"/></span></td>
      <td><fmt:formatDate value="${a.created_at}" pattern="dd MMM, hh:mm a"/></td></tr></c:forEach>
    <c:if test="${empty attendees}"><tr><td colspan="7" class="muted">No registrations found.</td></tr></c:if>
  </table></div>
</div>

<div class="card mt"><h3>Announcements</h3>
  <c:choose><c:when test="${ev.status == 'APPROVED'}">
  <form method="post" action="${ctx}/organizer/announce" class="form">
    <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="event_id" value="${ev.id}">
    <label>Title<input name="title" required maxlength="120"></label>
    <label>Message<textarea name="message" rows="3" required maxlength="1000"></textarea></label>
    <button class="btn btn-primary">Send to confirmed attendees</button>
  </form></c:when><c:otherwise><p class="muted">Available once the event is approved.</p></c:otherwise></c:choose>
  <c:forEach var="a" items="${announcements}"><div class="note-line"><strong><c:out value="${a.title}"/></strong> &mdash; <c:out value="${a.message}"/> <small class="muted">(<fmt:formatDate value="${a.created_at}" pattern="dd MMM"/>)</small></div></c:forEach>
</div>
</section>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
