package com.tavern.domain.model;

import java.util.Objects;

public class IdentifiedDomainObject<Id extends Identifier> {
	private Id id;

	public IdentifiedDomainObject(Id id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		IdentifiedDomainObject<?> that = (IdentifiedDomainObject<?>) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id);
	}

	public Id getId() {
		return id;
	}

	public void setId(Id id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return String.valueOf(getClass()) + "{id=" + String.valueOf(getId()) + "}";
	}

}
