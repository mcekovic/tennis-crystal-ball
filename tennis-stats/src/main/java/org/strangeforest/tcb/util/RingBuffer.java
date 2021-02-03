package org.strangeforest.tcb.util;

import java.util.*;

public class RingBuffer<T> {

	private final T[] buffer;
	private int count;
	private int first;
	private int last;

	public RingBuffer(int capacity) {
		buffer = (T[])new Object[capacity];
		last = capacity - 1;
	}

	private RingBuffer(T[] buffer, int count, int first, int last) {
		this.buffer = buffer;
		this.count = count;
		this.first = first;
		this.last = last;
	}

	public boolean isEmpty() {
		return count == 0;
	}

	public int size() {
		return count;
	}

	public void push(T item) {
		last = (last + 1) % buffer.length;
		buffer[last] = item;
		if (count < buffer.length)
			count++;
		else
			first = (first + 1) % buffer.length;
	}

	public T pop() {
		var item = peekFirst();
		buffer[first] = null;
		first = (first + 1) % buffer.length;
		count--;
		return item;
	}

	public T peekFirst() {
		if (isEmpty())
			throw new NoSuchElementException("Ring buffer underflow");
		return buffer[first];

	}

	public T peekLast() {
		if (isEmpty())
			throw new NoSuchElementException("Ring buffer underflow");
		return buffer[last];
	}

	public RingBuffer<T> copy() {
		var length = this.buffer.length;
		var buffer = (T[])new Object[length];
		System.arraycopy(this.buffer, 0, buffer, 0, length);
		return new RingBuffer<>(buffer, count, first, last);
	}
}