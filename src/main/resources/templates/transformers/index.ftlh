<#import "/spring.ftl" as spring>
<#import "../layout.ftlh" as layout>

<#assign filters = [
    {"id": "all", "label": "All", "value": ""},
    {"id": "ok", "label": "Healthy", "value": "OK"},
    {"id": "alert", "label": "Alert", "value": "ALERT"},
    {"id": "danger", "label": "Danger", "value": "DANGER"}
]>
<#assign current = health???then(health.name(), "")>

<@layout.page title="Transformers" active="transformers">
    <#-- Health filter and tag search share one form, so both are always sent together.
         Without JavaScript the form still works as a plain GET. -->
    <form role="search" action="<@spring.url '/transformers'/>" method="get"
          class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3"
          hx-get="<@spring.url '/transformers'/>" hx-trigger="input delay:300ms, search, submit"
          hx-target="#transformers" hx-swap="outerHTML" hx-push-url="true">
        <div class="btn-group btn-group-sm" role="group" aria-label="Filter by health">
            <#list filters as f>
                <input type="radio" class="btn-check" name="health" id="health-${f.id}" value="${f.value}"
                       autocomplete="off"<#if f.value == current> checked</#if>>
                <label class="btn btn-outline-secondary" for="health-${f.id}">${f.label}</label>
            </#list>
        </div>

        <div class="input-group input-group-sm search-field">
            <span class="input-group-text"><i class="bi bi-search" aria-hidden="true"></i></span>
            <input type="search" name="q" value="${q}" maxlength="50" class="form-control"
                   placeholder="Search by tag" aria-label="Search by tag" autocomplete="off">
        </div>
    </form>

    <div class="card">
        <#include "_table.ftlh">
    </div>
</@layout.page>
