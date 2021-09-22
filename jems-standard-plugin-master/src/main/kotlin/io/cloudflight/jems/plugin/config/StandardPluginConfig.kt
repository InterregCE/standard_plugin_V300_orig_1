package io.cloudflight.jems.plugin.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackages = ["io.cloudflight.jems.plugin"])
open class StandardPluginConfig
