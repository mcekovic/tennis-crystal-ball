package org.strangeforest.tcb.util;

import java.util.*;
import java.util.stream.*;

import static java.util.stream.Collectors.*;

public interface CodedEnum {

	String getCode();
	String getText();

	static <E extends Enum<E> & CodedEnum> E decode(Class<E> enumClass, String code) {
		for (E e : enumClass.getEnumConstants()) {
			if (e.getCode().equals(code))
				return e;
		}
		throw new IllegalArgumentException(String.format("Invalid %1$s code: %2$s", enumClass.getSimpleName(), code));
	}

	static <E extends Enum<E> & CodedEnum> E safeDecode(Class<E> enumClass, String code) {
		return code != null ? decode(enumClass, code) : null;
	}

	static <E extends Enum<E> & CodedEnum> String safeEncode(E e) {
		return e != null ? e.getCode() : null;
	}

	static <E extends Enum<E> & CodedEnum> Map<String, String> asMap(Class<E> enumClass) {
		return Stream.<CodedEnum>of(enumClass.getEnumConstants()).collect(toMap(CodedEnum::getCode, CodedEnum::getText));
	}
}
