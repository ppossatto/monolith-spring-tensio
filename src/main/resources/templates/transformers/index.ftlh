<#import "/spring.ftl" as spring>
<#import "../layout.ftlh" as layout>

<#assign filters = [
    {"label": "All", "value": ""},
    {"label": "Healthy", "value": "OK"},
    {"label": "Alert", "value": "ALERT"},
    {"label": "Danger", "value": "DANGER"}
]>
<#assign current = health???then(health.name(), "")>

<@layout.page title="Transformers" active="transformers">
    <nav class="mb-3" aria-label="Filter by health">
        <div class="btn-group btn-group-sm">
            <#list filters as f>
                <#assign href = f.value?has_content?then("/transformers?health=" + f.value, "/transformers")>
                <a href="<@spring.url href/>"
                   class="btn btn-outline-secondary<#if f.value == current> active</#if>"
                   <#if f.value == current>aria-current="page"</#if>>${f.label}</a>
            </#list>
        </div>
    </nav>

    <div class="card">
        <#include "_table.ftlh">
    </div>
</@layout.page>
