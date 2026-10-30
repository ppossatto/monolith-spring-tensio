<#import "/spring.ftl" as spring>
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
                <tr>
                    <td><a href="<@spring.url '/substations/${s.id()?c}'/>">${s.acronym()}</a></td>
                    <td>${s.name()!'—'}</td>
                    <td><#if s.city()??>${s.city()}<#if s.state()??> / ${s.state()}</#if><#else>—</#if></td>
                    <td class="text-end">${s.transformerCount()}</td>
                    <td>
                        <#if s.active()>
                            <span class="badge text-bg-light border">Active</span>
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

    <#if substations.totalPages gt 1>
        <nav class="d-flex justify-content-between align-items-center p-2">
            <button class="btn btn-sm btn-outline-secondary" <#if !substations.hasPrevious()>disabled</#if>
                    hx-get="<@spring.url '/substations?page=${(substations.number - 1)?c}${filterQuery}'/>"
                    hx-target="#substations" hx-swap="outerHTML">Previous</button>
            <span class="small">${substations.number + 1} / ${substations.totalPages}</span>
            <button class="btn btn-sm btn-outline-secondary" <#if !substations.hasNext()>disabled</#if>
                    hx-get="<@spring.url '/substations?page=${(substations.number + 1)?c}${filterQuery}'/>"
                    hx-target="#substations" hx-swap="outerHTML">Next</button>
        </nav>
    </#if>
</div>
