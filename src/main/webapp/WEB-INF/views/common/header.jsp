<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" scope="request"/>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title><c:out value="${pageTitle}"/> | EventSphere</title>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
  <link rel="stylesheet" href="${ctx}/css/style.css">
</head>
<body>
<header class="navbar">
  <div class="container nav-inner">
    <a class="logo" href="${ctx}/home"><i class="fa-solid fa-circle-nodes"></i> EventSphere</a>
    <button class="menu-btn" type="button" aria-label="Menu" onclick="document.getElementById('mainnav').classList.toggle('open')"><i class="fa-solid fa-bars"></i></button>
    <nav id="mainnav">
      <a href="${ctx}/home">Home</a>
      <a href="${ctx}/events">Explore</a>
      <a href="${ctx}/about">About</a>
      <a href="${ctx}/contact">Contact</a>
      <c:choose>
        <c:when test="${not empty sessionScope.user}">
          <a href="${ctx}/dashboard">Dashboard</a>
          <a href="${ctx}/notifications"><i class="fa-regular fa-bell"></i></a>
          <form action="${ctx}/logout" method="post" class="inline">
            <input type="hidden" name="_csrf" value="${csrf}">
            <button class="btn btn-outline btn-sm" type="submit">Logout (<c:out value="${sessionScope.user.name}"/>)</button>
          </form>
        </c:when>
        <c:otherwise>
          <a href="${ctx}/login">Login</a>
          <a class="btn btn-primary btn-sm" href="${ctx}/register">Sign up</a>
        </c:otherwise>
      </c:choose>
    </nav>
  </div>
</header>
<main>
  <div class="container flash">
    <c:if test="${not empty sessionScope.flashOk}"><div class="alert ok"><c:out value="${sessionScope.flashOk}"/></div><c:remove var="flashOk" scope="session"/></c:if>
    <c:if test="${not empty sessionScope.flashErr}"><div class="alert err"><c:out value="${sessionScope.flashErr}"/></div><c:remove var="flashErr" scope="session"/></c:if>
    <c:if test="${not empty error}"><div class="alert err"><c:out value="${error}"/></div></c:if>
  </div>
