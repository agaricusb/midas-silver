package test;

import static org.junit.Assert.*;
import havocx42.TranslationRecord;
import havocx42.TranslationRecordFactory;

import org.junit.Test;

public class TranslationRecordFactoryTest {

	@Test
	public void testCreateTranslationRecordStringString() {
		TranslationRecord t = TranslationRecordFactory.createTranslationRecord("123:43   test1", "5643 test2");
		assertEquals(new Integer(123), t.source.blockID);
		assertEquals(new Integer(43), t.source.dataValue);
		assertEquals("test1", t.sourceName);
		assertEquals(new Integer(5643), t.target.blockID);
		assertEquals(null, t.target.dataValue);
		assertEquals("test2", t.targetName);
	}

	@Test
	public void testCreateTranslationRecordString() {
		TranslationRecord t =TranslationRecordFactory.createTranslationRecord("123 test1 -> 5643:0 test2");
		assertEquals(new Integer(123), t.source.blockID);
		assertEquals(null, t.source.dataValue);
		assertEquals("test1", t.sourceName);
		assertEquals(new Integer(5643), t.target.blockID);
		assertEquals(new Integer(0), t.target.dataValue);
		assertEquals("test2", t.targetName);
	}

}
