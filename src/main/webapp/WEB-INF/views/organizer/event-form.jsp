<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Event Form" scope="request"/>
<c:set var="active" value="new" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container dash">
<jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>
<section class="dash-main">
<h1><c:out value="${empty ev.id ? 'Create event' : 'Edit event'}"/></h1>
<form class="card form wide" method="post" action="${ctx}/organizer/event/save">
  <input type="hidden" name="_csrf" value="${csrf}">
  <c:if test="${not empty ev.id}"><input type="hidden" name="id" value="${ev.id}"></c:if>
  <label>Title<input name="title" value="<c:out value='${ev.title}'/>" required maxlength="150"></label>
  <label>Description<textarea name="description" rows="5" required maxlength="5000"><c:out value="${ev.description}"/></textarea></label>
  <div class="form-row">
    <label>Category<select name="category_id" required><option value="">Select...</option>
      <c:forEach var="c" items="${cats}"><option value="${c.id}" ${ev.category_id == c.id || ev.category_id == c.id.toString() ? 'selected' : ''}><c:out value="${c.name}"/></option></c:forEach></select></label>
    <label>Banner image URL (optional)<input name="banner_url" value="<c:out value='${ev.banner_url}'/>" placeholder="https://..." maxlength="500"></label>
  </div>
  <label>Venue or online meeting information<input name="venue" value="<c:out value='${ev.venue}'/>" required maxlength="255"></label>
  <label class="check"><input type="checkbox" name="is_online" ${ev.is_online ? 'checked' : ''}> This is an online event</label>
  <div class="form-row">
    <label>Date<input type="date" name="event_date" value="<c:out value='${ev.event_date}'/>" required></label>
    <label>Start time<input type="time" name="start_time" value="<c:out value='${ev.start_time}'/>" required></label>
    <label>End time<input type="time" name="end_time" value="<c:out value='${ev.end_time}'/>" required></label>
  </div>
  <div class="form-row">
    <label>Registration deadline (optional)<input type="datetime-local" name="reg_deadline" value="<c:out value='${ev.deadline_str}'/>"></label>
    <label>Maximum capacity<input type="number" name="capacity" min="1" max="100000" value="<c:out value='${ev.capacity}'/>" required></label>
  </div>
  <div><button class="btn btn-primary" type="submit">Save as draft</button> <a class="btn btn-outline" href="${ctx}/organizer/dashboard">Cancel</a></div>
  <small class="muted">After saving, add ticket types and submit the event for administrator approval from the Manage page.</small>
</form>
</section>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
