<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Notifications" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container narrow">
  <div class="row-between">
    <h1>Notifications</h1>
    <form method="post" action="${ctx}/notifications"><input type="hidden" name="_csrf" value="${csrf}"><button class="btn btn-outline btn-sm">Mark all as read</button></form>
  </div>
  <c:forEach var="n" items="${notifications}">
    <div class="card note ${n.is_read ? '' : 'unread'}">
      <i class="fa-regular fa-bell"></i>
      <div><c:out value="${n.message}"/><br><small class="muted"><fmt:formatDate value="${n.created_at}" pattern="dd MMM yyyy, hh:mm a"/></small></div>
    </div>
  </c:forEach>
  <c:if test="${empty notifications}"><div class="card center muted">No notifications yet.</div></c:if>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
