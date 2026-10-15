<%@ tag body-content="empty" pageEncoding="UTF-8" %>
<%@ attribute name="ev" required="true" type="java.util.Map" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<article class="card event-card">
  <c:choose>
    <c:when test="${not empty ev.banner_url}"><img class="banner" src="<c:out value='${ev.banner_url}'/>" alt="" loading="lazy"></c:when>
    <c:otherwise><div class="banner ph"><i class="fa-solid ${ev.category_icon}"></i></div></c:otherwise>
  </c:choose>
  <div class="card-body">
    <span class="chip"><c:out value="${ev.category_name}"/></span>
    <h3><a href="${pageContext.request.contextPath}/event?id=${ev.id}"><c:out value="${ev.title}"/></a></h3>
    <p class="muted"><i class="fa-regular fa-calendar"></i> <fmt:formatDate value="${ev.event_date}" pattern="dd MMM yyyy"/> &middot; <fmt:formatDate value="${ev.start_time}" type="time" pattern="hh:mm a"/></p>
    <p class="muted"><i class="fa-solid ${ev.is_online ? 'fa-video' : 'fa-location-dot'}"></i> <c:out value="${ev.venue}"/></p>
    <div class="row-between">
      <strong>
        <c:choose>
          <c:when test="${ev.min_price == null}">Tickets soon</c:when>
          <c:when test="${ev.min_price == 0}">Free</c:when>
          <c:otherwise>From &#8377;<fmt:formatNumber value="${ev.min_price}" maxFractionDigits="0"/></c:otherwise>
        </c:choose>
      </strong>
      <a class="btn btn-primary btn-sm" href="${pageContext.request.contextPath}/event?id=${ev.id}">View</a>
    </div>
  </div>
</article>
