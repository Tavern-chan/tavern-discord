package com.asm.tavern.domain.model

class IdentifiedDomainObject<Id extends Identifier> {
	private Id id

	IdentifiedDomainObject(Id id) {
		this.id = id
	}

	boolean equals(o) {
		if (this.is(o)) return true
		if (getClass() != o.class) return false

		IdentifiedDomainObject that = (IdentifiedDomainObject) o

		if (id != that.id) return false

		return true
	}

	int hashCode() {
		return (id != null ? id.hashCode() : 0)
	}

	Id getId() {
		return id
	}

	void setId(Id id) {
		this.id = id
	}

	@Override
	String toString() {
		return "${getClass()}{id=${id}}"
	}
}
