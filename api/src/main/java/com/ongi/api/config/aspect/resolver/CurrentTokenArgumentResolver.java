package com.ongi.api.config.aspect.resolver;

import com.ongi.api.config.aspect.CurrentToken;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@RequiredArgsConstructor
@Component
public class CurrentTokenArgumentResolver implements HandlerMethodArgumentResolver {

	//private final JwtTokenProvider jwtTokenProvider;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(CurrentToken.class)
			&& parameter.getParameterType().equals(String.class);
	}

	// TODO Token Decrypt
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
//		HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
//		return jwtTokenProvider.resolveToken(request);
		return null;
	}

}
