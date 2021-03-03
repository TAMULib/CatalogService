package edu.tamu.catalog.resolver;

import static java.lang.String.format;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import edu.tamu.catalog.annotation.DefaultCatalog;
import edu.tamu.catalog.service.CatalogService;

public class CatalogServiceArgumentResolver implements HandlerMethodArgumentResolver {

    private List<CatalogService> catalogServices;

    public CatalogServiceArgumentResolver(List<CatalogService> catalogServices) {
        this.catalogServices = catalogServices;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return CatalogService.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        final HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        String catalogName = request.getParameter("catalogName");
        if (StringUtils.isEmpty(catalogName)) {
            DefaultCatalog defaultCatalog = parameter.getParameterAnnotation(DefaultCatalog.class);
            catalogName = defaultCatalog.value();
        }
        for (CatalogService catalogService : catalogServices) {
            if (catalogService.getName().equalsIgnoreCase(catalogName)) {
                return catalogService;
            }
        }
        throw new RuntimeException(format("Catalog service %s not found!", catalogName));
    }

}
