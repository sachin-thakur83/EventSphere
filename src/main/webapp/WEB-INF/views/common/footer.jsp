<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
</main>
<footer class="footer">
  <div class="container foot-inner">
    <div><strong><i class="fa-solid fa-circle-nodes"></i> EventSphere</strong><br><small>College project - payments are simulated (demo only).</small></div>
    <div class="foot-links"><a href="${ctx}/about">About</a><a href="${ctx}/contact">Contact</a><a href="${ctx}/terms">Terms</a><a href="${ctx}/privacy">Privacy</a></div>
  </div>
</footer>
<c:if test="${needCharts}"><script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script></c:if>
<script src="${ctx}/js/app.js"></script>
</body>
</html>
