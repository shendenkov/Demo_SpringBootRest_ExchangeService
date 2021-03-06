package com.example.exchange.config;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import springfox.documentation.service.Tags;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spring.web.readers.operation.DefaultTagsProvider;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import java.util.Set;

@Component
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER + 1)
public class DisableAutoGeneratedTagsPlugin implements OperationBuilderPlugin {

    private final DefaultTagsProvider tagsProvider;

    @Autowired
    public DisableAutoGeneratedTagsPlugin(DefaultTagsProvider tagsProvider) {
        this.tagsProvider = tagsProvider;
    }

    @Override
    public void apply(OperationContext context) {
        Set<String> defaultTags = tagsProvider.tags(context);
        Set<String> tagsOnController = controllerTags(context);
        Set<String> tagsOnOperation = operationTags(context);

        Sets.SetView<String> intersectionTags = Sets.intersection(tagsOnController, tagsOnOperation);
        Sets.SetView<String> allTags = Sets.union(tagsOnController, tagsOnOperation);

        if (allTags.isEmpty()) {
            context.operationBuilder().tags(defaultTags);
        } else if (intersectionTags.isEmpty()) {
            context.operationBuilder().tags(allTags);
        } else {
            context.operationBuilder().tags(tagsOnOperation);
        }
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }

    private Set<String> controllerTags(OperationContext context) {
        Optional<Api> controllerAnnotation = context.findControllerAnnotation(Api.class);
        return controllerAnnotation.transform(tagsFromController()).or(Sets.<String>newHashSet());
    }

    private Set<String> operationTags(OperationContext context) {
        Optional<ApiOperation> annotation = context.findAnnotation(ApiOperation.class);
        return annotation.transform(tagsFromOperation()).or(Sets.<String>newHashSet());
    }

    private Function<ApiOperation, Set<String>> tagsFromOperation() {
        return new Function<ApiOperation, Set<String>>() {
            @Override
            public Set<String> apply(ApiOperation input) {
                Set<String> tags = Sets.newTreeSet();
                tags.addAll(FluentIterable.from(Lists.newArrayList(input.tags())).filter(Tags.emptyTags()).toSet());
                return tags;
            }
        };
    }

    private Function<Api, Set<String>> tagsFromController() {
        return new Function<Api, Set<String>>() {
            @Override
            public Set<String> apply(Api input) {
                Set<String> tags = Sets.newTreeSet();
                tags.addAll(FluentIterable.from(Lists.newArrayList(input.tags())).filter(Tags.emptyTags()).toSet());
                return tags;
            }
        };
    }
}
