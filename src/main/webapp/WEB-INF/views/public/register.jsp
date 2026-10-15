<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Register" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container center-box">
  <form class="card form" method="post" action="${ctx}/register">
    <h1>Create your account</h1>
    <input type="hidden" name="_csrf" value="${csrf}">
    <label>Full name<input name="name" value="<c:out value='${f_name}'/>" required maxlength="100"></label>
    <label>Email<input type="email" name="email" value="<c:out value='${f_email}'/>" required maxlength="150"></label>
    <label>Phone (optional)<input name="phone" value="<c:out value='${f_phone}'/>" maxlength="15"></label>
    <label>I want to<select name="role">
      <option value="ATTENDEE" ${f_role != 'ORGANIZER' ? 'selected' : ''}>Attend events (Attendee)</option>
      <option value="ORGANIZER" ${f_role == 'ORGANIZER' ? 'selected' : ''}>Host events (Organizer)</option></select></label>
    <label>Password<input type="password" name="password" required minlength="8" placeholder="8+ characters, letter and digit"></label>
    <label>Confirm password<input type="password" name="confirm" required minlength="8"></label>
    <button class="btn btn-primary block" type="submit">Register</button>
    <p class="muted center">Already registered? <a href="${ctx}/login">Log in</a></p>
  </form>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
