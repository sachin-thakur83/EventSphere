<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Booking Confirmation" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container center-box">
  <div class="card form ticket" id="ticket">
    <h1><c:choose><c:when test="${booking.status=='CONFIRMED'}"><i class="fa-solid fa-circle-check ok-ic"></i> Booking confirmed</c:when><c:otherwise>Booking <c:out value="${booking.status}"/></c:otherwise></c:choose></h1>
    <div class="ref">Reference: <strong><c:out value="${booking.reference}"/></strong></div>
    <p><strong><c:out value="${booking.title}"/></strong><br>
      <fmt:formatDate value="${booking.event_date}" pattern="EEEE, dd MMMM yyyy"/> at <fmt:formatDate value="${booking.start_time}" type="time" pattern="hh:mm a"/><br>
      <c:out value="${booking.venue}"/></p>
    <p>Attendee: <c:out value="${booking.user_name}"/> (<c:out value="${booking.user_email}"/>)</p>
    <table><tr><th>Ticket</th><th>Qty</th><th class="r">Amount</th></tr>
      <c:forEach var="i" items="${items}"><tr><td><c:out value="${i.name}"/></td><td>${i.quantity}</td><td class="r">&#8377;<fmt:formatNumber value="${i.line_total}" minFractionDigits="2"/></td></tr></c:forEach>
      <tr><th colspan="2">Total</th><th class="r">&#8377;<fmt:formatNumber value="${booking.total_amount}" minFractionDigits="2"/></th></tr></table>
    <c:if test="${not empty payment}"><p class="muted">Payment: <c:out value="${payment.method}"/> &middot; <c:out value="${payment.status}"/> &middot; <c:out value="${payment.txn_ref}"/> <strong>(simulated)</strong></p></c:if>
    <p><span class="badge b-${booking.status}"><c:out value="${booking.status}"/></span></p>
    <div class="actions no-print">
      <button type="button" class="btn btn-primary" onclick="window.print()"><i class="fa-solid fa-print"></i> Print / Save as PDF</button>
      <a class="btn btn-outline" href="${ctx}/attendee/dashboard">Dashboard</a>
      <c:if test="${booking.status=='CONFIRMED'}"><form method="post" action="${ctx}/attendee/cancel" class="inline" data-confirm="Cancel this booking?"><input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="ref" value="<c:out value='${booking.reference}'/>"><button class="btn btn-danger">Cancel booking</button></form></c:if>
    </div>
  </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
