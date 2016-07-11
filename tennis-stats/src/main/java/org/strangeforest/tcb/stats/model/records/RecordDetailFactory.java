package org.strangeforest.tcb.stats.model.records;

import java.io.*;

import com.fasterxml.jackson.databind.*;

public class RecordDetailFactory {

	public static RecordDetailFactory forClass(Class<? extends RecordDetail> detailClass) {
		return new RecordDetailFactory(detailClass);
	}

	private final ObjectReader reader;

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

	private RecordDetailFactory(Class<? extends RecordDetail> detailClass) {
		this.reader = JSON_MAPPER.readerFor(detailClass);
	}

	public RecordDetail createDetail(String json) throws IOException {
		return reader.readValue(json);
	}
}
