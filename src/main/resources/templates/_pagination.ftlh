<#import "/spring.ftl" as spring>

<#--
  Bootstrap pagination for a Spring Data Page, loaded with HTMX.
  page:   the org.springframework.data.domain.Page
  path:   list URL without query string, e.g. "/transformers"
  query:  extra query string appended after the page number, starting with "&" (or "")
  target: id of the element replaced by the response, without "#"
  Links keep a real href, so they also work without JavaScript.
-->
<#macro pager page path query target window=2>
    <#if page.totalPages gt 1>
        <#assign current = page.number>
        <#assign last = page.totalPages - 1>
        <#assign from = [current - window, 0]?max>
        <#assign to = [current + window, last]?min>
        <#assign firstRow = current * page.size + 1>
        <#assign lastRow = current * page.size + page.numberOfElements>

        <nav class="d-flex flex-wrap justify-content-between align-items-center gap-2 p-2" aria-label="Pagination">
            <span class="small text-muted">
                Showing ${firstRow?c}–${lastRow?c} of ${page.totalElements?c}
            </span>
            <ul class="pagination pagination-sm mb-0">
                <@item path=path query=query target=target number=(current - 1) label="Previous"
                       disabled=!page.hasPrevious() ariaLabel="Previous page"/>
                <#if from gt 0>
                    <@item path=path query=query target=target number=0 label="1"/>
                    <#if from gt 1><li class="page-item disabled"><span class="page-link">…</span></li></#if>
                </#if>
                <#list from..to as n>
                    <@item path=path query=query target=target number=n label=(n + 1)?c active=(n == current)/>
                </#list>
                <#if to lt last>
                    <#if to lt last - 1><li class="page-item disabled"><span class="page-link">…</span></li></#if>
                    <@item path=path query=query target=target number=last label=(last + 1)?c/>
                </#if>
                <@item path=path query=query target=target number=(current + 1) label="Next"
                       disabled=!page.hasNext() ariaLabel="Next page"/>
            </ul>
        </nav>
    </#if>
</#macro>

<#macro item path query target number label disabled=false active=false ariaLabel="">
    <#if disabled>
        <li class="page-item disabled"><span class="page-link">${label}</span></li>
    <#elseif active>
        <li class="page-item active" aria-current="page"><span class="page-link">${label}</span></li>
    <#else>
        <#assign url><@spring.url path + "?page=" + number?c + query/></#assign>
        <li class="page-item">
            <a class="page-link" href="${url}" hx-get="${url}" hx-target="#${target}" hx-swap="outerHTML"
               hx-push-url="true"<#if ariaLabel?has_content> aria-label="${ariaLabel}"</#if>>${label}</a>
        </li>
    </#if>
</#macro>
