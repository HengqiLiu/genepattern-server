package org.genepattern.server.indexer;

import org.apache.lucene.demo.html.HTMLParser;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.genepattern.server.genepattern.IDocumentCreator;

import java.io.File;
import java.io.IOException;

public class IndexHTML implements IDocumentCreator {

	public Document index(File f) throws IOException, InterruptedException {
		Document doc = new Document();
		HTMLParser parser = new HTMLParser(f);

		// Add the tag-stripped contents as a Reader-valued Text field so it
		// will
		// get tokenized and indexed.
		doc.add(Field.Text(Indexer.TASK_DOC, parser.getReader()));
		doc.add(Field.Text(Indexer.TITLE, parser.getTitle()));
		return doc;
	}
}