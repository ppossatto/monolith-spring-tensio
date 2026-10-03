<#import "/spring.ftl" as spring>
<#import "../layout.ftlh" as layout>

<#macro card title value color>
    <div class="col-md-4">
        <div class="card text-bg-${color} h-100">
            <div class="card-body">
                <h6 class="card-title text-uppercase mb-1">${title}</h6>
                <p class="display-6 mb-0">${value}</p>
            </div>
        </div>
    </div>
</#macro>

<@layout.page title="Dashboard" active="dashboard">
    <div class="row g-3 mb-4">
        <@card "Healthy" summary.ok() "success"/>
        <@card "Alert" summary.alert() "warning"/>
        <@card "Danger" summary.danger() "danger"/>
    </div>

    <div class="row g-4">
        <div class="col-lg-7">
            <div class="card">
                <div class="card-header">Transformers by priority</div>
                <#include "_transformers.ftlh">
            </div>
        </div>

        <div class="col-lg-5">
            <div class="card">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <span>Gas profile</span>
                    <#if radarOptions?has_content>
                        <select name="transformerId" class="form-select form-select-sm w-auto"
                                hx-get="<@spring.url '/dashboard/radar'/>" hx-target="#radar" hx-swap="outerHTML">
                            <#list radarOptions as o>
                                <option value="${o.id()?c}">${o.tag()}</option>
                            </#list>
                        </select>
                    </#if>
                </div>
                <#if radar??>
                    <#include "_radar.ftlh">
                <#else>
                    <p class="card-body text-muted mb-0">No analyses registered yet.</p>
                </#if>
            </div>
        </div>
    </div>
</@layout.page>