package edu.northeastern.ccs.im;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.im.NextDoesNotExistException;
/*
 * @author : Mahima Singh
 * The below class tests NextDoesNotExistException.
 */
class TestDoesNotExistException {

	@Test
	void test() {
		assertTrue(new NextDoesNotExistException("message") instanceof NextDoesNotExistException);
	}

}
