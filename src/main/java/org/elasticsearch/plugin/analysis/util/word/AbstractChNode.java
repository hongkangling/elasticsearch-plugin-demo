package org.elasticsearch.plugin.analysis.util.word;

/**
 * @Description
 * 
 * @Copyright Copyright (c)2011
 * 
 * @Company homedo.com
 * 
 * @Author li_yao
 * 
 * @Version 1.0
 * 
 * @Create-at 2011-8-5 10:02:56
 * 
 * @Modification-history <br>
 * 						Date Author Version Description <br>
 * 						----------------------------------------------------------
 *                       <br>
 * 						2011-8-5 10:02:56 li_yao 1.0 Newly created
 */
public abstract class AbstractChNode<T> implements ChNode<T> {
	protected char ch;
	protected AbstractChNode<T>[] children;
	protected int subSize = 0;

	public AbstractChNode(char ch) {
		this.ch = ch;
	}

	public char getCh() {
		return ch;
	}

	public boolean hasChildren() {
		return subSize > 0;
	}

	abstract protected AbstractChNode<T>[] createNodes(int length);

	abstract protected AbstractChNode<T> createNode(char ch);

	protected AbstractChNode<T>[] ensureCapacity(int minCapacity) {
		int newLength = 2;
		if (children == null || children.length < minCapacity && (newLength = children.length + 2) > 0) {
			return createNodes(newLength);
		}
		return null;
	}

	protected AbstractChNode<T> addChar(char subCh) {
		AbstractChNode<T> subNode = null;
		int pos = search(subCh);
		if (pos >= 0) {// already exist
			subNode = children[pos];
		} else {// not exist yet

			// construct a new subNode
			subNode = createNode(subCh);

			// insert the new subNode to the right position
			pos = -pos - 1;
			AbstractChNode<T>[] newChildren = ensureCapacity(++subSize);
			if (newChildren == null) {// insert into the orignal children
				for (int i = subSize - 1; i > pos; i--) {
					children[i] = children[i - 1];
				}
				children[pos] = subNode;
			} else {// copy to the new children
				for (int i = 0; i < pos; i++) {
					newChildren[i] = children[i];
				}
				for (int i = subSize - 1; i > pos; i--) {
					newChildren[i] = children[i - 1];
				}
				newChildren[pos] = subNode;
				children = newChildren;
			}

		}
		return subNode;
	}

	public AbstractChNode<T> prefixSearch(String word, int index) {
		AbstractChNode<T> destNode = null;
		char ch = word.charAt(index);
		int pos = search(ch);
		if (pos >= 0) {
			destNode = children[pos];
			if (++index < word.length() && destNode.hasChildren()) {
				AbstractChNode<T> subNode = destNode.prefixSearch(word, index);
				if (subNode != null) {
					destNode = subNode;
				}
			}
		}
		return destNode;
	}

	public AbstractChNode<T> search(String word, int index) {
		char ch = word.charAt(index);
		int pos = search(ch);
		if (pos >= 0) {
			if (++index < word.length()) {
				return children[pos].search(word, index);
			}
			return children[pos];
		}
		return null;
	}

	/**
	 * binary search
	 * 
	 * @param subCh
	 * @return
	 */
	public int search(char subCh) {
		if (children == null) {
			return -1;
		}
		int low = 0;
		int high = subSize - 1;
		while (low <= high) {
			int mid = (low + high) >>> 1;
			char midCh = children[mid].ch;
			if (subCh > midCh) {
				low = mid + 1;
			} else if (subCh < midCh) {
				high = mid - 1;
			} else {
				return mid;
			}
		}
		return -(low + 1);
	}

}
