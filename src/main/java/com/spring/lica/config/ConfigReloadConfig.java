package com.spring.lica.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * 설정 파일 변경 감지 및 자동 리로드 모듈.
 *
 * <p>{@code @RefreshScope}가 선언된 빈은 설정 변경 시 자동으로 재생성됩니다.
 * {@code @ConfigurationProperties} 빈의 속성도 자동으로 리바인딩됩니다.</p>
 *
 * <p>watch-path를 설정하면 파일 변경을 주기적으로 감지하여 자동 리프레시합니다.
 * watch-path가 없으면 {@code POST /actuator/refresh} 엔드포인트로 수동 트리거할 수 있습니다.</p>
 */
@Configuration
@ConditionalOnProperty(name = "app.module.config-reload.enabled", havingValue = "true")
@EnableScheduling
public class ConfigReloadConfig {

	private static final Logger log = LoggerFactory.getLogger(ConfigReloadConfig.class);

	private final ContextRefresher contextRefresher;
	private final Path watchPath;
	private long lastModified;

	public ConfigReloadConfig(
			ContextRefresher contextRefresher,
			@Value("${app.module.config-reload.watch-path:}") String path) {
		this.contextRefresher = contextRefresher;

		if (StringUtils.hasText(path)) {
			this.watchPath = Paths.get(path);
			this.lastModified = getLastModified();
			log.info("Config Reload module enabled - watching: {}", this.watchPath.toAbsolutePath());
		} else {
			this.watchPath = null;
			log.info("Config Reload module enabled - use POST /actuator/refresh to trigger");
		}
	}

	@Scheduled(fixedDelayString = "${app.module.config-reload.interval:5000}")
	public void checkForChanges() {
		if (watchPath == null || !Files.exists(watchPath)) {
			return;
		}

		long currentModified = getLastModified();
		if (lastModified > 0 && currentModified > lastModified) {
			log.info("Config file changed, refreshing context...");
			Set<String> keys = contextRefresher.refresh();
			if (!keys.isEmpty()) {
				log.info("Refreshed properties: {}", keys);
			}
		}
		lastModified = currentModified;
	}

	private long getLastModified() {
		try {
			return Files.getLastModifiedTime(watchPath).toMillis();
		} catch (IOException e) {
			return 0;
		}
	}
}
