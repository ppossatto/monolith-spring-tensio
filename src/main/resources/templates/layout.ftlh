<#import "/spring.ftl" as spring>

<#macro navItem href icon label key current>
  <li class="nav-item">
    <a class="nav-link<#if key == current> active</#if>" href="<@spring.url href/>">
      <i class="bi ${icon}"></i>
      <span class="label">${label}</span>
    </a>
  </li>
</#macro>

<#macro page title active="">
  <!DOCTYPE html>
  <html lang="en">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${title} · Tensio</title>
    <link rel="stylesheet" href="<@spring.url '/webjars/bootstrap/5.3.8/dist/css/bootstrap.min.css'/>">
    <link rel="stylesheet" href="<@spring.url '/webjars/bootstrap-icons/1.13.1/font/bootstrap-icons.min.css'/>">
    <link rel="stylesheet" href="<@spring.url '/css/styles.css'/>">
    <script src="<@spring.url '/webjars/bootstrap/5.3.8/dist/js/bootstrap.bundle.min.js'/>" defer></script>
    <script src="<@spring.url '/webjars/htmx.org/2.0.11/dist/htmx.min.js'/>" defer></script>
    <script src="<@spring.url '/webjars/chart.js/4.5.1/dist/chart.umd.js'/>" defer></script>
    <script src="<@spring.url '/js/dashboard.js'/>" defer></script>
  </head>
  <body <#if _csrf??>hx-headers='{"${_csrf.headerName}": "${_csrf.token}"}'</#if>>

  <#if userLogged??>
    <aside class="sidebar">
      <a class="sidebar-brand" href="<@spring.url '/'/>">
        <i class="bi bi-activity"></i>
        <span class="label">Tensio</span>
      </a>
      <ul class="nav flex-column">
        <@navItem href="/" icon="bi-speedometer2" label="Dashboard" key="dashboard" current=active/>
        <@navItem href="/transformers" icon="bi-lightning-charge" label="Transformers" key="transformers" current=active/>
        <@navItem href="/substations" icon="bi-building" label="Substations" key="substations" current=active/>
        <@navItem href="/analyses" icon="bi-clipboard-data" label="Analyses" key="analyses" current=active/>
        <@navItem href="/users" icon="bi-person" label="Users" key="users" current=active/>
      </ul>
    </aside>
  </#if>

  <div class="app-content<#if userLogged??> with-sidebar</#if>">
    <nav class="navbar bg-body-tertiary px-3">
      <span class="navbar-brand mb-0 h1">${title}</span>
      <#if userLogged?? && _csrf??>
        <form action="<@spring.url '/logout'/>" method="post" class="d-flex gap-2 align-items-center">
          <span>${userLogged}</span>
          <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
          <button class="btn btn-sm btn-outline-secondary">Exit</button>
        </form>
      </#if>
    </nav>
    <main class="container-fluid py-4">
      <#nested>
    </main>
  </div>
  </body>
  </html>
</#macro>