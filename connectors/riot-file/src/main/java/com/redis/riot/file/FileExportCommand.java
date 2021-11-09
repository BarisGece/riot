package com.redis.riot.file;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonObjectMarshaller;
import org.springframework.batch.item.resource.support.JsonResourceItemWriterBuilder;
import org.springframework.batch.item.xml.support.XmlResourceItemWriterBuilder;
import org.springframework.core.io.WritableResource;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.redis.riot.AbstractExportCommand;
import com.redis.spring.batch.support.DataStructure;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "export", description = "Export Redis data to JSON or XML files")
public class FileExportCommand extends AbstractExportCommand<DataStructure<String>> {

	private static final Logger log = LoggerFactory.getLogger(FileExportCommand.class);

	private static final String NAME = "file-export";

	@CommandLine.Parameters(arity = "1", description = "File path or URL", paramLabel = "FILE")
	private String file;
	@CommandLine.ArgGroup(exclusive = false, heading = "File export options%n")
	private FileExportOptions options = new FileExportOptions();

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public FileExportOptions getOptions() {
		return options;
	}

	@Override
	protected Flow flow() throws Exception {
		return flow(NAME, step(NAME, String.format("Exporting to %s", file), writer()).build());
	}

	private ItemWriter<DataStructure<String>> writer() throws IOException {
		WritableResource resource = options.outputResource(file);
		DumpFileType fileType = fileType();
		if (fileType == DumpFileType.XML) {
			XmlResourceItemWriterBuilder<DataStructure<String>> xmlWriterBuilder = new XmlResourceItemWriterBuilder<>();
			xmlWriterBuilder.name("xml-resource-item-writer");
			xmlWriterBuilder.append(options.isAppend());
			xmlWriterBuilder.encoding(options.getEncoding().name());
			xmlWriterBuilder.xmlObjectMarshaller(xmlMarshaller());
			xmlWriterBuilder.lineSeparator(options.getLineSeparator());
			xmlWriterBuilder.rootName(options.getRootName());
			xmlWriterBuilder.resource(resource);
			xmlWriterBuilder.saveState(false);
			log.debug("Creating XML writer with {} for file {}", options, file);
			return xmlWriterBuilder.build();
		}
		JsonResourceItemWriterBuilder<DataStructure<String>> jsonWriterBuilder = new JsonResourceItemWriterBuilder<>();
		jsonWriterBuilder.name("json-resource-item-writer");
		jsonWriterBuilder.append(options.isAppend());
		jsonWriterBuilder.encoding(options.getEncoding().name());
		jsonWriterBuilder.jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>());
		jsonWriterBuilder.lineSeparator(options.getLineSeparator());
		jsonWriterBuilder.resource(resource);
		jsonWriterBuilder.saveState(false);
		log.debug("Creating JSON writer with {} for file {}", options, file);
		return jsonWriterBuilder.build();
	}

	private DumpFileType fileType() {
		if (options.getType() == null) {
			return DumpFileType.of(file);
		}
		return options.getType();
	}

	private JsonObjectMarshaller<DataStructure<String>> xmlMarshaller() {
		XmlMapper mapper = new XmlMapper();
		mapper.setConfig(mapper.getSerializationConfig().withRootName(options.getElementName()));
		JacksonJsonObjectMarshaller<DataStructure<String>> marshaller = new JacksonJsonObjectMarshaller<>();
		marshaller.setObjectMapper(mapper);
		return marshaller;
	}

}
