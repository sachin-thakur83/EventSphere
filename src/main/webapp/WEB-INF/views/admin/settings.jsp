<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Settings" scope="request"/>
<c:set var="active" value="settings" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container dash">
<jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>
<section class="dash-main">
<h1>System settings</h1>
<form class="card form" method="post" action="${ctx}/admin/settings">
  <input type="hidden" name="_csrf" value="${csrf}">
  <label>Site name<input name="site_name" value="<c:out value='${settings.site_name}'/>" required maxlength="60"></label>
  <label>Support email<input type="email" name="support_email" value="<c:out value='${settings.support_email}'/>" required></label>
  <label>Max tickets per booking<input type="number" name="max_tickets_per_booking" min="1" max="20" value="<c:out value='${settings.max_tickets_per_booking}'/>" required></label>
  <label>Cancellation window (hours before event)<input type="number" name="cancellation_hours" min="0" max="720" value="<c:out value='${settings.cancellation_hours}'/>" required></label>
  <button class="btn btn-primary">Save settings</button>
</form>
</section>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
