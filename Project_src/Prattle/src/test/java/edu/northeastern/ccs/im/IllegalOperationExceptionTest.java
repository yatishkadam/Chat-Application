package edu.northeastern.ccs.im;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IllegalOperationExceptionTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testIllegalOperationException() {
		IllegalOperationException instance = new IllegalOperationException("Failure");
		
		assertTrue(instance instanceof IllegalOperationException);
	}
}
