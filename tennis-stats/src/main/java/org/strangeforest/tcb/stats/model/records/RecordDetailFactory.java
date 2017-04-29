package org.strangeforest.tcb.stats.model.records;

import java.io.*;

import com.fasterxml.jackson.databind.*;

public class RecordDetailFactory {

	private final ObjectReader reader;

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

	public RecordDetailFactory(Class<? extends RecordDetail> detailClass) {
		reader = JSON_MAPPER.readerFor(detailClass);
	}

	public RecordDetail createDetail(String json) throws IOException {
		return reader.readValue(json);
	}
}
