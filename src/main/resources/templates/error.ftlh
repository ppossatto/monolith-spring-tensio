<#import "error/_page.ftlh" as e>
<#-- Any other error status (400, 403, 405, ...): Spring Boot falls back to the "error" view -->
<#assign denied = (status!0) == 403>
<@e.page heading=denied?then("Access denied", (error!"Request error")) icon=denied?then("bi-shield-lock", "bi-exclamation-triangle")
    text=denied?then("You do not have permission to open this page.", "The request could not be processed. Check the address or go back to the dashboard.")/>
