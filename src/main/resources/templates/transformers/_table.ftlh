<#import "/spring.ftl" as spring>
<#assign badge = {"OK": "success", "ALERT": "warning", "DANGER": "danger"}>
<#assign filterQuery = "">
<#if health??><#assign filterQuery = filterQuery + "&health=" + health.name()></#if>
<#if q?has_content><#assign filterQuery = filterQuery + "&q=" + q?url("UTF-8")></#if>

<div id="transformers">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
            <tr>
                <th>Tag</th><th>Substation</th><th class="text-end">MVA</th>
                <th class="text-center">Criticality</th><th>Health</th><th>Fault</th><th>Last sample</th>
            </tr>
            </thead>
            <tbody>
            <#list transformers.content as t>
                <tr>
                    <td><a href="<@spring.url '/transformers/${t.id()?c}'/>">${t.tag()}</a></td>
                    <td>${t.substation()}</td>
                    <td class="text-end">${(t.ratedPowerMva()?string["0.##"])!'—'}</td>
                    <td class="text-center">${t.criticality()}</td>
                    <td>
                        <#if t.healthStatus()??>
                            <#assign hs = t.healthStatus().name()>
                            <span class="badge text-bg-${badge[hs]}">${hs}</span>
                        <#else>
                            <span class="text-muted">No data</span>
                        </#if>
                    </td>
                    <td><#if t.finalFault()??>${t.finalFault().label}<#else>—</#if></td>
                    <td>${t.lastSampleDate()!'—'}</td>
                </tr>
            <#else>
                <tr><td colspan="7" class="text-center text-muted">No transformers found.</td></tr>
            </#list>
            </tbody>
        </table>
    </div>

    <#if transformers.totalPages gt 1>
        <nav class="d-flex justify-content-between align-items-center p-2">
            <button class="btn btn-sm btn-outline-secondary" <#if !transformers.hasPrevious()>disabled</#if>
                    hx-get="<@spring.url '/transformers?page=${(transformers.number - 1)?c}${filterQuery}'/>"
                    hx-target="#transformers" hx-swap="outerHTML">Previous</button>
            <span class="small">${transformers.number + 1} / ${transformers.totalPages}</span>
            <button class="btn btn-sm btn-outline-secondary" <#if !transformers.hasNext()>disabled</#if>
                    hx-get="<@spring.url '/transformers?page=${(transformers.number + 1)?c}${filterQuery}'/>"
                    hx-target="#transformers" hx-swap="outerHTML">Next</button>
        </nav>
    </#if>
</div>