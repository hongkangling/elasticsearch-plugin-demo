package org.elasticsearch.plugin.analysis.config;

import lombok.Data;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;

@Data
public class EnvInfo {
    private Environment environment;
    private Settings settings;

    @Inject
    public EnvInfo(Environment env,Settings settings) {
        this.environment = env;
        this.settings=settings;
        Config.initialize(this);
    }
}
