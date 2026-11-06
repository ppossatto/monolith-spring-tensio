<#import "/spring.ftl" as spring>
<#import "../layout.ftlh" as layout>

<#assign u = user>

<#macro field id label value="" col="col-md-4">
    <div class="${col}">
        <label for="${id}" class="form-label small text-muted mb-1">${label}</label>
        <input id="${id}" class="form-control" value="${value}" readonly>
    </div>
</#macro>

<@layout.page title="User ${u.username()}" active="users">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <a href="<@spring.url '/users'/>" class="d-inline-flex align-items-center gap-1 small">
            <i class="bi bi-arrow-left" aria-hidden="true"></i> Back to users
        </a>
        <a href="<@spring.url '/users/${u.id()?c}/edit'/>" class="btn btn-sm btn-outline-primary">
            <i class="bi bi-pencil" aria-hidden="true"></i> Edit
        </a>
    </div>

    <form class="d-flex flex-column gap-4" aria-label="User ${u.username()}">
        <section class="card">
            <div class="card-header"><h2 class="h6 mb-0">Account</h2></div>
            <div class="card-body row g-3">
                <@field id="username" label="E-mail" value=u.username() col="col-md-6"/>
                <@field id="role" label="Role" value=u.role().name() col="col-md-3"/>
                <@field id="status" label="Status" value=u.active()?then("Active", "Inactive") col="col-md-3"/>
                <@field id="created-at" label="Created" value=u.createdAt()!"" col="col-md-6"/>
                <@field id="updated-at" label="Last change" value=u.updatedAt()!"" col="col-md-6"/>
            </div>
        </section>
    </form>
</@layout.page>
