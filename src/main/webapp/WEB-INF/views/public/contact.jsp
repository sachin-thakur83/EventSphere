<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Contact Us" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container narrow">
  <h1>Contact us</h1>
  <form class="card form" method="post" action="${ctx}/contact">
    <input type="hidden" name="_csrf" value="${csrf}">
    <label>Name<input name="name" value="<c:out value='${f_name}'/>" required maxlength="100"></label>
    <label>Email<input type="email" name="email" value="<c:out value='${f_email}'/>" required maxlength="150"></label>
    <label>Subject<input name="subject" value="<c:out value='${f_subject}'/>" required maxlength="150"></label>
    <label>Message<textarea name="message" rows="5" required maxlength="2000"><c:out value="${f_message}"/></textarea></label>
    <button class="btn btn-primary" type="submit">Send message</button>
  </form>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
