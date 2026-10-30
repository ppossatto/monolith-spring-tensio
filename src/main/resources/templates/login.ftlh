<#import "/spring.ftl" as spring>
<#import "layout.ftlh" as layout>

<@layout.page title="Sign In" navbar=false bodyClass="login-page" mainClass="">
    <div class="login-wrapper">
        <@layout.themeToggle class="login-theme-toggle"/>
        <form action="<@spring.url '/login'/>" method="post" class="login-card">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

            <#if error><div class="login-alert login-alert-error" role="alert">Invalid email or password.</div></#if>
            <#if exited><div class="login-alert login-alert-info" role="status">You have been signed out.</div></#if>

            <label for="email" class="visually-hidden">Email</label>
            <input id="email" name="email" type="email" class="login-input" placeholder="Email *"
                   autocomplete="username" required autofocus>

            <div class="login-password">
                <label for="password" class="visually-hidden">Password</label>
                <input id="password" name="password" type="password" class="login-input" placeholder="Password *"
                       autocomplete="current-password" required>
                <button type="button" class="login-password-toggle" data-password-toggle="password"
                        aria-controls="password" aria-pressed="false" aria-label="Show password">
                    <i class="bi bi-eye" aria-hidden="true"></i>
                </button>
            </div>

            <div class="login-actions">
                <button type="submit" class="login-button">LOGIN</button>
            </div>
        </form>
    </div>
    <script src="<@spring.url '/js/login.js'/>" defer></script>
</@layout.page>
