package edu.ksu.cis.bandera.staticanalyses.support;

public abstract class FastUnionFindElement {

	protected FastUnionFindElement set;

	public boolean isAtomic() {
		return true;
	}

	public boolean isBound() {
		return false;
	}

	public boolean sameType(FastUnionFindElement e) {
		return false;
	}

	public boolean unifyComponents(FastUnionFindElement e) {
		return true;
	}

	public final FastUnionFindElement find() {
		FastUnionFindElement result = this;
		while (result.set != null)
			result = result.set;
		if (result != this)
			set = result;
		return result;
	}

	public final void union(FastUnionFindElement e) {
		FastUnionFindElement a = find();
		FastUnionFindElement b = e.find();
		if (a != b) {
			if (b.isBound())
				a.set = b;
			else // if a.isBound() or neither is bound
				b.set = a;
		}
	}

	public boolean unify(FastUnionFindElement e) {
		boolean result = false;
		FastUnionFindElement a, b;
		a = find();
		b = e.find();
		if (a == b || a.sameType(b))
			result = true;
		else if (!(a.isAtomic() || b.isAtomic())) {
			a.union(b);
			result = a.unifyComponents(b);
		} else if (!(a.isBound() && b.isBound())) {
			a.union(b);
			result = true;
		}

		return result;
	}
}
