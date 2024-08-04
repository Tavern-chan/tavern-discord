package com.tavern.domain.model;

import com.tavern.utilities.StringUtils;

import java.util.Objects;

/**
 * Common class for a string based object identifier
 */
public abstract class Identifier {
	private String id;

	protected Identifier() { }

	protected Identifier(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Identifier that = (Identifier) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	@Override
	public String toString() {
		return getId();
	}

	public String getId() {
		return id;
	}

	private void setId(String id) {
		if (StringUtils.isNullOrBlank(id)) {
			throw new IllegalArgumentException("Identifier cannot be null or blank");
		}
		this.id = id;
	}

}
