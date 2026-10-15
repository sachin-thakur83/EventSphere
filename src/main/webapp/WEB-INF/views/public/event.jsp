<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="${ev.title}" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container">
  <c:if test="${ev.status != 'APPROVED'}"><div class="alert err">Preview: this event is <strong><c:out value="${ev.status}"/></strong> and is not visible to the public.</div></c:if>
  <div class="detail-grid">
    <div>
      <c:choose>
        <c:when test="${not empty ev.banner_url}"><img class="detail-banner" src="<c:out value='${ev.banner_url}'/>" alt=""></c:when>
        <c:otherwise><div class="detail-banner ph"><i class="fa-solid ${ev.category_icon}"></i></div></c:otherwise>
      </c:choose>
      <span class="chip"><c:out value="${ev.category_name}"/></span>
      <h1><c:out value="${ev.title}"/></h1>
      <ul class="meta">
        <li><i class="fa-regular fa-calendar"></i> <fmt:formatDate value="${ev.event_date}" pattern="EEEE, dd MMMM yyyy"/></li>
        <li><i class="fa-regular fa-clock"></i> <fmt:formatDate value="${ev.start_time}" type="time" pattern="hh:mm a"/> - <fmt:formatDate value="${ev.end_time}" type="time" pattern="hh:mm a"/></li>
        <li><i class="fa-solid ${ev.is_online ? 'fa-video' : 'fa-location-dot'}"></i> <c:out value="${ev.venue}"/> <c:if test="${ev.is_online}">(Online)</c:if></li>
        <li><i class="fa-regular fa-user"></i> Organized by <c:out value="${ev.organizer_name}"/></li>
        <c:if test="${not empty ev.reg_deadline}"><li><i class="fa-solid fa-hourglass-half"></i> Register by <fmt:formatDate value="${ev.reg_deadline}" pattern="dd MMM yyyy, hh:mm a"/></li></c:if>
        <li><i class="fa-solid fa-chair"></i> Capacity <c:out value="${ev.capacity}"/> &middot; <c:out value="${ev.capacity - ev.sold}"/> seats left</li>
      </ul>
      <h3>About this event</h3>
      <p class="pre"><c:out value="${ev.description}"/></p>
      <c:if test="${not empty announcements}">
        <h3>Organizer updates</h3>
        <c:forEach var="a" items="${announcements}"><div class="card note"><i class="fa-solid fa-bullhorn"></i><div><strong><c:out value="${a.title}"/></strong><br><c:out value="${a.message}"/><br><small class="muted"><fmt:formatDate value="${a.created_at}" pattern="dd MMM yyyy"/></small></div></div></c:forEach>
      </c:if>
    </div>

    <aside class="card ticket-box">
      <h3>Tickets</h3>
      <c:choose>
        <c:when test="${ev.status != 'APPROVED'}"><p class="muted">Booking opens after the event is approved.</p></c:when>
        <c:when test="${empty sessionScope.user}"><p>Please log in as an attendee to book tickets.</p><a class="btn btn-primary" href="${ctx}/login">Log in</a> <a class="btn btn-outline" href="${ctx}/register">Sign up</a></c:when>
        <c:when test="${sessionScope.user.role != 'ATTENDEE'}"><p class="muted">Only attendee accounts can book tickets.</p></c:when>
        <c:when test="${not empty myBooking}">
          <p>You already have a <strong><c:out value="${myBooking.status}"/></strong> booking for this event.</p>
          <a class="btn btn-primary" href="${ctx}/attendee/booking?ref=${myBooking.reference}">View booking</a>
        </c:when>
        <c:when test="${empty tickets}"><p class="muted">Tickets are not available yet.</p></c:when>
        <c:otherwise>
          <form method="post" action="${ctx}/attendee/book" id="bookForm">
            <input type="hidden" name="_csrf" value="${csrf}">
            <input type="hidden" name="event_id" value="${ev.id}">
            <c:forEach var="t" items="${tickets}">
              <c:set var="left" value="${t.quantity_total - t.quantity_sold}"/>
              <div class="ticket-row">
                <div><strong><c:out value="${t.name}"/></strong><br>
                  <span class="muted"><c:choose><c:when test="${t.price == 0}">Free</c:when><c:otherwise>&#8377;<fmt:formatNumber value="${t.price}" minFractionDigits="2"/></c:otherwise></c:choose>
                  &middot; ${left > 0 ? left : 0} left</span></div>
                <input type="number" name="qty_${t.id}" min="0" max="${left > 0 ? left : 0}" value="0" data-price="${t.price}" ${left <= 0 ? 'disabled' : ''}>
              </div>
            </c:forEach>
            <p class="row-between"><span>Estimated total</span><strong id="total">&#8377;0.00</strong></p>
            <small class="muted">The final amount is calculated by the server.</small>
            <button class="btn btn-primary block" type="submit">Book now</button>
          </form>
        </c:otherwise>
      </c:choose>
    </aside>
  </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
