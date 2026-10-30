<#import "/spring.ftl" as spring>
<#import "../layout.ftlh" as layout>

<@layout.page title="Substations" active="substations">
    <#-- Attention filter and search share one form, so both are always sent together.
         Without JavaScript the form still works as a plain GET. -->
    <form role="search" action="<@spring.url '/substations'/>" method="get"
          class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3"
          hx-get="<@spring.url '/substations'/>" hx-trigger="input delay:300ms, search, submit"
          hx-target="#substations" hx-swap="outerHTML" hx-push-url="true">
        <div class="btn-group btn-group-sm" role="group" aria-label="Filter substations">
            <input type="radio" class="btn-check" name="attention" id="attention-all" value=""
                   autocomplete="off"<#if !attention> checked</#if>>
            <label class="btn btn-outline-secondary" for="attention-all">All</label>
            <input type="radio" class="btn-check" name="attention" id="attention-only" value="true"
                   autocomplete="off"<#if attention> checked</#if>>
            <label class="btn btn-outline-secondary" for="attention-only">Needs attention</label>
        </div>

        <div class="d-flex flex-wrap align-items-center gap-2">
            <div class="input-group input-group-sm search-field">
                <span class="input-group-text"><i class="bi bi-search" aria-hidden="true"></i></span>
                <input type="search" name="q" value="${q}" maxlength="50" class="form-control"
                       placeholder="Search by acronym or name" aria-label="Search by acronym or name"
                       autocomplete="off">
            </div>
            <#if isAdmin!false>
                <a href="<@spring.url '/substations/new'/>" class="btn btn-sm btn-primary text-nowrap">
                    <i class="bi bi-plus-lg" aria-hidden="true"></i> New substation
                </a>
            </#if>
        </div>
    </form>

    <div class="card">
        <#include "_table.ftlh">
    </div>
</@layout.page>
