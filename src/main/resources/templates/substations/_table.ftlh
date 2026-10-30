<#import "/spring.ftl" as spring>
<#import "/_pagination.ftlh" as pagination>
<#assign filterQuery = "">
<#if attention><#assign filterQuery = filterQuery + "&attention=true"></#if>
<#if q?has_content><#assign filterQuery = filterQuery + "&q=" + q?url("UTF-8")></#if>

<div id="substations">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
            <tr>
                <th>Acronym</th><th>Name</th><th>City / State</th>
                <th class="text-end">Transformers</th><th>Status</th><th>Attention</th>
            </tr>
            </thead>
            <tbody>
            <#list substations.content as s>
                <tr data-href="<@spring.url '/substations/${s.id()?c}'/>">
                    <td><a href="<@spring.url '/substations/${s.id()?c}'/>">${s.acronym()}</a></td>
                    <td>${s.name()!'—'}</td>
                    <td><#if s.city()??>${s.city()}<#if s.state()??> / ${s.state()}</#if><#else>—</#if></td>
                    <td class="text-end">${s.transformerCount()}</td>
                    <td>
                        <#if s.active()>
                            <span class="badge bg-body-tertiary text-body border">Active</span>
                        <#else>
                            <span class="badge text-bg-secondary">Inactive</span>
                        </#if>
                    </td>
                    <td>
                        <#if s.dangerCount() gt 0>
                            <span class="badge rounded-pill bg-danger-subtle text-danger-emphasis border border-danger-subtle">
                                <i class="bi bi-exclamation-circle" aria-hidden="true"></i> ${s.dangerCount()} in danger
                            </span>
                        <#else>
                            <span class="text-muted">—</span>
                        </#if>
                    </td>
                </tr>
            <#else>
                <tr><td colspan="6" class="text-center text-muted">No substations found.</td></tr>
            </#list>
            </tbody>
        </table>
    </div>

    <@pagination.pager page=substations path="/substations" query=filterQuery target="substations"/>
</div>
