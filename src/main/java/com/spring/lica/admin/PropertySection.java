package com.spring.lica.admin;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PropertySection {
	private final String name;
	private final List<PropertyEntry> properties = new ArrayList<>();

	public PropertySection(String name) {
		this.name = name;
	}
}
