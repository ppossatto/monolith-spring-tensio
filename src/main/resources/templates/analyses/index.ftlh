<#import "/spring.ftl" as spring>
<#import "../layout.ftlh" as layout>

<#assign filters = [
    {"id": "all", "label": "All", "value": ""},
    {"id": "ok", "label": "Healthy", "value": "OK"},
    {"id": "alert", "label": "Alert", "value": "ALERT"},
    {"id": "danger", "label": "Danger", "value": "DANGER"},
    {"id": "pending", "label": "Pending", "value": "PENDING"}
]>
<#assign current = state???then(state.name(), "")>

<@layout.page title="Analyses" active="analyses">
    <#-- Diagnosis filter, period and tag search share one form, so they are always sent together.
         Without JavaScript the form still works as a plain GET. -->
    <form role="search" action="<@spring.url '/analyses'/>" method="get"
          class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3"
          hx-get="<@spring.url '/analyses'/>" hx-trigger="input delay:300ms, change, search, submit"
          hx-target="#analyses" hx-swap="outerHTML" hx-push-url="true">
        <div class="btn-group btn-group-sm" role="group" aria-label="Filter by diagnosis">
            <#list filters as f>
                <input type="radio" class="btn-check" name="state" id="state-${f.id}" value="${f.value}"
                       autocomplete="off"<#if f.value == current> checked</#if>>
                <label class="btn btn-outline-secondary" for="state-${f.id}">${f.label}</label>
            </#list>
        </div>

        <div class="d-flex flex-wrap align-items-center gap-2">
            <div class="input-group input-group-sm w-auto">
                <label class="input-group-text" for="from">From</label>
                <input type="date" id="from" name="from" class="form-control" value="${from}">
            </div>
            <div class="input-group input-group-sm w-auto">
                <label class="input-group-text" for="to">To</label>
                <input type="date" id="to" name="to" class="form-control" value="${to}">
            </div>
            <div class="input-group input-group-sm search-field">
                <span class="input-group-text"><i class="bi bi-search" aria-hidden="true"></i></span>
                <input type="search" name="q" value="${q}" maxlength="50" class="form-control"
                       placeholder="Search by transformer tag" aria-label="Search by transformer tag"
                       autocomplete="off">
            </div>
        </div>
    </form>

    <div class="card">
        <#include "_table.ftlh">
    </div>
</@layout.page>
