<div id="radar" class="card-body">
    <canvas data-radar
            data-labels="<#list radar.labels() as l>${l}<#sep>,</#list>"
            data-latest="<#list radar.latest() as v><#if v??>${v?c}<#else>null</#if><#sep>,</#list>"
            data-average="<#list radar.average() as v><#if v??>${v?c}<#else>null</#if><#sep>,</#list>">
    </canvas>
    <p class="small text-muted mt-2 mb-0">
        Values as % of the alert limit · Latest sample: ${radar.latestSampleDate()!'—'}
    </p>
</div>