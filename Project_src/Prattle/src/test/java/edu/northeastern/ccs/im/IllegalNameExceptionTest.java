package edu.northeastern.ccs.im;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class IllegalNameExceptionTest {

	@Test
	void test() {
		IllegalNameException instance = new IllegalNameException("Failure");
		assertTrue(instance instanceof IllegalNameException);
	}

}
