<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Categories" scope="request"/>
<c:set var="active" value="categories" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container dash">
<jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>
<section class="dash-main">
<h1>Event categories</h1>
<form class="card filters" method="post" action="${ctx}/admin/categories/add">
  <input type="hidden" name="_csrf" value="${csrf}">
  <input name="name" placeholder="New category name" required maxlength="60">
  <input name="icon" placeholder="Font Awesome icon, e.g. fa-star" maxlength="40">
  <button class="btn btn-primary">Add category</button>
</form>
<div class="card"><div class="table-wrap"><table>
  <tr><th>Icon</th><th>Name</th><th>Events</th><th>Action</th></tr>
  <c:forEach var="c" items="${cats}"><tr>
    <td><i class="fa-solid ${c.icon}"></i></td><td><c:out value="${c.name}"/></td><td>${c.events}</td>
    <td><form method="post" action="${ctx}/admin/categories/delete" data-confirm="Delete this category?"><input type="hidden" name="_csrf" value="${csrf}"><input type="hidden" name="id" value="${c.id}"><button class="btn btn-sm btn-danger" ${c.events > 0 ? 'disabled title="In use"' : ''}>Delete</button></form></td>
  </tr></c:forEach>
</table></div></div>
</section>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
