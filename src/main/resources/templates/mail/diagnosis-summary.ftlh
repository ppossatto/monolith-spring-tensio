<#-- E-mail summary of diagnosed analyses.
     Model: event (AnalysesDiagnosedEvent), results (worst health first), baseUrl, danger, alert, ok. -->
<#assign colors = {"OK": "#198754", "ALERT": "#b58105", "DANGER": "#dc3545"}>
<!DOCTYPE html>
<html lang="en">
<body style="font-family: Arial, Helvetica, sans-serif; color: #212529; margin: 0; padding: 24px;">
<h2 style="margin: 0 0 4px;">Tensio · diagnosis summary</h2>
<p style="margin: 0 0 16px; color: #6c757d;">Source: ${event.source()}</p>

<p style="margin: 0 0 16px;">
    <strong>${event.results()?size}</strong> analyses stored and diagnosed:
    <span style="color: ${colors.DANGER};"><strong>${danger}</strong> DANGER</span>,
    <span style="color: ${colors.ALERT};"><strong>${alert}</strong> ALERT</span>,
    <span style="color: ${colors.OK};"><strong>${ok}</strong> OK</span>.
</p>

<#if event.results()?has_content>
    <table cellpadding="6" cellspacing="0" style="border-collapse: collapse; font-size: 14px;">
        <thead>
        <tr style="background: #f1f3f5; text-align: left;">
            <th>Transformer</th><th>Sample date</th><th>Final fault</th><th>Health</th>
        </tr>
        </thead>
        <tbody>
        <#-- At most 200 lines -->
        <#list results as r>
            <#if r?index == 200><tr><td colspan="4">… and ${results?size - 200} more</td></tr><#break></#if>
            <#assign hs = r.diagnosis().healthStatus().name()>
            <tr style="border-top: 1px solid #dee2e6;">
                <td><a href="${baseUrl}/analyses/${r.analysisId()?c}">${r.transformerTag()}</a></td>
                <td>${r.sampleDate()}</td>
                <td>${r.diagnosis().finalFault().label}</td>
                <td style="color: ${colors[hs]}; font-weight: bold;">${hs}</td>
            </tr>
        </#list>
        </tbody>
    </table>
</#if>

<#if event.errors()?has_content>
    <h3 style="margin: 24px 0 8px; color: ${colors.DANGER};">${event.errors()?size} lines not imported</h3>
    <ul style="margin: 0; padding-left: 20px; font-size: 14px;">
        <#list event.errors() as error><li>${error}</li></#list>
    </ul>
</#if>
</body>
</html>
