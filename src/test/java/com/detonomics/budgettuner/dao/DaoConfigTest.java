
package com.detonomics.budgettuner.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.Test;

class DaoConfigTest {

    @Test
    void testGetAndSetDbPath() {
        String originalPath = DaoConfig.getDbPath();
        try {
            String newPath = "test/path/db.db";
            DaoConfig.setDbPath(newPath);
            assertEquals(newPath, DaoConfig.getDbPath());
        } finally {
            DaoConfig.setDbPath(originalPath);
        }
    }

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<DaoConfig> constructor = DaoConfig.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertNotNull(exception.getCause());
        assertEquals(AssertionError.class, exception.getCause().getClass());
    }
}
