<#import "/spring.ftl" as spring>
<#import "../layout.ftlh" as layout>

<#assign filters = [
    {"id": "all", "label": "All", "value": ""},
    {"id": "admin", "label": "Admins", "value": "ADMIN"},
    {"id": "user", "label": "Users", "value": "USER"}
]>
<#assign current = role???then(role.name(), "")>

<@layout.page title="Users" active="users">
    <#-- Role filter and search share one form, so both are always sent together.
         Without JavaScript the form still works as a plain GET. -->
    <form role="search" action="<@spring.url '/users'/>" method="get"
          class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3"
          hx-get="<@spring.url '/users'/>" hx-trigger="input delay:300ms, search, submit"
          hx-target="#users" hx-swap="outerHTML" hx-push-url="true">
        <div class="btn-group btn-group-sm" role="group" aria-label="Filter by role">
            <#list filters as f>
                <input type="radio" class="btn-check" name="role" id="role-${f.id}" value="${f.value}"
                       autocomplete="off"<#if f.value == current> checked</#if>>
                <label class="btn btn-outline-secondary" for="role-${f.id}">${f.label}</label>
            </#list>
        </div>

        <div class="input-group input-group-sm search-field">
            <span class="input-group-text"><i class="bi bi-search" aria-hidden="true"></i></span>
            <input type="search" name="q" value="${q}" maxlength="50" class="form-control"
                   placeholder="Search by e-mail" aria-label="Search by e-mail" autocomplete="off">
        </div>
    </form>

    <div class="card">
        <#include "_table.ftlh">
    </div>
</@layout.page>
