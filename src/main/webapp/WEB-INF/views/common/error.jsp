<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Error" scope="request"/>
<c:set var="code" value="${requestScope['jakarta.servlet.error.status_code']}"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container center-box">
  <div class="card center">
    <h1><c:out value="${empty code ? 'Error' : code}"/></h1>
    <c:choose>
      <c:when test="${code == 403}"><p>You do not have permission to do that (or your security token expired). Please go back and try again.</p></c:when>
      <c:when test="${code == 404}"><p>The page you are looking for was not found.</p></c:when>
      <c:otherwise><p>Something went wrong on our side. Please try again later.</p></c:otherwise>
    </c:choose>
    <a class="btn btn-primary" href="${ctx}/home">Back to home</a>
  </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
