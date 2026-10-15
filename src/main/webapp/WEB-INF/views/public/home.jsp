<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="es" tagdir="/WEB-INF/tags" %>
<c:set var="pageTitle" value="Discover Events" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp"/>
<section class="hero">
  <div class="container">
    <h1>Discover events you'll love</h1>
    <p>Find, book and manage events in one place &mdash; conferences, workshops, music and more.</p>
    <form class="hero-search" action="${ctx}/events" method="get">
      <input name="q" placeholder="Search events, e.g. Python, music, summit" maxlength="100">
      <button class="btn btn-accent" type="submit"><i class="fa-solid fa-magnifying-glass"></i> Search</button>
    </form>
  </div>
</section>
<div class="container">
  <h2>Featured events</h2>
  <div class="grid grid-3"><c:forEach var="e" items="${featured}"><es:eventCard ev="${e}"/></c:forEach></div>
  <c:if test="${empty featured}"><p class="muted">No events published yet.</p></c:if>

  <h2>Browse by category</h2>
  <div class="grid grid-4">
    <c:forEach var="cat" items="${categories}">
      <a class="card cat" href="${ctx}/events?cat=${cat.id}"><i class="fa-solid ${cat.icon}"></i><strong><c:out value="${cat.name}"/></strong><small class="muted">${cat.n} upcoming</small></a>
    </c:forEach>
  </div>

  <h2>Upcoming events</h2>
  <div class="grid grid-3"><c:forEach var="e" items="${upcoming}"><es:eventCard ev="${e}"/></c:forEach></div>
  <div class="center mt"><a class="btn btn-outline" href="${ctx}/events">See all events</a></div>

  <div class="cta">
    <h2>Hosting an event?</h2>
    <p>Create your free organizer account, publish tickets and track registrations in real time.</p>
    <a class="btn btn-accent" href="${ctx}/register">Become an organizer</a>
  </div>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp"/>
