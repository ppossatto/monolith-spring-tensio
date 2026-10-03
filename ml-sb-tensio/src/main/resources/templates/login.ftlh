<#import "/spring.ftl" as spring>
<#import "layout.ftlh" as layout>

<@layout.page title="Sign In">
    <form action="<@spring.url '/login'/>" method="post" class="mx-auto" style="max-width: 360px">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <#if error><div class="alert alert-danger">Invalid credentials</div></#if>
        <#if exited><div class="alert alert-info">Session ended</div></#if>
        <label>
            <input name="username" class="form-control mb-2" placeholder="Email">
        </label>
        <label>
            <input name="password" type="password" class="form-control mb-2" placeholder="Password">
        </label>
        <button class="btn btn-primary w-100">Sign In</button>
    </form>
</@layout.page>