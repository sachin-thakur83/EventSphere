<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Users" scope="request"/>
<c:set var="active" value="users" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container dash">
<jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>
<section class="dash-main">
<h1>User management</h1>
<form class="card filters" method="get" action="${ctx}/admin/users">
  <input name="q" value="<c:out value='${q}'/>" placeholder="Search name or email" maxlength="100">
  <select name="role"><option value="">All roles</option>
    <option value="ADMIN" ${role=='ADMIN'?'selected':''}>Admin</option><option value="ORGANIZER" ${role=='ORGANIZER'?'selected':''}>Organizer</option><option value="ATTENDEE" ${role=='ATTENDEE'?'selected':''}>Attendee</option></select>
  <button class="btn btn-primary">Filter</button><a class="btn btn-outline" href="${ctx}/admin/users">Reset</a>
</form>
<p class="muted">${total} user(s)</p>
<div class="card"><div class="table-wrap"><table>
  <tr><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>Joined</th><th>Action</th></tr>
  <c:forEach var="u" items="${users}">
    <tr>
      <td><c:out value="${u.name}"/></td><td><c:out value="${u.email}"/></td>
      <td><span class="badge b-${u.role}"><c:out value="${u.role}"/></span></td>
      <td><span class="badge b-${u.status}"><c:out value="${u.status}"/></span></td>
      <td><fmt:formatDate value="${u.created_at}" pattern="dd MMM yyyy"/></td>
      <td><c:if test="${u.role != 'ADMIN'}">
        <form method="post" action="${ctx}/admin/users/status" class="inline-form" data-confirm="Change status of this user?">
          <input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${u.id}">
          <select name="status"><option ${u.status=='ACTIVE'?'selected':''}>ACTIVE</option><option ${u.status=='INACTIVE'?'selected':''}>INACTIVE</option><option ${u.status=='SUSPENDED'?'selected':''}>SUSPENDED</option></select>
          <button class="btn btn-sm btn-outline">Update</button>
        </form></c:if></td>
    </tr>
  </c:forEach>
  <c:if test="${empty users}"><tr><td colspan="6" class="muted">No users found.</td></tr></c:if>
</table></div></div>
<c:if test="${pages > 1}"><div class="pager">
  <c:forEach begin="1" end="${pages}" var="i"><c:url var="u" value="/admin/users"><c:param name="q" value="${q}"/><c:param name="role" value="${role}"/><c:param name="page" value="${i}"/></c:url>
  <a class="${i == page ? 'active' : ''}" href="<c:out value='${u}'/>">${i}</a></c:forEach></div></c:if>
</section>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
