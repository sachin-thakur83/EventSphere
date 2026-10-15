<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Demo Payment" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container center-box">
  <div class="card form">
    <div class="alert warn"><strong>DEMO PAYMENT.</strong> This is a simulation for the college project. No real money is charged and no card details are collected.</div>
    <h1>Complete payment</h1>
    <p><strong><c:out value="${booking.title}"/></strong><br><small class="muted">Reference <c:out value="${booking.reference}"/></small></p>
    <table><c:forEach var="i" items="${items}"><tr><td><c:out value="${i.name}"/> x ${i.quantity}</td><td class="r">&#8377;<fmt:formatNumber value="${i.line_total}" minFractionDigits="2"/></td></tr></c:forEach>
      <tr><th>Total</th><th class="r">&#8377;<fmt:formatNumber value="${booking.total_amount}" minFractionDigits="2"/></th></tr></table>
    <form method="post" action="${ctx}/attendee/pay">
      <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="ref" value="<c:out value='${booking.reference}'/>">
      <label>Demo payment method<select name="method"><option value="UPI">UPI (demo)</option><option value="CARD">Card (demo)</option><option value="NETBANKING">Net banking (demo)</option></select></label>
      <button class="btn btn-primary block" name="outcome" value="success">Simulate successful payment</button>
      <button class="btn btn-outline block" name="outcome" value="fail">Simulate failed payment</button>
    </form>
    <form method="post" action="${ctx}/attendee/cancel" data-confirm="Cancel this booking?"><input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="ref" value="<c:out value='${booking.reference}'/>"><button class="btn btn-danger btn-sm">Cancel booking</button></form>
  </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
