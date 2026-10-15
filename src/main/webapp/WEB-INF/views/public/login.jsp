<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Login" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container center-box">
  <form class="card form" method="post" action="${ctx}/login">
    <h1>Welcome back</h1>
    <input type="hidden" name="_csrf" value="${csrf}">
    <label>Email<input type="email" name="email" value="<c:out value='${email}'/>" required autofocus></label>
    <label>Password<input type="password" name="password" required></label>
    <button class="btn btn-primary block" type="submit">Log in</button>
    <p class="muted center">New here? <a href="${ctx}/register">Create an account</a></p>
    <div class="demo-box"><strong>Demo accounts</strong> (local testing only)<br>
      Admin: admin@eventsphere.com / Admin@123<br>
      Organizer: organizer@eventsphere.com / Organizer@123<br>
      Attendee: attendee@eventsphere.com / Attendee@123</div>
  </form>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
