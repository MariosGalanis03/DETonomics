
package com.detonomics.budgettuner.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class AnalysisTypeTest {

    @Test
    void testEnumValues() {
        assertEquals(3, AnalysisType.values().length);
        assertEquals(AnalysisType.REVENUE, AnalysisType.valueOf("REVENUE"));
        assertEquals(AnalysisType.EXPENSE, AnalysisType.valueOf("EXPENSE"));
        assertEquals(AnalysisType.MINISTRY, AnalysisType.valueOf("MINISTRY"));
    }
}
