<#import "/spring.ftl" as spring>
<#import "/_pagination.ftlh" as pagination>
<#assign badge = {"OK": "success", "ALERT": "warning", "DANGER": "danger"}>
<#assign filterQuery = "">
<#if health??><#assign filterQuery = filterQuery + "&health=" + health.name()></#if>
<#if q?has_content><#assign filterQuery = filterQuery + "&q=" + q?url("UTF-8")></#if>
<#if from?has_content><#assign filterQuery = filterQuery + "&from=" + from></#if>
<#if to?has_content><#assign filterQuery = filterQuery + "&to=" + to></#if>

<div id="analyses">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead>
            <tr><th>Sample date</th><th>Transformer</th><th>Substation</th><th>Fault</th><th>Health</th></tr>
            </thead>
            <tbody>
            <#list analyses.content as a>
                <tr data-href="<@spring.url '/analyses/${a.id()?c}'/>">
                    <td class="text-nowrap"><a href="<@spring.url '/analyses/${a.id()?c}'/>">${a.sampleDate()}</a></td>
                    <td><a href="<@spring.url '/transformers/${a.transformerId()?c}'/>">${a.transformerTag()}</a></td>
                    <td>${a.substation()}</td>
                    <td><#if a.finalFault()??>${a.finalFault().label}<#else>—</#if></td>
                    <td>
                        <#if a.healthStatus()??>
                            <#assign hs = a.healthStatus().name()>
                            <span class="badge text-bg-${badge[hs]}">${hs}</span>
                        <#else>
                            <span class="text-muted">—</span>
                        </#if>
                    </td>
                </tr>
            <#else>
                <tr><td colspan="5" class="text-center text-muted">No analyses found.</td></tr>
            </#list>
            </tbody>
        </table>
    </div>

    <@pagination.pager page=analyses path="/analyses" query=filterQuery target="analyses"/>
</div>
