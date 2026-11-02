package com.poc.hub.core.feature;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lists every feature module that is present in this build of the application. */
@RestController
@RequestMapping("/api/features")
public class FeatureController {

    private final List<FeatureDescriptor> descriptors;
    private final String appVersion;

    public FeatureController(List<FeatureDescriptor> descriptors, @Value("${app.version}") String appVersion) {
        this.descriptors = descriptors.stream()
                .sorted(Comparator.comparingInt(FeatureDescriptor::order))
                .toList();
        this.appVersion = appVersion;
    }

    @GetMapping
    public FeaturesResponse list() {
        List<FeatureView> features = descriptors.stream().map(FeatureView::from).toList();
        return new FeaturesResponse(appVersion, features.size(), features);
    }

    public record FeaturesResponse(String appVersion, int count, List<FeatureView> features) {
    }

    public record FeatureView(String key, String name, String description, String route, String apiPath,
                              int phase, int order, long itemCount) {

        static FeatureView from(FeatureDescriptor d) {
            return new FeatureView(d.key(), d.name(), d.description(), d.route(), d.apiPath(),
                    d.phase(), d.order(), d.itemCount());
        }
    }
}
