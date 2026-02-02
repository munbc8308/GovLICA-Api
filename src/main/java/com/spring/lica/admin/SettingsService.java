package com.spring.lica.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SettingsService {

	private final Path filePath;

	public SettingsService(AdminProperties props) {
		this.filePath = Path.of(props.getSettingsFile());
		log.info("Settings file: {}", this.filePath.toAbsolutePath());
	}

	public List<PropertySection> loadSections() throws IOException {
		if (!Files.exists(filePath)) {
			throw new IOException("Settings file not found: " + filePath.toAbsolutePath());
		}

		List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
		List<PropertySection> sections = new ArrayList<>();
		PropertySection currentSection = null;

		for (String line : lines) {
			String trimmed = line.trim();

			if (trimmed.isEmpty()) {
				continue;
			}

			if (trimmed.startsWith("#") && !trimmed.contains("=")) {
				String sectionName = trimmed.substring(1).trim();
				if (!sectionName.isEmpty()) {
					currentSection = new PropertySection(sectionName);
					sections.add(currentSection);
				}
				continue;
			}

			if (trimmed.startsWith("#")) {
				continue;
			}

			int eqIndex = trimmed.indexOf('=');
			if (eqIndex > 0) {
				String key = trimmed.substring(0, eqIndex).trim();
				String value = trimmed.substring(eqIndex + 1);
				if (currentSection == null) {
					currentSection = new PropertySection("General");
					sections.add(currentSection);
				}
				currentSection.getProperties().add(new PropertyEntry(key, value));
			}
		}

		return sections;
	}

	public void saveProperties(Map<String, String> updatedProperties) throws IOException {
		List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
		List<String> newLines = new ArrayList<>();

		for (String line : lines) {
			String trimmed = line.trim();
			if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
				int eqIndex = trimmed.indexOf('=');
				if (eqIndex > 0) {
					String key = trimmed.substring(0, eqIndex).trim();
					if (updatedProperties.containsKey(key)) {
						newLines.add(key + "=" + updatedProperties.get(key));
						continue;
					}
				}
			}
			newLines.add(line);
		}

		Files.write(filePath, newLines, StandardCharsets.UTF_8);
		log.info("Settings saved to {}", filePath.toAbsolutePath());
	}
}
