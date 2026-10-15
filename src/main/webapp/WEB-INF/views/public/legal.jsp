<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${doc == 'terms' ? 'Terms of Use' : 'Privacy Policy'}" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container narrow">
  <c:choose>
    <c:when test="${doc == 'terms'}">
      <h1>Terms of Use</h1>
      <p>EventSphere is a college project. By using it you agree to provide accurate information and not to misuse the platform.</p>
      <ul><li>Organizers are responsible for the accuracy of their event details.</li>
      <li>Events are published only after administrator approval.</li>
      <li>Confirmed bookings can be cancelled until the cancellation window set by the administrator (default 24 hours before the event).</li>
      <li><strong>Payments are simulated.</strong> No real money is charged and no refunds of real money occur.</li></ul>
    </c:when>
    <c:otherwise>
      <h1>Privacy Policy</h1>
      <p>We store only what is needed to run the platform: name, email, optional phone number, bookings and notifications.</p>
      <ul><li>Passwords are stored as BCrypt hashes, never in plain text.</li>
      <li>No card numbers, CVVs or payment credentials are collected or stored.</li>
      <li>Organizers can see only the details of attendees who booked their own events.</li></ul>
    </c:otherwise>
  </c:choose>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
