package com.orrien.expense_tracker.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Injects the logged-in user's id into a controller parameter: list(@CurrentUserId Long userId).
// It's a "meta-annotation" over Spring Security's @AuthenticationPrincipal: the principal is the
// verified Jwt, and the SpEL expression turns its "sub" claim (a String) into a Long.
// The id always comes from the token, never from the URL, so nobody can act as another user.
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal(expression = "T(java.lang.Long).valueOf(subject)")
public @interface CurrentUserId {
}
