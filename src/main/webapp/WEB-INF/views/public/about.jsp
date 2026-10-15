<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="About" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container narrow">
  <h1>About EventSphere</h1>
  <p>EventSphere is an online event management platform that brings administrators, organizers and attendees together. Organizers create events and sell tickets, attendees discover and book them, and administrators keep the platform safe and organised.</p>
  <div class="grid grid-3">
    <div class="card"><h3><i class="fa-solid fa-user-shield"></i> Administrators</h3><p>Approve events, manage users and categories, and monitor platform activity.</p></div>
    <div class="card"><h3><i class="fa-solid fa-calendar-plus"></i> Organizers</h3><p>Create events, define ticket types, track sales and message attendees.</p></div>
    <div class="card"><h3><i class="fa-solid fa-ticket"></i> Attendees</h3><p>Search events, book tickets, and keep all bookings in one dashboard.</p></div>
  </div>
  <p class="muted mt">Built with Java Servlets, JSP, JDBC and MySQL as a college project. Payments in this version are a clearly labeled demo simulation.</p>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
