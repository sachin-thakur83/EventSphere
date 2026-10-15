<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<aside class="sidebar">
  <div class="side-user"><i class="fa-solid fa-circle-user"></i> <span><c:out value="${sessionScope.user.name}"/><br><small><c:out value="${sessionScope.user.role}"/></small></span></div>
  <c:choose>
    <c:when test="${sessionScope.user.role == 'ADMIN'}">
      <a class="${active=='dashboard'?'active':''}" href="${ctx}/admin/dashboard"><i class="fa-solid fa-gauge"></i> Dashboard</a>
      <a class="${active=='events'?'active':''}" href="${ctx}/admin/events"><i class="fa-solid fa-calendar-check"></i> Events &amp; Approvals</a>
      <a class="${active=='users'?'active':''}" href="${ctx}/admin/users"><i class="fa-solid fa-users"></i> Users</a>
      <a class="${active=='categories'?'active':''}" href="${ctx}/admin/categories"><i class="fa-solid fa-tags"></i> Categories</a>
      <a class="${active=='activity'?'active':''}" href="${ctx}/admin/activity"><i class="fa-solid fa-clock-rotate-left"></i> Activity &amp; Bookings</a>
      <a class="${active=='settings'?'active':''}" href="${ctx}/admin/settings"><i class="fa-solid fa-gear"></i> Settings</a>
    </c:when>
    <c:when test="${sessionScope.user.role == 'ORGANIZER'}">
      <a class="${active=='dashboard'?'active':''}" href="${ctx}/organizer/dashboard"><i class="fa-solid fa-gauge"></i> Dashboard</a>
      <a class="${active=='new'?'active':''}" href="${ctx}/organizer/event/new"><i class="fa-solid fa-plus"></i> Create Event</a>
      <a href="${ctx}/notifications"><i class="fa-regular fa-bell"></i> Notifications</a>
    </c:when>
    <c:otherwise>
      <a class="${active=='dashboard'?'active':''}" href="${ctx}/attendee/dashboard"><i class="fa-solid fa-gauge"></i> My Dashboard</a>
      <a href="${ctx}/events"><i class="fa-solid fa-magnifying-glass"></i> Browse Events</a>
      <a href="${ctx}/attendee/dashboard#profile"><i class="fa-solid fa-user-gear"></i> Profile</a>
      <a href="${ctx}/notifications"><i class="fa-regular fa-bell"></i> Notifications</a>
    </c:otherwise>
  </c:choose>
</aside>
