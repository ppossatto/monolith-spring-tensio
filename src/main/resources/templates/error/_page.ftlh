<#import "/spring.ftl" as spring>
<#import "../layout.ftlh" as layout>

<#--
  Error page shared by error.ftlh, 404.ftlh and 5xx.ftlh. Rendered by Spring Boot's error controller with the
  default error attributes (status, error, path, timestamp); exception details and messages are never shown.
  icon:   Bootstrap Icons class
  retry:  shows a "Try again" link that opens the failed path again (GET)
-->
<#macro page heading text icon="bi-exclamation-triangle" retry=false>
    <#assign code = (status?c)!"">
    <@layout.page title=heading active="">
        <div class="d-flex flex-column align-items-center justify-content-center text-center py-5 mx-auto"
             style="max-width: 32rem; min-height: 60vh;">
            <i class="bi ${icon} text-primary opacity-75" style="font-size: 4rem;" aria-hidden="true"></i>
            <#if code?has_content>
                <p class="display-4 fw-semibold text-body-secondary mb-0">${code}</p>
            </#if>
            <h1 class="h3 mt-2">${heading}</h1>
            <p class="text-muted mb-4">${text}</p>
            <div class="d-flex flex-wrap justify-content-center gap-2">
                <#if retry && path?? && path?has_content>
                    <#-- getContextUrl instead of spring.url: the path comes from the request and must be HTML-escaped -->
                    <a href="${springMacroRequestContext.getContextUrl(path)}" class="btn btn-outline-primary">
                        <i class="bi bi-arrow-clockwise" aria-hidden="true"></i> Try again
                    </a>
                </#if>
                <a href="<@spring.url '/'/>" class="btn btn-primary">
                    <i class="bi bi-house" aria-hidden="true"></i> Back to dashboard
                </a>
            </div>
            <#if path?? && path?has_content>
                <p class="small text-muted mt-4 mb-0">
                    <code>${path}</code><#if timestamp??> · ${timestamp?string("yyyy-MM-dd HH:mm:ss")}</#if>
                </p>
            </#if>
        </div>
    </@layout.page>
</#macro>
