<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="es" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="Explore Events" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<div class="container">
  <h1>Explore events</h1>
  <form class="card filters" method="get" action="${ctx}/events">
    <input name="q" value="<c:out value='${q}'/>" placeholder="Search by name" maxlength="100">
    <select name="cat"><option value="">All categories</option>
      <c:forEach var="c" items="${categories}"><option value="${c.id}" ${cat == c.id.toString() ? 'selected' : ''}><c:out value="${c.name}"/></option></c:forEach>
    </select>
    <input name="loc" value="<c:out value='${loc}'/>" placeholder="Location" maxlength="100">
    <input type="date" name="from" value="<c:out value='${from}'/>" title="On or after this date">
    <select name="price"><option value="">Any price</option>
      <option value="free" ${price=='free'?'selected':''}>Free</option><option value="paid" ${price=='paid'?'selected':''}>Paid</option></select>
    <select name="sort">
      <option value="date" ${sort=='date'?'selected':''}>Soonest first</option>
      <option value="newest" ${sort=='newest'?'selected':''}>Newest added</option>
      <option value="price_asc" ${sort=='price_asc'?'selected':''}>Price: low to high</option>
      <option value="price_desc" ${sort=='price_desc'?'selected':''}>Price: high to low</option></select>
    <button class="btn btn-primary" type="submit">Apply</button>
    <a class="btn btn-outline" href="${ctx}/events">Reset</a>
  </form>
  <p class="muted">${total} event(s) found</p>
  <div class="grid grid-3"><c:forEach var="e" items="${events}"><es:eventCard ev="${e}"/></c:forEach></div>
  <c:if test="${empty events}"><div class="card center muted">No events match your search.</div></c:if>

  <c:if test="${pages > 1}">
    <div class="pager">
      <c:forEach begin="1" end="${pages}" var="i">
        <c:url var="u" value="/events"><c:param name="q" value="${q}"/><c:param name="cat" value="${cat}"/><c:param name="loc" value="${loc}"/><c:param name="from" value="${from}"/><c:param name="price" value="${price}"/><c:param name="sort" value="${sort}"/><c:param name="page" value="${i}"/></c:url>
        <a class="${i == page ? 'active' : ''}" href="<c:out value='${u}'/>">${i}</a>
      </c:forEach>
    </div>
  </c:if>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
